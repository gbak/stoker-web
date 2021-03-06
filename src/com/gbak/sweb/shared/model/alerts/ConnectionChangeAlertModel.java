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

package com.gbak.sweb.shared.model.alerts;

public class ConnectionChangeAlertModel extends AlertModel
{
   private static final long serialVersionUID = 7846663410227329176L;

   private static final String m_ConnectionChangeAlertName = "Connection Change Alarm";
   
   public ConnectionChangeAlertModel()
   {
      super( m_ConnectionChangeAlertName );
   }
   
   public ConnectionChangeAlertModel( boolean b )
   {
      super( m_ConnectionChangeAlertName, b );
   }
}
