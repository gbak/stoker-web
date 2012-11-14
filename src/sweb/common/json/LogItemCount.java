package sweb.common.json;

import org.codehaus.jackson.annotate.JsonProperty;

public class LogItemCount
{

    @JsonProperty(value = "cookerName")
    public String cookerName;
    
    @JsonProperty(value = "count")
    public int count;
    
}
