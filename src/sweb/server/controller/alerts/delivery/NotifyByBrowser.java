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

package sweb.server.controller.alerts.delivery;

import java.util.ArrayList;

import sweb.shared.model.alerts.BrowserAlarmModel;

public class NotifyByBrowser implements Notify
{

   @Override
   public void init()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void sendAlert(ArrayList<String> message)
   {
      // TODO Auto-generated method stub
      try
      {
         String[] sa = new String[] { };
         String strSubject = "message from stoker";
         String strMessage = "unknown message";
         
         if ( message.size() == 1 )
         {
            strSubject = message.get(0);
            strMessage = message.get(0);
         }
         else if ( message.size() > 1 )
         {
            strSubject = message.get(0);
            StringBuilder sb = new StringBuilder();
            for ( int i = 0; i < message.size(); i++)
            {
               sb.append(message.get(i) + "\n");
            }
            strMessage = sb.toString();
         }
         
         BrowserAlarmModel alarm = new BrowserAlarmModel();
         alarm.setMessage(strMessage);
         
         BrowserDelivery.send(alarm);
         
      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }
      
   }


}
