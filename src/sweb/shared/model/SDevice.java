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

package sweb.shared.model;

import java.io.Serializable;

import sweb.shared.model.StokerDeviceTypes.DeviceType;

public class SDevice  implements Serializable
{

    private static final long serialVersionUID = -2040954508382938760L;
    private String strID;
    private String strName;
    private int iCookerNum;
    private String strCookerName;
    private String strDeviceLogNum;  // This is the number of the device as used in the log file.


   SDevice( String id, String name )
   {
      strID = id;
      strName = name;
      iCookerNum = 1;
      strDeviceLogNum = "";
      
      if (( strName.length() > 2 ) &&  (strName.charAt(1) == '_' ))
      {
          try { iCookerNum = new Integer( strName.substring(0,1)).intValue(); }
          catch ( NumberFormatException nfe ) {}
      }
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

   public String getName()
   {
       return strName;
   }

   public boolean isProbe()
   {
       return false;
   }

   public String getPostData()
   {
       return new String();
   }

   public String debugString()
   {
       return "\nID: [" + strID + "] Name: [" + strName + "] ";
   }

   public int getCookerNum()
   {
       return iCookerNum;
   }
   public void setCookerNum( int i )
   {
       iCookerNum = i;
   }
   public void setCookerName( String s )
   {
       strCookerName = s;
   }
   public String getCookerName()
   {
       return strCookerName;
   }

   public DeviceType getProbeType()
   {
      return DeviceType.UNKNOWN;
   }
   
   public void setDeviceLogNum(String s)
   {
       strDeviceLogNum = s;
   }
   
   public String getDeviceLogNum()
   {
       return strDeviceLogNum;
   }
}
