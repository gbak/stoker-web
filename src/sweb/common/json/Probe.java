package sweb.common.json;

import org.codehaus.jackson.annotate.JsonProperty;

import sweb.common.base.constant.AlarmType;

public class Probe extends Device
{

    @JsonProperty(value="targetTemp")
    public String targetTemp;
    
    @JsonProperty(value="alarmLow")
    public String lowerTempAlarm;
    
    @JsonProperty(value="alarmHigh")
    String upperTempAlarm;
    
    @JsonProperty(value="alarmType")
    public AlarmType alarmType;
    
    @JsonProperty(value="currentTemp")
    public String currentTemp;
    
    public Probe() { }
    
    public Probe( String ID, 
                  String Name, 
                  String targetTemp,
                  String lowerTempAlarm,
                  String upperTempAlarm,
                  AlarmType alarmType,
                  String currentTemp )
    {
        super( ID, Name );
        this.targetTemp = targetTemp;
        this.lowerTempAlarm = lowerTempAlarm;
        this.upperTempAlarm = upperTempAlarm;
        this.alarmType = alarmType;
        this.currentTemp = currentTemp;
    }
    
}
