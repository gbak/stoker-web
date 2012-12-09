package com.gbak.sweb.common.json;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonProperty;

public class LogNote
{
    @JsonProperty(value = "note")
    public String note;
    
    @JsonProperty(value = "logList")
    public ArrayList<String> logList;
    
    public LogNote() {  logList = new ArrayList<String>(); }
    
    public LogNote( String note, ArrayList<String> logList )
    {
        this.note  = note;
        this.logList = logList;
    }
}
