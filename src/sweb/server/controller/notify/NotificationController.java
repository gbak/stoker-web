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

package sweb.server.controller.notify;

public class NotificationController
{

   NotificationController()
   {

   }

   public void init()
   {
      NotificationClassLoader cl = new NotificationClassLoader();
      try
      {
         Class<NotificationBase> notifyClass = cl.loadClass("sweb.server.notify.delivery.NotifyByEmail");
         NotificationBase notify = notifyClass.newInstance();

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

   public static void main(String[] args)
   {
      NotificationController nm = new NotificationController();

      nm.init();
   }
}
