package sweb.common.json;

import java.util.ArrayList;
import java.util.Date;


public class DeviceDataList //extends ArrayList<Device>
{   
    public ArrayList<Device> devices;
    public Date receivedDate;
    
    public DeviceDataList()
    {
        devices = new ArrayList<Device>();
    }
}
