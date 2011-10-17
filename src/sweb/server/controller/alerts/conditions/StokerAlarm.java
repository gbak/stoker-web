package sweb.server.controller.alerts.conditions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.ibm.icu.util.Calendar;

import sweb.server.controller.Controller;
import sweb.server.controller.StokerConfiguration;
import sweb.server.controller.data.DataOrchestrator;
import sweb.server.controller.events.ConfigControllerEvent;
import sweb.server.controller.events.ConfigControllerEventListener;
import sweb.server.controller.events.DataControllerEvent;
import sweb.server.controller.events.DataControllerEventListener;
import sweb.server.controller.events.DataPointEvent;
import sweb.server.controller.events.DataPointEventListener;
import sweb.server.controller.events.WeatherChangeEvent;
import sweb.server.controller.events.WeatherChangeEventListener;
import sweb.server.controller.events.DataControllerEvent.EventType;
import sweb.shared.model.SDevice;
import sweb.shared.model.SProbeDataPoint;
import sweb.shared.model.StokerProbe;
import sweb.shared.model.events.ControllerEventLight;
import sweb.shared.model.events.ControllerEventLight.EventTypeLight;

public class StokerAlarm extends Alert
{

   public static enum TempAlertType { NONE, LOW, HIGH };
   Date lastAlertDate = null;
   private HashMap<String,SDevice> m_hmConfig = null;
         
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
            ArrayList<SProbeDataPoint> aldp = de.getSProbeDataPoints();
            TempAlertType tat = TempAlertType.NONE;
            
            for ( SProbeDataPoint spdp : aldp )
            {
               SDevice sd = m_hmConfig.get(spdp.getDeviceID());
               if ( sd == null || ! sd.isProbe() )
                  continue;
               
               StokerProbe sp = (StokerProbe) sd;
               
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
               else if ( data < sp.getLowerTempAlarm()  )
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
         last.add( Calendar.MINUTE, 30);
         if ( last.before( current ) )
         {
               
         }
         
      }
      else
      {
         lastAlertDate = current.getTime();
      }
       // TODO: finish
      
   }
     
}
