package sweb.common.json;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonProperty;

public class LogItemCountList
{
    @JsonProperty(value = "countList")
    public ArrayList<LogItemCount> countList;
    
    public LogItemCountList()
    {
        countList = new ArrayList<LogItemCount>();
    }
}
