package sweb.common.json;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonProperty;

public class Cooker
{
    @JsonProperty(value="name")
    public String name;
    
    @JsonProperty(value="pitProbe")
    public PitProbe pitProbe;
    
    @JsonProperty(value="probeList")
    public ArrayList<Probe> probeList;
    
    public Cooker()
    {
        probeList = new ArrayList<Probe>();
    }
}
