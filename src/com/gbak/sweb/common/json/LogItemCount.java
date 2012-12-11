package com.gbak.sweb.common.json;

import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonProperty;

public class LogItemCount
{

   /* @JsonProperty(value = "cookerName")
    public String cookerName;
    
    @JsonProperty(value = "count")
    public int count;
    */
    
    @JsonProperty( value = "logItemCount")
    public HashMap<String,Integer> logItemCount;
}
