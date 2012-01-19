package sweb.server.controller.alerts.conditions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import sweb.server.StokerWebProperties;
import sweb.server.controller.Controller;
import sweb.server.controller.StokerConfiguration;
import sweb.server.controller.alerts.delivery.Messenger;
import sweb.server.controller.alerts.delivery.Notify;
import sweb.server.controller.alerts.delivery.NotifyByEmail;
import sweb.server.controller.alerts.delivery.NotifyByBrowser;
import sweb.server.controller.data.DataOrchestrator;
import sweb.server.controller.events.ConfigControllerEvent;
import sweb.server.controller.events.ConfigControllerEventListener;

import sweb.server.controller.events.DataPointEvent;
import sweb.server.controller.events.DataPointEventListener;

import sweb.shared.model.SDevice;
import sweb.shared.model.SProbeDataPoint;
import sweb.shared.model.StokerProbe;
import sweb.shared.model.alerts.AlertModel;
import sweb.shared.model.alerts.StokerAlarmAlertModel;


public class StokerAlarm extends AlertCondition
{

   public static enum TempAlertType { NONE, LOW, HIGH, FOOD };
   Date lastAlertDate = null;
   private HashMap<String,SDevice> m_hmConfig = null;
   StokerAlarmAlertModel saa = null;
         
   public StokerAlarm() { super(); init(); }
   public  StokerAlarm( boolean b ) { super(b); init(); }
   
   private ExecutorService executor = null;
   
   private static final Logger logger = Logger.getLogger(StokerAlarm.class.getName());
   
   private void init()
   {
      setConfig();
      executor = Executors.newFixedThreadPool(2);
      handleControllerEvents();
      saa = new StokerAlarmAlertModel(false);
     // saa.setAvailableDeliveryMethods(Controller.getInstance().getAvailableDeliveryMethods());
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
            Runnable worker = new CheckDataEventRunnable( de );
            executor.execute( worker );
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
         last.add( Calendar.MINUTE, 1);  // TODO: add this to StokerWebProperties
      }
         if ( lastAlertDate == null || last.before( current ) )
         {
            //SOund alarm here
            lastAlertDate = current.getTime();
            
            
            ArrayList<String> alertList = new ArrayList<String>();
            if ( t== TempAlertType.HIGH )
            {
               alertList.add("High Temperature Alarm on " + sp.getName() );
               alertList.add("  Current Temperature: " + data );
               alertList.add("  High Alarm Setting:  " + sp.getUpperTempAlarm());
            }
            else if ( t == TempAlertType.LOW )
            {
               alertList.add("Low Temperature Alarm on " + sp.getName() );
               alertList.add("  Current Temperature: " + data );
               alertList.add("  Low Alarm Setting:  " + sp.getLowerTempAlarm());
            }
            else if ( t == TempAlertType.FOOD )
            {
                alertList.add("Food target temperature alarm on " + sp.getName() );
                alertList.add("  Current Temperature: " + data );
                alertList.add("  Alarm Setting:  " + sp.getTargetTemp());
                
            }
            
            Messenger.deliver(saa.getConfiguredDeliveryMethods(), alertList );
               
         }

       // TODO: finish
      
   }
   @Override
   public void setAlertConfiguration(AlertModel ab)
   {
      if ( ab instanceof StokerAlarmAlertModel )
         saa = (StokerAlarmAlertModel) ab;
      // TODO: should probably throw in invalid class exception here.
   }
   
   @Override
   public AlertModel getAlertConfiguration()
   {
       // TODO: this is kind of a hack, this does not really belong here but I had
       // problems putting it in the constructors or init methods because of circular dependencies.
       saa.setAvailableDeliveryMethods(Controller.getInstance().getAvailableDeliveryMethods());
      return (AlertModel) saa;
   }
     
   class CheckDataEventRunnable implements Runnable
   {
      private final DataPointEvent de;
      
      CheckDataEventRunnable( DataPointEvent d )
      {
         this.de = d;
      }
      
      public void run()
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
            
            
            logger.debug("StokerAlarm: DataPoint::StateChange() StokerProbe Name: " + sp.getName());
            if ( sp.getAlarmEnabled() == StokerProbe.AlarmType.NONE )
            {
               continue;
            }
            
            if ( sp.getAlarmEnabled() == StokerProbe.AlarmType.ALARM_FIRE )
            {
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
            else if (sp.getAlarmEnabled() == StokerProbe.AlarmType.ALARM_FOOD )
            {
                float data = spdp.getData();
                if ( data > sp.getTargetTemp()  )
                {
                   soundTempAlert( TempAlertType.FOOD, sp, data );
                }
                
            }
               
            
            
         } // end for probe data point
         
         
      }  // end run
         
   }

}
