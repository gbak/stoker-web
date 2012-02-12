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
    
    public void Cooker()
    {
        
    }
}
