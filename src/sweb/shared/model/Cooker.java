package sweb.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

import sweb.shared.model.devices.SDevice;
import sweb.shared.model.stoker.StokerPitSensor;
import sweb.shared.model.stoker.StokerProbe;

public class Cooker implements Serializable
{
    /**
     * 
     */
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
    
    public String getCookerName()
    {
        if ( this.cookerName != null)
           return this.cookerName;
        else
            return "null";
    }
    
    public StokerPitSensor getStokerPitSensor()
    {
        return pitSensor;
    }
    
    public ArrayList<StokerProbe> getStokerProbeList()
    {
        return probeList;
    }
    
    public int getProbeCount()
    {
        int count = 0;
        if ( pitSensor != null )
            count++;
        
        count += probeList.size();
        
        return count;
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
        return sd;
    }
    
    public SDevice getDeviceByID( String ID )
    {
        if ( pitSensor.getID().equalsIgnoreCase(ID))
            return pitSensor;
        
        
        for ( SDevice p : probeList )
        {
            if ( p.getID().equalsIgnoreCase(ID))
            {
                return p;
            }
        }
     
        return null;
    }
}
