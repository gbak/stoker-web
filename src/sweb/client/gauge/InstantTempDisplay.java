package sweb.client.gauge;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsRenderable;

import sweb.shared.model.StokerProbe;
import sweb.shared.model.StokerProbe.AlarmType;


public abstract class InstantTempDisplay extends Composite
{

    StokerProbe localProbe = null;
    protected enum TempAlert { HIGH, LOW, NONE }; 
    
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
            if ( tempAlert == tempAlert.NONE )
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
