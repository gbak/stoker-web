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

package sweb.shared.model.data;
import java.io.Serializable;
import java.util.Date;



public abstract class SDataPoint  implements Serializable
{

    private static final long serialVersionUID = -3544817415196333092L;
    private String  m_deviceID;
    private String  m_deviceName;
    private Date    m_collectedTime;
    private boolean m_bTimedEvent = false;

    // name field is optional, it is needed when creating the graph since the name field can
    // change over time and the current cooker state will only return the current probe name.

   public SDataPoint()
   {
      m_deviceID = null;
      m_collectedTime = null;
   }

   public SDataPoint( SDataPoint sdp )
   {
       m_deviceID = sdp.getDeviceID();
       m_deviceName = sdp.getDeviceName();
       m_collectedTime = sdp.getCollectedDate();
       m_bTimedEvent = sdp.isTimedEvent();
   }

   public SDataPoint( String device, Date date )
   {
       m_deviceID = device;
       m_collectedTime = date;
   }
   
   public SDataPoint( String device, String name, Date date )
   {
       m_deviceID = device;
       m_deviceName = name;
       m_collectedTime = date;
   }

   public String getDeviceName()
   {
       return m_deviceName;
   }
   
   public void setDeviceName(String name)
   {
       m_deviceName = name;
   }
   
   /**
    * Test to see if the name field exists in the datapoint
    * @return true of name is not null and lengh > 0
    */
   public boolean hasName()
   {
       if ( m_deviceName == null  || m_deviceName.length() == 0 )
           return false;
       
       return true;
   }
   
   public String getDeviceID()
   {
       return m_deviceID;
   }

   public boolean isTimedEvent()
   {
       return m_bTimedEvent;
   }

   public void setTimedEvent( boolean b)
   {
      m_bTimedEvent = b;
   }
  /* public boolean isFan()
   {
       return false;
   }*/

   public Date getCollectedDate()
   {
       return m_collectedTime;
   }

   public void setCollectedDate(Date d)
   {
       m_collectedTime = d;
   }

   public void update( SDataPoint sdp )
   {
       m_collectedTime = sdp.getCollectedDate();
       m_bTimedEvent = sdp.isTimedEvent();
   }
   
   public abstract boolean compare( SDataPoint sdp);

   public abstract float getData();

   public String getDebugString()
   {
       StringBuilder sb = new StringBuilder();
       sb.append("DeviceID: " + m_deviceID + "\n");
       sb.append("collectedTime: " + m_collectedTime + "\n");
       sb.append("Timed Event: " + m_bTimedEvent + "\n");
       return sb.toString();
       		
   }
   /*
   public static void main(String[] args)
   {
      SDataPoint ddp = new SDataPoint();
      try
      {
         ddp.digest( "E70000116F279030: 2 28.1 82.6 -6.9 0.2 1.1 0.9 25.9 78.6 PID: NORM tgt:26.1 error:1.2 drive:0 istate:0 on:1 off:9 blwr:off");
         System.out.println("out: " + ddp.debugString());
         System.out.println("outS: " + ddp.toString());

         System.out.println("out2: " + new SDataPoint( "DB0000116F0BEC30: 3 28.9 84 -7.5 0.2 1.2 1 27.3 81").debugString());
         System.out.println("out3: " + new SDataPoint( "B0000116F0BEC30: 3 28.9 84 -7.5 0.2 1.2 1 27.3 81").debugString());
      }
      catch( InvalidDataPointException idp )
      {
         System.out.println(idp.getMessage());
      }
   }
   */
}
