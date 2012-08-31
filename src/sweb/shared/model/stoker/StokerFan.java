/**
 *  Stoker-web
 *
 *  Copyright (C) 2011  Gary Bak
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

package sweb.shared.model.stoker;

import java.io.Serializable;

import sweb.shared.model.data.SBlowerDataPoint;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.stoker.StokerDeviceTypes.DeviceType;



public class StokerFan extends SDevice  implements Serializable
{

    private static final long serialVersionUID = 7336076181286776312L;
    boolean bFanOn = false;
    long  m_TotalRuntime = 0;

    protected StokerFan()
    {
        super();
    }

    public StokerFan(String id, String name)
    {
        super(id, name);
        bFanOn = false;
    }

    public void startFan()
    {
        bFanOn = true;
    }

    public void stopFan()
    {
       bFanOn = false;
    }

    public void setFanOn( boolean b)
    {
        bFanOn = b;
    }
    public boolean isFanOn()
    {
        return bFanOn;
    }

    public long getTotalRuntime()
    {
        return m_TotalRuntime;
    }
    public void setTotalRuntime(long r )
    {
        m_TotalRuntime = r;
    }
    
    public String debugString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( super.debugString());

        return sb.toString();
    }
    public void update( SDataPoint dp)
    {
        if ( dp instanceof SBlowerDataPoint )
        {
           bFanOn = ((SBlowerDataPoint)dp).isFanOn();
           m_TotalRuntime = ((SBlowerDataPoint)dp).getTotalRuntime();
        }
    }
    
    public String printString()
    {
        return ("Name: " + this.getName() + "\n");
    }
    
    @Override
    public DeviceType getProbeType()
    {
       return DeviceType.BLOWER;
    }
    
    public void setProbeType( DeviceType dt)
    {
        
    }
    
    public void update( StokerFan sf )
    {
        super.update( sf );
    }
}
