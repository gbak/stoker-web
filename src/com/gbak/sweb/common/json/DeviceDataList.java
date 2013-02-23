package com.gbak.sweb.common.json;

import java.util.ArrayList;
import java.util.Date;


public class DeviceDataList //extends ArrayList<Device>
{   
    public ArrayList<Device> devices;
 //  public ArrayList<Device> availableBlowers;
    public Date receivedDate;
    public LogItemCount logCount;
    
    public DeviceDataList()
    {
        devices = new ArrayList<Device>();
    //    availableBlowers = new ArrayList<Device>();
    }
    
}
