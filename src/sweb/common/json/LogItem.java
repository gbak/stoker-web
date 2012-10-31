package sweb.common.json;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonProperty;


public class LogItem
{
    @JsonProperty(value = "logName")
    public String logName;
    
    @JsonProperty(value = "cookerName")
    public String cookerName;
    
    @JsonProperty(value = "deviceList")
    public ArrayList<Device> deviceList = null;
    
    public LogItem() { }
    
    public LogItem( String logName, String cookerName, ArrayList<Device> deviceList )
    {
        this.logName = logName;
        this.cookerName = cookerName;
        this.deviceList = deviceList;
    }
}
