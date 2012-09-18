/**
 *  Stoker-web
 *
 *  Copyright (C) 2011  Gary Bak
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

package sweb.server.alerts;

import java.util.ArrayList;
import java.util.Set;

import org.apache.log4j.Logger;

import sweb.server.alerts.conditions.ConnectionOrConfigChangeAlert;
import sweb.server.alerts.conditions.StokerAlarm;
import sweb.server.alerts.conditions.TempAlert;
import sweb.server.alerts.conditions.TimedAlert;
import sweb.server.alerts.delivery.Messenger;
import sweb.shared.model.alerts.AlertModel;
import sweb.shared.model.alerts.ConnectionChangeAlertModel;
import sweb.shared.model.alerts.StokerAlarmAlertModel;

import com.google.inject.Inject;

public class AlertsManagerImpl implements AlertManager
{
   StokerAlarm stokerAlarm; // = new StokerAlarm(false);
   ConnectionOrConfigChangeAlert connConfigChangeAlarm = new ConnectionOrConfigChangeAlert(false);
   ArrayList<TempAlert> tempAlert = new ArrayList<TempAlert>();
   ArrayList<TimedAlert> timedAlert = new ArrayList<TimedAlert>();

   private static final Logger logger = Logger.getLogger(AlertsManagerImpl.class.getName());
   
   @Inject
   public AlertsManagerImpl(StokerAlarm sa, Messenger m)
   {
       stokerAlarm = sa;
   }

   /*
    * Set configuration options sent by Client.  All configuration comes in as the base AlertModel class.
    */
   @Override
   public void setConfiguration(ArrayList<AlertModel> alertBaseList)
   {
      logger.debug("AlertsController::setConfiguration");
      for ( AlertModel ab : alertBaseList )
      {
         if ( ab instanceof StokerAlarmAlertModel )
         {
            stokerAlarm.setAlertConfiguration(ab);
         }
         else if ( ab instanceof ConnectionChangeAlertModel )
         {
            
         }
      }
   }

   @Override
   public ArrayList<AlertModel> getConfiguration()
   {
      ArrayList<AlertModel> alertBaseList = new ArrayList<AlertModel>();
      
      alertBaseList.add(stokerAlarm.getAlertConfiguration());
      alertBaseList.add(connConfigChangeAlarm.getAlertConfiguration());
      
      return alertBaseList;
      
   }
   /*
   public void init()
   {
      
       
      AlertsClassLoader cl = new AlertsClassLoader();
      try
      {
         Class<AlertsBase> notifyClass = cl.loadClass("sweb.server.notify.delivery.NotifyByEmail");
         AlertsBase notify = notifyClass.newInstance();

         notify.sendNotification();
         notify.optionalNotification();

      }
      catch (ClassNotFoundException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (InstantiationException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (IllegalAccessException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }


   }
*/
   public Set<String> getAvailableDeliveryMethods()
   {
       return Messenger.getDeliveryChannels();
       
   }
   
   public static void main(String[] args)
   {
     // AlertsController nm = new AlertsController();

     // nm.init();
   }


}
