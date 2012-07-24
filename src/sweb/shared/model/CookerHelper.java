package sweb.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

import sweb.shared.model.devices.SDevice;
import sweb.shared.model.stoker.StokerPitSensor;
import sweb.shared.model.stoker.StokerProbe;

public class CookerHelper  implements Serializable
{

    private static final long serialVersionUID = -7918341887496824145L;


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

        if ( cooker.getPitSensor().getID().equalsIgnoreCase(ID))
            return cooker.getPitSensor();
        
        
        for ( SDevice p : cooker.getProbeList() )
        {
            if ( p.getID().equalsIgnoreCase(ID))
            {
                return p;
            }
        }
     
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
