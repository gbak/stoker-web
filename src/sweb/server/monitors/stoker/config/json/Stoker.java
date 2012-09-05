/**
 *  Stoker-web
 *
 *  Copyright (C) 2012  Gary Bak
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package sweb.server.monitors.stoker.config.json;

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
