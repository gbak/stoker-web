package sweb.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

import sweb.shared.model.devices.SDevice;

public class ConfigurationSettings implements Serializable
{

    private static final long serialVersionUID = 7382010422462966314L;

    private CookerList cookerList;
    private ArrayList<SDevice> availableDevices;
    
    ConfigurationSettings()
    {
        cookerList = null;
        availableDevices = null;
    }
    
    public ConfigurationSettings(CookerList cl, ArrayList<SDevice> hm )
    {
        cookerList = cl;
        availableDevices = hm;
    }
    
    public CookerList getCookerList()
    {
        return cookerList;
    }
    public void setCookerList(CookerList cookerList)
    {
        this.cookerList = cookerList;
    }
    public ArrayList<SDevice> getAvailableDevices()
    {
        return availableDevices;
    }
    public void setAvailableDevices(ArrayList<SDevice> availableDevices)
    {
        this.availableDevices = availableDevices;
    }
    
}
