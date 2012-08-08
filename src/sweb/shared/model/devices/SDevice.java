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
    private String strID;
    private String strName;

   protected SDevice( String id, String name )
   {
      strID = id.toUpperCase();
      strName = name;
   }

   public boolean equals( SDevice sd )
   {
       return strID.compareToIgnoreCase(sd.getID()) == 0;
   }
   protected SDevice()  { }

   public String getID()
   {
       return strID;
   }
   
   public void setID(String id)
   {
       strID = id.toUpperCase();
   }

   public void setName(String name)
   {
       strName = name;
   }
   public String getName()
   {
       return strName;
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
       return "\nID: [" + strID + "] Name: [" + strName + "] ";
   }

   public DeviceType getProbeType()
   {
      return DeviceType.UNKNOWN;
   }
   
   public void setProbeType( DeviceType dt )
   {
       
   }
   
 
}
