package sweb.common.json;

import org.codehaus.jackson.annotate.JsonProperty;

import sweb.common.base.constant.AlarmType;


public class PitProbe extends Probe
{
    @JsonProperty(value="blower")
    Blower blower;
    
    public PitProbe() { } 
    
    public PitProbe( String ID, 
                     String Name, 
                     String targetTemp,
                     String lowerTempAlarm,
                     String upperTempAlarm,
                     AlarmType alarmType,
                     String currentTemp,
                     Blower blower )
    {
        super( ID, Name, targetTemp, lowerTempAlarm, upperTempAlarm, alarmType, currentTemp );
        this.blower = blower;
    }
    
}
