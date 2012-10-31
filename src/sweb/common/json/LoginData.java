package sweb.common.json;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class LoginData
{
    @JsonProperty(value = "username")
    public String username;
    @JsonProperty(value= "password")
    public String password;
    
    public LoginData( ) { } 
    
    @JsonIgnore
    public LoginData( String user, String password )
    {
        this.username = user;
        this.password = password;
    }
}
