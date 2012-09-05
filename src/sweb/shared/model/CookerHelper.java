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

package sweb.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

//import org.apache.log4j.Logger;


import sweb.shared.model.devices.SDevice;
import sweb.shared.model.stoker.StokerPitSensor;
import sweb.shared.model.stoker.StokerProbe;

public class CookerHelper  implements Serializable
{

    private static final long serialVersionUID = -7918341887496824145L;
  //  private static final Logger logger = Logger.getLogger(CookerHelper.class.getName());

    public static SDevice getDeviceByID(CookerList cl,  String ID )
    {
        for ( Cooker c : cl.getCookerList() )
        {
            return getDeviceByID( c, ID );
        }
        return null;
    }
    
    public static SDevice getDeviceByID( Cooker cooker, String ID )
    {

     //   logger.trace("CookerHelper::getDeviceByID() searching for: " + ID );
        if ( cooker.getPitSensor() != null )
           if ( cooker.getPitSensor().getID().equalsIgnoreCase(ID))
           {
         //      logger.trace("CookerHelper::getDeviceByID() found pit sensor: " + cooker.getPitSensor());
              return cooker.getPitSensor();
           }
        
        for ( SDevice p : cooker.getProbeList() )
        {
            if ( p.getID().equalsIgnoreCase(ID))
            {
         //       logger.trace("CookerHelper::getDeviceByID() found probe: " + p.getID());
                return p;
            }
        }
     
     //   logger.trace("CookerHelper::getDeviceByID() found nothing");
        return null;
    }
    
    public static int getProbeCount(Cooker cooker)
    {
        int count = 0;
        if ( cooker.getPitSensor() != null )
            count++;
        
        count += cooker.getProbeList().size();
        
        return count;
    }
    

    public static ArrayList<SDevice> getDeviceList(Cooker cooker)
    {
        ArrayList<SDevice> sd = new ArrayList<SDevice>();
        
        StokerPitSensor pitSensor = cooker.getPitSensor();
        if ( pitSensor != null)
        {
           sd.add((SDevice)pitSensor);
           if ( pitSensor.getFanDevice() != null)
           {
               sd.add((SDevice) pitSensor.getFanDevice());
               
           }
        }
        
        for ( StokerProbe sp : cooker.getProbeList() )
        {
            sd.add( (SDevice)sp );
        }
        return sd;
    }
    
}
