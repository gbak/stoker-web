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

package sweb.server.controller;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import sweb.server.controller.config.stoker.StokerConfigurationController;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.stoker.StokerFan;
import sweb.shared.model.stoker.StokerPitSensor;
import sweb.shared.model.stoker.StokerDeviceTypes.DeviceType;

/**
 * @author gary.bak
 *
 */
public class StokerConfiguration
{

   // private volatile static StokerConfiguration stc;
    HashMap<String,SDevice> htStokConfig = new HashMap<String,SDevice>();
    private volatile boolean bisUpToDate = false;

    private static final Logger logger = Logger.getLogger(StokerConfiguration.class.getName());
    
   /* public static StokerConfiguration getInstance()
    {
        if ( stc == null)
        {
            synchronized ( StokerConfiguration.class)
            {
                if ( stc == null )
                {
                    stc = new StokerConfiguration();
                }
            }
        }
        return stc;
    }*/

    public HashMap<String,SDevice> data()
    {
        return htStokConfig;
    }

    public boolean isUpToDate()
    {
        return bisUpToDate;
    }
    public void setUpdatedStaus( boolean b)
    {
        bisUpToDate = b;
    }


    protected StokerConfiguration()
    {

    }


    /**
     *  Clears out the configuration table for upcoming reload of config data
     *
     *   Any existing items in the list will be replaced with addDevice and
     *   new items will also be added, but devices that have been removed
     *   will never be detected.  This wipes the existing configuration and
     *   starts over.
     */
    public synchronized void clear()
    {
        htStokConfig.clear();
    }

    /**
     * Add device to configuration
     * @param d Device to add to configuration.  This will replace any existing
     * device in the configuration with the same device ID.
     */
    public synchronized void addDevice( SDevice d )
    {
           htStokConfig.put( d.getID(), d);
    }

    public synchronized SDevice getDevice( String ID)
    {
        SDevice sd = htStokConfig.get(ID);
        return sd;
    }

    /**
     * Check to see if device ID exists in the Configuration
     * @param ID String ID of device in question
     * @return true if device exists in configuration
     */
    public synchronized boolean hasDevice( String ID)
    {
        return htStokConfig.containsKey(ID);

    }
    public synchronized void replaceDevice( SDevice d )
    {
       htStokConfig.remove(d.getID());
       htStokConfig.put( d.getID(), d );

    }
    /*
    public HashMap<String,SDevice> getHashMap()
    {
        return htStokConfig;
    }
    */
    public Set<Entry<String,SDevice>> getEntrySet()
    {
        return htStokConfig.entrySet();
    }

    /**
     * Returns all devices found on Stoker
     *
     * @return
     */
    public synchronized ArrayList<SDevice> getAllDevices()
    {
        return ( new ArrayList<SDevice>(Collections.unmodifiableCollection( htStokConfig.values())));
    }


    /** Get the blower ID for the probe ID passed in.
     * @param strProbeID String ID of probe
     * @return Blower ID if the probe pass in was a pit probe and has associated fan device.
     *         null otherwise.
     */
    public String getBlowerID( String strProbeID )
    {
        SDevice sd =  htStokConfig.get( strProbeID );
        if ( sd != null)
        {
            if ( sd.getProbeType() == DeviceType.PIT )
            {
                StokerFan sf = ((StokerPitSensor)sd).getFanDevice();
                return sf.getID();
            }
        }
        return null;
    }

    public ArrayList<String> getAllBlowerIDs()
    {
       ArrayList<String> arBlowers = new ArrayList<String>();

       for ( SDevice sd : htStokConfig.values())
       {
           if ( sd.getProbeType() == DeviceType.PIT )
           {
               StokerFan sf = ((StokerPitSensor)sd).getFanDevice();
               arBlowers.add( sf.getID());           }
       }
       return arBlowers;

    }

    public Integer update( ArrayList<SDevice> asd )
    {
        StokerConfigurationController.postUpdate(asd);

        return new Integer(1);
    }

    public String debugString()
    {
        StringBuilder sb = new StringBuilder();
        Set<Entry<String, SDevice>> configSet = htStokConfig.entrySet();
        Iterator<Entry<String,SDevice>> iter = configSet.iterator();
        while ( iter.hasNext() )
        {
            Entry<String,SDevice> e = iter.next();
            SDevice d = e.getValue();
            sb.append(d.debugString());

        }
        return sb.toString();
    }
}
