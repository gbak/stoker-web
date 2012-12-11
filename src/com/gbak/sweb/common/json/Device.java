package com.gbak.sweb.common.json;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
    @JsonSubTypes.Type(value=Probe.class, name="probe"),
    @JsonSubTypes.Type(value=Blower.class, name="fan")
})
public class Device
{
    @JsonProperty(value="id")
    public String id;
    
    @JsonProperty(value="name")
    public String Name;
    
    @JsonProperty(value="cooker")
    public String cooker;
    
    public Device() { }
    
    public Device( String id, String Name )
    {
        this.id = id;
        this.Name = Name;
    }
    
    public Device( String id, String Name, String cooker )
    {
        this.id = id;
        this.Name = Name;
        this.cooker = cooker;
    }
}

