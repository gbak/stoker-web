package sweb.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

public class CookerList  implements Serializable
{

    private static final long serialVersionUID = -5245139149478373870L;
    
    ArrayList<Cooker> cookerList;
    
    public CookerList() { cookerList = new ArrayList<Cooker>(); }
    
    public ArrayList<Cooker> getCookerList()
    {
        return cookerList;
    }
    
    public void setCookerList( ArrayList<Cooker> cookerList )
    {
        this.cookerList = cookerList;
    }
}
