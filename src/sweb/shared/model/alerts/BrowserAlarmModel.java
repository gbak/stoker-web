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
import java.util.Date;

public class BrowserAlarmModel  implements Serializable
{
    private static final long serialVersionUID = 6295441263225889854L;

    String m_AlarmName = "";
    String m_Message = "";
    String m_SupressAlarmDuration = "";
    Date   m_AlarmSupressTime = null;
    
    public BrowserAlarmModel() { }
    public BrowserAlarmModel( String AlarmName ) { m_AlarmName = AlarmName; }
    
    public String getMessage() { return m_Message; }
    public void setMessage( String message) { m_Message = message; }
    
}
