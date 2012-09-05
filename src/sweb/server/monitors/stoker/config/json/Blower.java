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

public  class Blower {
    String id;
    String name;
    String on;
    
    public Blower() { }
    public Blower( String id, String name, String on ) {
        this.id = id;
        this.name = name;
        this.on = on;
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


    public String getOn()
    {
        return on;
    }


    public void setOn(String on)
    {
        this.on = on;
    }


    public void writeDebug()
    {
        System.out.println("blower - id:" + id + " name:" + name + " on:" + on);
    }
}
