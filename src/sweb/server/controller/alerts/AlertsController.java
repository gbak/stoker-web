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

package sweb.server.controller.alerts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import sweb.server.controller.alerts.conditions.ConnectionOrConfigChangeAlert;
import sweb.server.controller.alerts.conditions.StokerAlarm;
import sweb.server.controller.alerts.conditions.TempAlert;
import sweb.server.controller.alerts.conditions.TimedAlert;
import sweb.shared.model.alerts.Alert;
import sweb.shared.model.alerts.ConnectionChangeAlert;
import sweb.shared.model.alerts.StokerAlarmAlert;

public class AlertsController
{
   StokerAlarm stokerAlarm = new StokerAlarm(false);
   ConnectionOrConfigChangeAlert connConfigChangeAlarm = new ConnectionOrConfigChangeAlert(false);
   ArrayList<TempAlert> tempAlert = new ArrayList<TempAlert>();
   ArrayList<TimedAlert> timedAlert = new ArrayList<TimedAlert>();
   
   Set<String> availableDeliveryList = new HashSet<String>();

   public AlertsController()
   {
       availableDeliveryList.add( "Email" );
       availableDeliveryList.add( "Browser Alert" );
   }

   public void setConfiguration(ArrayList<Alert> alertBaseList)
   {
      System.out.println("AlertsController::setConfiguration");
      for ( Alert ab : alertBaseList )
      {
         if ( ab instanceof StokerAlarmAlert )
         {
            stokerAlarm.setAlertConfiguration(ab);
         }
         else if ( ab instanceof ConnectionChangeAlert )
         {
            
         }
      }
   }

   
   public ArrayList<Alert> getConfiguration()
   {
      ArrayList<Alert> alertBaseList = new ArrayList<Alert>();
      
      alertBaseList.add(stokerAlarm.getAlertConfiguration());
      alertBaseList.add(connConfigChangeAlarm.getAlertConfiguration());
      
      return alertBaseList;
      
   }
   
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

   public Set<String> getAvailableDeliveryMethods()
   {
       return availableDeliveryList;
       
   }
   
   public static void main(String[] args)
   {
      AlertsController nm = new AlertsController();

      nm.init();
   }
}
