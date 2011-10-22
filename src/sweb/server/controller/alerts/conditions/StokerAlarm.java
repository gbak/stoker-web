package sweb.server.controller.alerts.conditions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import sweb.server.controller.Controller;
import sweb.server.controller.StokerConfiguration;
import sweb.server.controller.alerts.delivery.AlertDelivery;
import sweb.server.controller.alerts.delivery.NotifyByEmail;
import sweb.server.controller.data.DataOrchestrator;
import sweb.server.controller.events.ConfigControllerEvent;
import sweb.server.controller.events.ConfigControllerEventListener;

import sweb.server.controller.events.DataPointEvent;
import sweb.server.controller.events.DataPointEventListener;

import sweb.shared.model.SDevice;
import sweb.shared.model.SProbeDataPoint;
import sweb.shared.model.StokerProbe;
import sweb.shared.model.alerts.AlertBase;
import sweb.shared.model.alerts.StokerAlarmAlert;


public class StokerAlarm extends Alert
{

   public static enum TempAlertType { NONE, LOW, HIGH };
   Date lastAlertDate = null;
   private HashMap<String,SDevice> m_hmConfig = null;
   StokerAlarmAlert saa = null;
         
   public StokerAlarm() { super(); init(); }
   public  StokerAlarm( boolean b ) { super(b); init(); }
   
   
   private void init()
   {
      setConfig();
      handleControllerEvents();
   }
   
   private void setConfig()
   {
      m_hmConfig = StokerConfiguration.getInstance().data();   
   }
   
   private void handleControllerEvents()
   {

      DataPointEventListener m_dl = new DataPointEventListener() {

         public void stateChange(DataPointEvent de)
         {
            // saa is null then it has not been configured yet, skip
            if ( saa == null || saa.getEnabled() == false )
               return;
            
            ArrayList<SProbeDataPoint> aldp = de.getSProbeDataPoints();
            TempAlertType tat = TempAlertType.NONE;
            
            for ( SProbeDataPoint spdp : aldp )
            {
               SDevice sd = m_hmConfig.get(spdp.getDeviceID());
               if ( sd == null || ! sd.isProbe() )
                  continue;
               
               StokerProbe sp = (StokerProbe) sd;
               
               // TODO: log debug
              // System.out.println("StokerAlarm: DataPoint::StateChange() StokerProbe Name: " + sp.getName());
               if ( sp.getAlarmEnabled() == StokerProbe.AlarmType.NONE )
               {
                  continue;
               }
               
               // Alarm is enabled for device
               float data = spdp.getData();
               if ( data > sp.getUpperTempAlarm()  )
               {
                  soundTempAlert( TempAlertType.HIGH, sp, data );
               }
               else if ( sp.getAlarmEnabled() == StokerProbe.AlarmType.ALARM_FIRE && data < sp.getLowerTempAlarm()  )
               {
                  soundTempAlert( TempAlertType.LOW, sp, data );
               }
               
               
            }
            
         }
      };
      
      DataOrchestrator.getInstance().addListener(m_dl);

       ConfigControllerEventListener m_ccel = new ConfigControllerEventListener() {

           public void actionPerformed(ConfigControllerEvent ce)
           {
              if ( saa == null || saa.getEnabled() == false )
                 return;
              
               switch( ce.getEventType())
               {
                   case NONE:
                       break;
                   case CONFIG_UPDATE:
                       setConfig();
                       break;
                   default:
               }
           }

          };

       Controller.getInstance().addConfigEventListener(m_ccel);

   }
   
   private void soundTempAlert( TempAlertType t, StokerProbe sp, float data )
   {
      Calendar current = Calendar.getInstance();
      Calendar last = Calendar.getInstance();
      
      if ( lastAlertDate != null )
      {
         last.setTime( lastAlertDate );
         last.add( Calendar.MINUTE, 1);
      }
         if ( lastAlertDate == null || last.before( current ) )
         {
            //SOund alarm here
            lastAlertDate = current.getTime();
            
            
            ArrayList<String> alertList = new ArrayList<String>();
            if ( t== TempAlertType.HIGH )
            {
               alertList.add("High Temperature Alarm" );
               alertList.add("  Current Temperature: " + data );
               alertList.add("  High Alarm Setting:  " + sp.getUpperTempAlarm());
            }
            else if ( t == TempAlertType.LOW )
            {
               alertList.add("Low Temperature Alarm" );
               alertList.add("  Current Temperature: " + data );
               alertList.add("  Low Alarm Setting:  " + sp.getLowerTempAlarm());
            }
            
            // Get delivery channel and send!
            //System.out.println("Alarm condition: " + sb.toString());
            AlertDelivery ad = new NotifyByEmail();
            ad.sendAlert(alertList);
         }

       // TODO: finish
      
   }
   @Override
   public void setAlertConfiguration(AlertBase ab)
   {
      if ( ab instanceof StokerAlarmAlert )
         saa = (StokerAlarmAlert) ab;
      // TODO: should probably throw in invalid class exception here.
   }
   @Override
   public AlertBase getAlertConfiguration()
   {
      return (AlertBase) saa;
   }
     
}
