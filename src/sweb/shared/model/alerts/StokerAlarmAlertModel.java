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

package sweb.shared.model.alerts;

import java.io.Serializable;

public class StokerAlarmAlertModel extends AlertModel implements Serializable
{
   private static final long serialVersionUID = -3312639618804325814L;

   private static final String m_StokerAlertName = "Stoker Alarm"; 

   public StokerAlarmAlertModel()
   {
      super( m_StokerAlertName );
   }

   public StokerAlarmAlertModel(boolean b)
   {
      super( m_StokerAlertName, b );
   }
}
