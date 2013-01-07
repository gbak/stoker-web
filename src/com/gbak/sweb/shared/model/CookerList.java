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

package com.gbak.sweb.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.gbak.sweb.shared.model.devices.SDevice;
import com.gbak.sweb.shared.model.events.ControllerEventLight.EventTypeLight;


public class CookerList  implements Serializable
{

    private static final long serialVersionUID = -5245139149478373870L;
    
    protected ArrayList<Cooker> cookerList;
    protected boolean configRequired = false;
    protected EventTypeLight status = EventTypeLight.NONE;
    
    public CookerList() { cookerList = new ArrayList<Cooker>(); }
    
    public ArrayList<Cooker> getCookerList()
    {
        return cookerList;
    }
    
    public void setConfigRequired( boolean required )
    {
        configRequired = required;
    }
    
    public boolean getConfigRequired()
    {
        return configRequired;
    }
    public void setCookerList( ArrayList<Cooker> cookerList )
    {
        this.cookerList = cookerList;
    }
    
    public  void update( ArrayList<SDevice> arsd )
    {
        for ( Cooker cooker : cookerList )
        {
            cooker.update( arsd );
        }
    }

    public void setStatus( EventTypeLight el )
    {
        status = el;
    }
    
    public EventTypeLight getStatus()
    {
        return status;
    }
    
}
