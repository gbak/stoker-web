package com.gbak.sweb.common.json;

import org.codehaus.jackson.annotate.JsonProperty;

import com.gbak.sweb.common.base.constant.AlarmType;


public class PitProbe extends Probe
{
    @JsonProperty(value = "blower")
    public Blower blower;

    public PitProbe()
    {
    }

    public PitProbe(String ID, String Name, String targetTemp,
            String lowerTempAlarm, String upperTempAlarm, AlarmType alarmType,
            String currentTemp, Blower blower)
    {
        super(ID, Name, targetTemp, lowerTempAlarm, upperTempAlarm, alarmType,
                currentTemp);
        this.blower = blower;
    }

    public PitProbe(String ID, String Name, String targetTemp,
            String lowerTempAlarm, String upperTempAlarm, AlarmType alarmType,
            String currentTemp, Blower blower, String cooker)
    {
        super(ID, Name, targetTemp, lowerTempAlarm, upperTempAlarm, alarmType,
                currentTemp, cooker);
        this.blower = blower;
    }

    public PitProbe(Probe probe, Blower blower)
    {
        this(probe.id, probe.Name, probe.targetTemp, probe.lowerTempAlarm,
                probe.upperTempAlarm, probe.alarmType, probe.currentTemp,
                blower);
    }

}
