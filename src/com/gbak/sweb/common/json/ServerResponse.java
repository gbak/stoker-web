package com.gbak.sweb.common.json;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;


public class ServerResponse<T>
{
    @JsonProperty(value = "success")
    public boolean success;
    
    @JsonProperty(value = "data")
    public T data;
    
    @JsonProperty(value="messages")
    public ArrayList<String> messages;
    
    @JsonProperty( value = "date")
    public Date date;
    
    public ServerResponse() { date = Calendar.getInstance().getTime();
                       messages = new ArrayList<String>(); }
    
}
