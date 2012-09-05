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

package sweb.server.controller.alerts.conditions;

import sweb.shared.model.alerts.AlertModel;


public abstract class AlertCondition
{

    // TODO: make this an interface
   boolean m_Enabled = false;
   String  m_Identifier = "";
   
   AlertCondition() { }
   public AlertCondition( boolean b ) { m_Enabled = b; }
   
   public void setEnabled( boolean b ) { m_Enabled = b; }
   public boolean getEnabled()  { return m_Enabled; }
   
   public void setIdentifier( String id ) { m_Identifier = id; }
   public String getIdentifier( ) { return(m_Identifier); }
   
   public abstract void setAlertConfiguration( AlertModel ab );
   public abstract AlertModel getAlertConfiguration();
   
   
}
