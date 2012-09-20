/**
 *  Stoker-web
 *
 *  Copyright (C) 2012  Gary Bak
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package sweb.server.alerts.conditions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import sweb.server.StokerWebConstants;
import sweb.server.StokerWebProperties;
import sweb.server.alerts.AlertManager;
import sweb.server.alerts.delivery.Messenger;
import sweb.server.config.StokerWebConfiguration;
import sweb.server.events.ConfigChangeEvent;
import sweb.server.events.DataPointEvent;

import sweb.server.monitors.PitMonitor;
import sweb.shared.model.alerts.AlertModel;
import sweb.shared.model.alerts.StokerAlarmAlertModel;
import sweb.shared.model.data.SProbeDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.devices.stoker.StokerProbe;


public class StokerAlarm extends AlertCondition
{

   public static enum TempAlertType { NONE, LOW, HIGH, FOOD };
   Date lastAlertDate = null;
   private StokerAlarmAlertModel saa = null;
         
   //private Controller controller;
  // private PitMonitor m_pitMonitor;
   private StokerWebConfiguration m_stokerWebConfiguration;
   private AlertManager m_alertManager;
   
   Integer m_alarmRepeatMinutes = StokerWebConstants.ALARM_REPEAT_TIMER_MINUTES;
   
   
   @Inject
   public StokerAlarm(StokerWebConfiguration stokerWebConfiguration, AlertManager alert, EventBus eventBus) 
   { 
       super(); 
       this.m_stokerWebConfiguration = stokerWebConfiguration;
       this.m_alertManager = alert;
       eventBus.register(this);
       init();
   }
  // public  StokerAlarm( boolean b ) { super(b); init(); }
  
   private ExecutorService executor = null;
   
   private static final Logger logger = Logger.getLogger(StokerAlarm.class.getName());
   
   
   private void init()
   {

      executor = Executors.newFixedThreadPool(3);
      
      saa = new StokerAlarmAlertModel(false);

      String alarmRepeat = StokerWebProperties.getInstance().getProperty(StokerWebConstants.PROPS_ALARM_REPEAT_TIMER);
      if ( alarmRepeat != null )
      {
          try
          {
              m_alarmRepeatMinutes = new Integer(alarmRepeat);
              
          }
          catch( NumberFormatException nfe )
          {
              logger.warn("Invalid minute value for property: " + StokerWebConstants.PROPS_ALARM_REPEAT_TIMER);
          }
          if ( m_alarmRepeatMinutes < 1 )
              m_alarmRepeatMinutes = 1;
          
      }

   }
   
   @Subscribe
   public void handleDataPointEvent(DataPointEvent de)
   {
       Runnable worker = new CheckDataEventRunnable( de );
       executor.execute( worker );
   }
   
   @Subscribe
   public void handleConfigChangeEvent( ConfigChangeEvent ce )
   {
       if ( saa == null || saa.getEnabled() == false )
           return;
        
         switch( ce.getEventType())
         {
             case NONE:
                 break;
             case CONFIG_UPDATE_DETECTED:
     //            setConfig();
                 break;
             default:
         }
   }
  
   
   private void soundTempAlert( TempAlertType t, StokerProbe sp, float data )
   {
      Calendar current = Calendar.getInstance();
      Calendar last = Calendar.getInstance();
      
      if ( lastAlertDate != null )
      {
         last.setTime( lastAlertDate );
         last.add( Calendar.MINUTE, m_alarmRepeatMinutes.intValue()); 
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
       saa.setAvailableDeliveryMethods(m_alertManager.getAvailableDeliveryMethods());
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
             SDevice sd = m_stokerWebConfiguration.getDeviceByID( spdp.getDeviceID());

            if ( sd == null || ! sd.isProbe() )
               continue;
            
            StokerProbe sp = (StokerProbe) sd;
            
            
            logger.debug("StokerAlarm: DataPoint::StateChange() StokerProbe Name: " + sp.getName() + " Alarm: " + sp.getAlarmEnabled() );
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
