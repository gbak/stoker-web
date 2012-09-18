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

import sweb.shared.model.alerts.AlertModel;
import sweb.shared.model.alerts.ConnectionChangeAlertModel;

public class ConnectionOrConfigChangeAlert extends AlertCondition
{

   ConnectionChangeAlertModel cca = null;
   
   public ConnectionOrConfigChangeAlert() { super(); }
   public ConnectionOrConfigChangeAlert( boolean b ) { super(b); }
   @Override
   public void setAlertConfiguration(AlertModel ab)
   {
      if ( ab instanceof ConnectionChangeAlertModel )
         cca = (ConnectionChangeAlertModel) ab;
      // TODO: error on else condition
      
   }
   @Override
   public AlertModel getAlertConfiguration() 
   {
      
      return (AlertModel) cca;
   }
   
   
}
