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

public class Sensor {
    
    String id;
    String name;
    String al;
    int ta;
    int th;
    int tl;
    double tc;
    String blower;
    
    public Sensor() { }
    public Sensor( String id, String name, String al, int ta, int th, int tl, double tc, String blower ) {
        this.id = id;
        this.name = name;
        this.al = al;
        this.ta = ta;
        this.th = th;
        this.tl = tl;
        this.tc = tc;
        this.blower = blower;
    }
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAl()
    {
        return al;
    }

    public void setAl(String al)
    {
        this.al = al;
    }

    public int getTa()
    {
        return ta;
    }

    public void setTa(int ta)
    {
        this.ta = ta;
    }

    public int getTh()
    {
        return th;
    }

    public void setTh(int th)
    {
        this.th = th;
    }

    public int getTl()
    {
        return tl;
    }

    public void setTl(int tl)
    {
        this.tl = tl;
    }

    public double getTc()
    {
        return tc;
    }

    public void setTc(double tc)
    {
        this.tc = tc;
    }

    public String getBlower()
    {
        return blower;
    }

    public void setBlower(String blower)
    {
        this.blower = blower;
    }

    
    
    public void writeDebug()
    {
        System.out.println("sensor - id:" + id + " name: " + name + " al:"+ al + "ta:" + ta + " th: " + th + "tl:" + tl + " tc:" + tc + " blower:" + blower );
    }
}
