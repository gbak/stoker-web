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

package sweb.shared.model;

import java.io.Serializable;

import sweb.shared.model.StokerDeviceTypes.DeviceType;

public class StokerProbe extends SDevice implements Serializable
{

    private static final long serialVersionUID = 4566639321784483771L;

    public static enum AlarmType { NONE, ALARM_FOOD, ALARM_FIRE };

    int fTargetTemp;
    int fLowerTempAlarm;
    int fUpperTempAlarm;
    AlarmType alarm;
    float fCurrentTemp;

    protected StokerProbe() { super(); }

    public StokerProbe(String id, String name)
    {
        super(id, name);
        fTargetTemp = 0;
        fLowerTempAlarm = 0;
        fUpperTempAlarm = 0;
        alarm = AlarmType.NONE;
    }

    public StokerProbe(String id, String name, int f )
    {
        super(id, name);
        fTargetTemp = f;
        fLowerTempAlarm = 0;
        fUpperTempAlarm = 0;
        alarm = AlarmType.NONE;
    }

    public StokerProbe( StokerProbe sp )
    {
       super( sp.getID(), sp.getName() );
       fTargetTemp = sp.fTargetTemp;
       fLowerTempAlarm = sp.fLowerTempAlarm;
       fUpperTempAlarm = sp.fUpperTempAlarm;
       alarm = sp.alarm;
    }

    public StokerProbe(String id, String name, int f, int up, int dn )
    {
        super(id, name);
        fTargetTemp = f;
        fLowerTempAlarm = dn;
        fUpperTempAlarm = up;
        alarm = AlarmType.NONE;
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
        fTargetTemp = f;
    }
    public int getTargetTemp()
    {
        return fTargetTemp;
    }

    public void setLowerTempAlarm( int f )
    {
        fLowerTempAlarm = f;
    }
    public int getLowerTempAlarm()
    {
        return fLowerTempAlarm;
    }

    public void setUpperTempAlarm( int f )
    {
        fUpperTempAlarm = f;
    }
    public int getUpperTempAlarm()
    {
        return fUpperTempAlarm;
    }

    public void setAlarmEnabled( AlarmType a )
    {
        alarm = a;
    }
    public AlarmType getAlarmEnabled()
    {
        return alarm;
    }

    public StokerFan getFanDevice()
    {
        return null;
    }
    public String debugString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.debugString());
        sb.append("Target Temp: [" + fTargetTemp + "] ");
        sb.append("Lower Temp Alarm: [" + fLowerTempAlarm + "] ");
        sb.append("Upper Temp Alarm: [" + fUpperTempAlarm + "] ");
        sb.append("Alarm Type: [" + alarm + "] ");
        sb.append("Current Temp: [" + fCurrentTemp + "] ");

        return sb.toString();
    }

    public void update( SProbeDataPoint dp )
    {
        fCurrentTemp = dp.getTempF();
    }

    @Override
    public DeviceType getProbeType()
    {
       return DeviceType.FOOD;
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
}
