package sweb.common.json;

import java.util.ArrayList;
import java.util.List;

public class StokerJson
{
    
    public Stoker stoker;
    
    public static class Stoker
    {
        public List<Sensors> sensors;
        public List<Blowers> blowers;
        
        public Stoker() 
        { 
            sensors = new ArrayList<Sensors>();
            blowers = new ArrayList<Blowers>();
        }
    }
    
    public static class Sensors
    {
        public String id;
        public String name;
        public String al;
        public String ta;
        public String th;
        public String tl;
        public String tc;
        public String blower;
    }
    
    public static class Blowers
    {
        public String id;
        public String name;
        public String on;
    }
}
