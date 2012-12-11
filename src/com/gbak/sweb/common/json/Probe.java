package com.gbak.sweb.common.json;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.gbak.sweb.common.base.constant.AlarmType;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
{ @JsonSubTypes.Type(value = PitProbe.class, name = "pit") })
public class Probe extends Device
{

    @JsonProperty(value = "targetTemp")
    public String targetTemp;

    @JsonProperty(value = "alarmLow")
    public String lowerTempAlarm;

    @JsonProperty(value = "alarmHigh")
    public String upperTempAlarm;

    @JsonProperty(value = "alarmType")
    public AlarmType alarmType;

    @JsonProperty(value = "currentTemp")
    public String currentTemp;

    public Probe()
    {
    }

    public Probe(String ID, String Name, String targetTemp,
            String lowerTempAlarm, String upperTempAlarm, AlarmType alarmType,
            String currentTemp)
    {
        super(ID, Name);
        this.targetTemp = targetTemp;
        this.lowerTempAlarm = lowerTempAlarm;
        this.upperTempAlarm = upperTempAlarm;
        this.alarmType = alarmType;
        this.currentTemp = currentTemp;
    }

    public Probe(String ID, String Name, String targetTemp,
            String lowerTempAlarm, String upperTempAlarm, AlarmType alarmType,
            String currentTemp, String cooker)
    {
        super(ID, Name, cooker);
        this.targetTemp = targetTemp;
        this.lowerTempAlarm = lowerTempAlarm;
        this.upperTempAlarm = upperTempAlarm;
        this.alarmType = alarmType;
        this.currentTemp = currentTemp;
    }

}
