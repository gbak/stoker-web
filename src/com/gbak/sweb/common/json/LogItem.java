package com.gbak.sweb.common.json;

import java.util.ArrayList;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;


public class LogItem
{
    @JsonProperty(value = "logName")
    public String logName;
    
    @JsonProperty(value = "cookerName")
    public String cookerName;
    
    @JsonProperty(value= "startDate")
    public Date startDate;
    
    @JsonProperty(value = "deviceList")
    public ArrayList<Device> deviceList = null;
    
    public LogItem() { }
    
    public LogItem( String logName, String cookerName, Date startDate, ArrayList<Device> deviceList )
    {
        this.logName = logName;
        this.cookerName = cookerName;
        this.startDate = startDate;
        this.deviceList = deviceList;
    }
}
