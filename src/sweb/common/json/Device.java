package sweb.common.json;

import org.codehaus.jackson.annotate.JsonProperty;

public class Device
{
    @JsonProperty(value="id")
    public String id;
    
    @JsonProperty(value="name")
    public String Name;
    
    public Device() { }
    
    public Device( String id, String Name )
    {
        this.id = id;
        this.Name = Name;
    }
}

