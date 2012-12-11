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

package com.gbak.sweb.shared.model.devices.stoker;

import java.io.Serializable;
import java.util.ArrayList;

import com.gbak.sweb.shared.model.data.SProbeDataPoint;
import com.gbak.sweb.shared.model.devices.SDevice;
import com.gbak.sweb.shared.model.stoker.StokerDeviceTypes.DeviceType;


public class StokerProbe extends SDevice implements Serializable
{

    private static final long serialVersionUID = 4566639321784483771L;

    public static enum AlarmType { NONE, ALARM_FOOD, ALARM_FIRE };

    int targetTemp;
    int lowerTempAlarm;
    int upperTempAlarm;
    AlarmType alarm;
    float fCurrentTemp;

    protected StokerProbe() { super(); }

    public StokerProbe(String id, String name)
    {
        super(id, name);
        targetTemp = 0;
        lowerTempAlarm = 0;
        upperTempAlarm = 0;
        alarm = AlarmType.NONE;
    }

    public StokerProbe(String id, String name, int f )
    {
        super(id, name);
        targetTemp = f;
        lowerTempAlarm = 0;
        upperTempAlarm = 0;
        alarm = AlarmType.NONE;
    }

    public StokerProbe( StokerProbe sp )
    {
       super( sp.getID(), sp.getName() );
       targetTemp = sp.targetTemp;
       lowerTempAlarm = sp.lowerTempAlarm;
       upperTempAlarm = sp.upperTempAlarm;
       alarm = sp.alarm;
    }

    public StokerProbe(String id, String name, int target, int upperTempAlarm, int lowerTempAlarm )
    {
        super(id, name);
        targetTemp = target;
        this.lowerTempAlarm = lowerTempAlarm;
        this.upperTempAlarm = upperTempAlarm;
        this.alarm = AlarmType.NONE;
    }
    
    public StokerProbe(String id, String name, int targetTemp, int upperTempAlarm, int  lowerTempAlarm, AlarmType alarmType )
    {
        super(id, name);
        this.targetTemp = targetTemp;
        this.lowerTempAlarm = lowerTempAlarm;
        this.upperTempAlarm = upperTempAlarm;
        this.alarm = alarmType;
    }

    public StokerProbe( String id, 
                        String name, 
                        int targetTemp, 
                        int upperTempAlarm, 
                        int  lowerTempAlarm, 
                        AlarmType alarmType,
                        float currentTemp )
    {
        super(id, name);
        this.targetTemp = targetTemp;
        this.lowerTempAlarm = lowerTempAlarm;
        this.upperTempAlarm = upperTempAlarm;
        this.alarm = alarmType;
        this.fCurrentTemp = currentTemp;
    }
    public void setCurrentTemp( float f )
    {
        fCurrentTemp = f;
    }

    public float getCurrentTemp()
    {
        return fCurrentTemp;
    }

    public boolean isProbe()
    {
        return true;
    }

    public void setTargetTemp( int f )
    {
        targetTemp = f;
    }
    public int getTargetTemp()
    {
        return targetTemp;
    }

    public void setLowerTempAlarm( int f )
    {
        lowerTempAlarm = f;
    }
    public int getLowerTempAlarm()
    {
        return lowerTempAlarm;
    }

    public void setUpperTempAlarm( int f )
    {
        upperTempAlarm = f;
    }
    public int getUpperTempAlarm()
    {
        return upperTempAlarm;
    }

    public void setAlarmEnabled( AlarmType a )
    {
        alarm = a;
    }
    public AlarmType getAlarmEnabled()
    {
        return alarm;
    }

    public void setFanDevice( StokerFan sf )
    {
        // Dummy method for jackson
    }
    public StokerFan getFanDevice()
    {
        return null;
    }
    public String debugString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.debugString());
        sb.append("Target Temp: [" + targetTemp + "] ");
        sb.append("Lower Temp Alarm: [" + lowerTempAlarm + "] ");
        sb.append("Upper Temp Alarm: [" + upperTempAlarm + "] ");
        sb.append("Alarm Type: [" + alarm + "] ");
        sb.append("Current Temp: [" + fCurrentTemp + "] ");

        return sb.toString();
    }

    public void update( SProbeDataPoint dp )
    {
        fCurrentTemp = dp.getTempF();
    }

    public String printString()
    {
        String str = new String();
        str = "Name: " + this.getName() + " - Alarm: " + this.getAlarmEnabled();
        if ( this.getAlarmEnabled() == AlarmType.ALARM_FIRE )
        {
            str = str + " - Low Temp: " + this.getLowerTempAlarm() + " - High Temp: " + this.getUpperTempAlarm();
        }
        else if ( this.getAlarmEnabled() == AlarmType.ALARM_FOOD)
        {
            str = str + " - TargetTemp: " + this.getTargetTemp();
        }
        //str = str + " - ID: " + this.getID() + "\n";
        str = str + "\n";
  
        return (str);
    }

    
    @Override
    public DeviceType getProbeType()
    {
       return DeviceType.FOOD;
    }
    
    public void setProbeType(DeviceType type)
    {
       
    }

    public static AlarmType getAlarmTypeForString( String str )
    {
        String s = str.toLowerCase();
        if ( s.contains("fire"))
            return AlarmType.ALARM_FIRE;
        if ( s.contains("food"))
            return AlarmType.ALARM_FOOD;

        return AlarmType.NONE;
    }
   
    
    public void update( StokerProbe sp )
    {
        super.update( (SDevice) sp );
        targetTemp = sp.getTargetTemp();

        lowerTempAlarm = sp.getLowerTempAlarm();
        upperTempAlarm = sp.getUpperTempAlarm();
        alarm = sp.getAlarmEnabled();

    }
    
    public void update( ArrayList<SDevice> arsd )
    {
        for ( SDevice sp : arsd )
        {
            if ( sp.getID().compareToIgnoreCase(m_ID) == 0)
            {
                if ( sp instanceof StokerProbe )
                   update( (StokerProbe) sp );
                else
                    update( sp );
            }
        }
    }
}
