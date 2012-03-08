package sweb.server.controller.config.json;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonCreator;

public class Stoker {
    
    ArrayList<Sensor> sensors;
    ArrayList<Blower> blowers;
    @JsonCreator
    public Stoker() { }
    public Stoker( ArrayList<Sensor> sensors, ArrayList<Blower> blowers ) {
        this.blowers = blowers;
        this.sensors = sensors;
    }
    
    public ArrayList<Sensor> getSensors() {
        return sensors;
    }
    public ArrayList<Blower> getBlowers(){
        return blowers;
    }
    public void setBlowers( ArrayList<Blower> blowers) {
        this.blowers = blowers;
    }
    public void setSensors( ArrayList<Sensor> sensors ) {
        this.sensors = sensors;
    }
    
    public void writeDebug()
    {
        System.out.println("sensors:");
        for ( Sensor s : sensors )
            s.writeDebug();
        System.out.println("sensors:");
        for ( Blower b : blowers )
            b.writeDebug();
       
    }
}
