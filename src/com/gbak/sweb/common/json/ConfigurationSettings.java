package com.gbak.sweb.common.json;

import java.util.ArrayList;

public class ConfigurationSettings
{

    public CookerList cookerList;
    public ArrayList<Device> deviceList;
    
    public ConfigurationSettings()
    {
        cookerList = new CookerList();
        deviceList = new ArrayList<Device>();
    }
}
