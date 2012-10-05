package sweb.utils;

import sweb.common.json.Blower;
import sweb.common.json.Device;
import sweb.common.json.PitProbe;
import sweb.common.json.Probe;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.devices.stoker.StokerFan;
import sweb.shared.model.devices.stoker.StokerPitProbe;
import sweb.shared.model.devices.stoker.StokerProbe;
import sweb.shared.model.devices.stoker.StokerProbe.AlarmType;

public class ConvertUtils
{

    public static Device toDevice( SDevice sd )
    {
        return new Device(sd.getID(), sd.getName());
    }
  /*  public SDevice toSDevice( Device d )
    {
        return new SDevice( d.id, d.Name);
    }*/
    
    

    public static StokerProbe toStokerProbe( Probe p )
    {
        return new StokerProbe( p.id,
                                p.Name,
                                Integer.valueOf(p.targetTemp).intValue(), 
                                Integer.valueOf(p.upperTempAlarm).intValue(), 
                                Integer.valueOf(p.lowerTempAlarm).intValue(), 
                                convertAlarmType(p.alarmType),
                                Float.valueOf(p.currentTemp ).floatValue());
    }
    
    public static Probe toProbe( StokerProbe sp )
    {
       return new Probe( sp.getID(),
                         sp.getName(),
                         String.valueOf(sp.getTargetTemp()),
                         String.valueOf(sp.getLowerTempAlarm()),
                         String.valueOf(sp.getUpperTempAlarm()),
                         convertAlarmType(sp.getAlarmEnabled()),
                         String.valueOf(sp.getCurrentTemp()));    
    }
    
    public static Probe toProbe( StokerProbe sp, SDataPoint sdp )
    {
       return new Probe( sp.getID(),
                         sp.getName(),
                         String.valueOf(sp.getTargetTemp()),
                         String.valueOf(sp.getLowerTempAlarm()),
                         String.valueOf(sp.getUpperTempAlarm()),
                         convertAlarmType(sp.getAlarmEnabled()),
                         String.valueOf(sdp.getData()));    
    }
    
    public static StokerFan toStokerFan( Blower b)
    {
        StokerFan sf = new StokerFan( b.id, b.Name );
        sf.setFanOn( b.fanOn );
        sf.setTotalRuntime( b.totalRuntime );
        return sf;
    }
    
    public static StokerPitProbe toStokerPitSensor( PitProbe pp )
    {
        return new StokerPitProbe(toStokerProbe( (Probe) pp ), toStokerFan( pp.blower ) );
    }
    
    public static PitProbe toPitProbe( StokerPitProbe sps )
    {
        return new PitProbe( toProbe( (StokerProbe) sps ), toBlower(sps.getFanDevice()));
    }
    
    public static PitProbe toPitProbe( StokerPitProbe sps, SDataPoint sdp )
    {
        return new PitProbe( toProbe( (StokerProbe) sps, sdp ), toBlower(sps.getFanDevice()));
    }
    
    public static Blower toBlower( StokerFan sf )
    {
        return new Blower(sf.getID(), sf.getName(), sf.isFanOn(), sf.getTotalRuntime());
    }
    
     public static sweb.common.base.constant.AlarmType convertAlarmType( AlarmType at )
     { 
         sweb.common.base.constant.AlarmType newType = sweb.common.base.constant.AlarmType.NONE;;
         switch ( at )
         {
             case ALARM_FOOD:
                 newType = sweb.common.base.constant.AlarmType.ALARM_FOOD;
                 break;
             case ALARM_FIRE:
                 newType = sweb.common.base.constant.AlarmType.ALARM_FIRE;
                 break;
         }
         return newType;
     }
     
     public static AlarmType convertAlarmType( sweb.common.base.constant.AlarmType at )
     { 
         switch ( at )
         {
             case NONE:
                 return AlarmType.NONE;
             case ALARM_FOOD:
                 return AlarmType.ALARM_FOOD;
             case ALARM_FIRE:
                 return AlarmType.ALARM_FIRE;
         }
         return AlarmType.NONE;
     }
}
