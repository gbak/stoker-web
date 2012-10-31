package sweb.common.json;

import java.util.Calendar;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;


public class ServerRequest<T>
{
    @JsonProperty(value= "login")
    public LoginData login;
    
    @JsonProperty(value="data")
    public T data;
    
    @JsonProperty(value= "hash")
    public String hash;
    
    @JsonProperty(value= "submitTime")
    public Date date;
    
    public ServerRequest() { date = Calendar.getInstance().getTime(); }
    

}
