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

package sweb.client.gauge;

import com.google.gwt.user.client.ui.Composite;

import sweb.shared.model.devices.stoker.StokerProbe;
import sweb.shared.model.devices.stoker.StokerProbe.AlarmType;


public abstract class InstantTempDisplay extends Composite
{

    StokerProbe localProbe = null;
    static protected enum TempAlert { HIGH, LOW, NONE }; 
    
    TempAlert tempAlert = TempAlert.NONE;
    boolean change = false;
    
    public abstract void init(StokerProbe sp);
    
    public abstract void setAlarmRange(StokerProbe stokerProbe);
    
    public abstract void draw();
    
    public abstract void setTemp( float f );
    
    public abstract void setTemp( int i );
    
    /**
     * checks to see if the stoker alarm conditions are met so the temp display
     * can change accordingly
     * 
     * This method sets local variables:
     *  tempAlert - this is set to either TempAlert.HIGH or TempAlert.LOW
     *  change - boolean indicator if the tempAlert setting has changed.
     * @param temp Temperature to check the min and max values with
     */
    public void checkAlarms(int temp)
    {
        if ( localProbe == null )
            return;
        
        AlarmType at = localProbe.getAlarmEnabled();
        
        if ( at == AlarmType.NONE )
        {
            if ( tempAlert == TempAlert.NONE )
                return;
            else
            {
                change = true;
                tempAlert = TempAlert.NONE;
                return;
            }
        }
            
        
        float l = localProbe.getLowerTempAlarm();
        float h = localProbe.getUpperTempAlarm();
        float t = localProbe.getTargetTemp();
        
        switch( at )
        {
            case ALARM_FIRE:
                if ( temp >= h )
                {
                    if ( tempAlert != TempAlert.HIGH )
                    {
                       change = true;
                       tempAlert = TempAlert.HIGH;
                    }
                }  
                else if ( temp <= l )
                {
                    if ( tempAlert != TempAlert.LOW )
                    {
                       change = true;
                       tempAlert = TempAlert.LOW;
                    }
                }
                else if ( temp > l && temp < h && tempAlert != TempAlert.NONE )
                {
                    tempAlert = TempAlert.NONE;
                    change = true;
                }
                
                break;
                
            case ALARM_FOOD:
                if ( temp >= t )
                {
                    if ( tempAlert != TempAlert.HIGH )
                    {
                       change = true;
                       tempAlert = TempAlert.HIGH;
                    }
                    else if ( tempAlert != TempAlert.NONE)
                    {
                        tempAlert = TempAlert.NONE;
                       change = true;
                    }
                }
                break;
        }  //end switch

    }

}
