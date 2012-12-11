package com.gbak.sweb.common.json;

import java.util.ArrayList;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;

public class LogItemList
{
    @JsonProperty(value = "logList")
    public ArrayList<LogItem> logList;
    
    @JsonProperty( value = "receivedDate")
    public Date receivedDate;
    
    public LogItemList()
    {
        logList = new ArrayList<LogItem>();
    }
}
