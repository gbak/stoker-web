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

package sweb.shared.model.devices;

import java.io.Serializable;

import sweb.shared.model.stoker.StokerDeviceTypes.DeviceType;

public class SDevice  implements Serializable
{

    private static final long serialVersionUID = -2040954508382938760L;
    protected String m_ID;
    private String m_Name;

   protected SDevice( String id, String name )
   {
      m_ID = id.toUpperCase();
      m_Name = name;
   }

   public boolean equals( SDevice sd )
   {
       return m_ID.compareToIgnoreCase(sd.getID()) == 0;
   }
   protected SDevice()  { }

   public String getID()
   {
       return m_ID;
   }
   
   public void setID(String id)
   {
       m_ID = id.toUpperCase();
   }

   public void setName(String name)
   {
       m_Name = name;
   }
   public String getName()
   {
       return m_Name;
   }

   // Dummy method for Jackson.  Can't use the annotations because of gwt.
   public void setProbe(boolean b)
   {
       
   }
   public boolean isProbe() 
   {
       return false;
   }

   public String debugString()
   {
       return "\nID: [" + m_ID + "] Name: [" + m_Name + "] ";
   }

   public DeviceType getProbeType()
   {
      return DeviceType.UNKNOWN;
   }
   
   public void setProbeType( DeviceType dt )
   {
       
   }
   
   public void update( SDevice sd )
   {
       m_Name = sd.getName();
   }
 
}
