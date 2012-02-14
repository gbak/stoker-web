package sweb.shared.model;

import java.util.ArrayList;

import sweb.shared.model.stoker.StokerPitSensor;
import sweb.shared.model.stoker.StokerProbe;

public class Cooker
{
    String strCookerName;
    StokerPitSensor pitSensor;
    ArrayList<StokerProbe> probeList;
    
    // TODO: local Alerts will need to be configured here
    
    public Cooker( String cookerName )
    {
        this.strCookerName = cookerName;
        
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
        return this.strCookerName;
    }
    
    public StokerPitSensor getStokerPitSensor()
    {
        return pitSensor;
    }
    
    public ArrayList<StokerProbe> getStokerProbeList()
    {
        return probeList;
    }
}
