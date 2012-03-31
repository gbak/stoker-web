package sweb.server.controller.config.json;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;


public class StokerOuter {
    public Stoker getStoker()
    {
        return stoker;
    }

    @JsonProperty("stoker")
    public void setStoker(Stoker stoker)
    {
        this.stoker = stoker;
    }

    
    Stoker stoker;
    @JsonCreator
    public StokerOuter() { }
    public StokerOuter( Stoker stoker) {
        this.stoker = stoker;
    }
    
}