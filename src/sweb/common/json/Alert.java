package sweb.common.json;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;

public class Alert
{
    @JsonProperty(value="alertTime")
    public Date alertTime;
    
    @JsonProperty(value="type")
    public AlertType type; 
    
    @JsonProperty(value="message")
    public String message; 
    
    @JsonProperty(value="id")
    public String id;
    
    @JsonProperty(value="name")
    public String name;

    public Alert() { }
}

