package sweb.common.json;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonProperty;

public class LogItemList
{
    @JsonProperty(value = "lotList")
    public ArrayList<LogItem> logList;
    
    public LogItemList()
    {
        logList = new ArrayList<LogItem>();
    }
}
