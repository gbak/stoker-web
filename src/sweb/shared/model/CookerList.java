package sweb.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

import sweb.shared.model.devices.SDevice;

public class CookerList  implements Serializable
{

    private static final long serialVersionUID = -5245139149478373870L;
    
    protected ArrayList<Cooker> cookerList;
    
    public CookerList() { cookerList = new ArrayList<Cooker>(); }
    
    public ArrayList<Cooker> getCookerList()
    {
        return cookerList;
    }
    
    public void setCookerList( ArrayList<Cooker> cookerList )
    {
        this.cookerList = cookerList;
    }
    
    public  void update( ArrayList<SDevice> arsd )
    {
        for ( Cooker cooker : cookerList )
        {
            cooker.update( arsd );
        }
    }

}
