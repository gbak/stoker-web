package sweb.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

import sweb.shared.model.devices.SDevice;
import sweb.shared.model.stoker.StokerPitSensor;
import sweb.shared.model.stoker.StokerProbe;

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
