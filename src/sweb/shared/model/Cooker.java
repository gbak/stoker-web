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

import sweb.shared.model.devices.SDevice;
import sweb.shared.model.devices.stoker.StokerPitSensor;
import sweb.shared.model.devices.stoker.StokerProbe;

public class Cooker implements Serializable
{

    private static final long serialVersionUID = 2387713458606878320L;
    
    String cookerName;
    StokerPitSensor pitSensor;
    ArrayList<StokerProbe> probeList = new ArrayList<StokerProbe>();
    
    // TODO: local Alerts will need to be configured here
    
    public Cooker() { }
    
    public Cooker( String cookerName )
    {
        this.cookerName = cookerName;
        
    }
    
    public Cooker( String cookerName, StokerPitSensor pitSensor )
    {
        this( cookerName );
        this.pitSensor = pitSensor;
        
    }
    
    public Cooker( String cookerName, StokerPitSensor pitSensor, ArrayList<StokerProbe> probeList )
    {
        this( cookerName, pitSensor);
        this.probeList = probeList;
    }
    
    public void setPitSensor( StokerPitSensor pitSensor )
    {
        this.pitSensor = pitSensor;
    }
    
    public void setProbeList( ArrayList<StokerProbe> probeList )
    {
        this.probeList = probeList;
    }
    
    public void addStokerProbe( StokerProbe stokerProbe )
    {
        this.probeList.add( stokerProbe );
    }
    
    public void setCookerName(String cookerName)
    {
        this.cookerName = cookerName;
    }
    
    public String getCookerName()
    {
        if ( this.cookerName != null)
           return this.cookerName;
        else
            return "null";
    }
    
    public StokerPitSensor getPitSensor()
    {
        return pitSensor;
    }
    
    public ArrayList<StokerProbe> getProbeList()
    {
        return probeList;
    }

    public void removeStokerProbe( String probeID )
    {
        for ( StokerProbe sp : probeList )
        {
            if ( sp.getID().equalsIgnoreCase(probeID));
            {
                probeList.remove(sp);
                break;
            }
        }
    }
    
    public void update( ArrayList<SDevice> arsd )
    {
        if ( pitSensor != null)
           pitSensor.update( arsd );
        for ( StokerProbe sp : probeList )
        {
            sp.update( arsd );
            // TODO: update me
        }
        
    }
    
/*
    public ArrayList<SDevice> getDeviceList()
    {
        ArrayList<SDevice> sd = new ArrayList<SDevice>();
        
        if ( pitSensor != null)
        {
           sd.add((SDevice)pitSensor);
           if ( pitSensor.getFanDevice() != null)
           {
               sd.add((SDevice) pitSensor.getFanDevice());
               
           }
        }
        for ( StokerProbe sp : probeList )
        {
            sd.add( (SDevice)sp );
        }
        return sd;
    }*/
    
    
}
