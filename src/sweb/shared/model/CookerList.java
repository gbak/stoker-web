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

package sweb.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

import sweb.shared.model.devices.SDevice;

public class CookerList  implements Serializable
{

    private static final long serialVersionUID = -5245139149478373870L;
    
    protected ArrayList<Cooker> cookerList;
    
    public CookerList() { cookerList = new ArrayList<Cooker>(); }
    
    public ArrayList<Cooker> getCookerList()
    {
        return cookerList;
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

}
