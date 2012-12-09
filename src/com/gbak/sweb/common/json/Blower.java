package com.gbak.sweb.common.json;

import org.codehaus.jackson.annotate.JsonProperty;

public class Blower extends Device
{
    @JsonProperty(value="fanOn")
    public Boolean fanOn;
    
    @JsonProperty(value="totalRuntime")
    public Long totalRuntime;
    
    public Blower() { }
    
    public Blower( Boolean fanOn, Long totalRuntime )
    {
        this.fanOn = fanOn;
        this.totalRuntime = totalRuntime;
    }
    
    public Blower( String ID, String Name, Boolean fanOn, Long totalRuntime )
    {
        super( ID, Name );
        this.fanOn = fanOn;
        this.totalRuntime = totalRuntime;
    }
}
