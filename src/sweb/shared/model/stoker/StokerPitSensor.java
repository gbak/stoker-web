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
import sweb.shared.model.data.SProbeDataPoint;
import sweb.shared.model.stoker.StokerDeviceTypes.DeviceType;

public class StokerPitSensor extends StokerProbe implements Serializable
{

    private static final long serialVersionUID = 462230168684643107L;
    StokerFan sfan;

    private StokerPitSensor() { super(); }

    public StokerPitSensor(String id, String name)
    {
        super(id, name);
        sfan = null;
    }
    public StokerPitSensor( StokerProbe sp, StokerFan f)
    {
        super( sp );
        sfan = f;
    }
    public StokerPitSensor( StokerProbe sp )
    {
        super( sp );
        sfan = null;
    }

    public void setFanDevice( StokerFan f)
    {
        sfan = f;
    }

    public StokerFan getFanDevice()
    {
        return sfan;
    }

    public String debugString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( super.debugString());
        if ( sfan == null )
        {
           sb.append("no fan attached"); 
        }
        else
           sb.append( "Fan: " + sfan.debugString() );
        return sb.toString();
    }

    public void update(SProbeDataPoint dp)
    {
        super.update(dp);

    }

    public void update( SBlowerDataPoint pdp)
    {
        sfan.update(pdp);
    }

    public String getPrintString()
    {
        String str = new String();
        str = "Name: " + this.getName() + " - TargetTemp: " + this.getTargetTemp() + " - Alarm: " + this.getAlarmEnabled();
        if ( this.getAlarmEnabled() == AlarmType.ALARM_FIRE )
        {
            str = str + " - Low Temp: " + this.getLowerTempAlarm() + " - High Temp: " + this.getUpperTempAlarm();
        }
        else if ( this.getAlarmEnabled() == AlarmType.ALARM_FOOD)
        {
            str = str + " - TargetTemp: " + this.getTargetTemp();
        }
        //str = str + " - ID: " + this.getID() + "\n";
       // str = str + "\n";
        
        return (str);

    }
    
    @Override
    public DeviceType getProbeType()
    {
       return DeviceType.PIT;
    }

    public boolean equals( StokerPitSensor sp )
    {
        if ( super.equals(sp ) == true )
            if ( this.getFanDevice() != null && sp.getFanDevice() != null )
               return this.getFanDevice().equals(sp.getFanDevice());
        
        return false;
        
    }
}
