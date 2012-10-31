package sweb.utils;

import java.util.ArrayList;

import sweb.common.json.Blower;
import sweb.common.json.Device;
import sweb.common.json.DeviceDataList;
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

    public static ArrayList<SDevice> toSDeviceList( DeviceDataList ddl )
    {
        ArrayList<SDevice> deviceList = new ArrayList<SDevice>();
        
       for ( Device d : ddl.devices )
       {
           deviceList.add( toDevice(d));
       }
       return deviceList;
    }
    
    public static Device toDevice( SDevice sd )
    {
        if ( sd instanceof StokerPitProbe )
        {
           return (Device) toPitProbe( (StokerPitProbe) sd );    
        }
        else if ( sd instanceof StokerProbe )
        {
            return (Device) toProbe( (StokerProbe) sd );
        } 
        else if ( sd instanceof StokerFan )
        {
            return (Device) toBlower( (StokerFan) sd );
        }
         
        
        return new Device(sd.getID(), sd.getName());
    }

    public static SDevice toDevice( Device d )
    {
        if ( d instanceof PitProbe )
        {
            return (SDevice) toStokerPitSensor( (PitProbe) d );
        }
        else if ( d instanceof Probe )
        {
            return (SDevice) toStokerProbe( (Probe) d );
            
        }
        else if ( d instanceof Blower )
        {
            return (SDevice) toStokerFan( (Blower) d );
        }
        
        
        return null;
    }
    
    
    public static sweb.common.json.Cooker toCooker( sweb.shared.model.Cooker sharedCooker )
    {
        sweb.common.json.Cooker jsonCooker = new sweb.common.json.Cooker();
        
        jsonCooker.name = sharedCooker.getCookerName();
        jsonCooker.pitProbe = toPitProbe( sharedCooker.getPitSensor() );
        
        for ( sweb.shared.model.devices.stoker.StokerProbe sp : sharedCooker.getProbeList() )
        {
            jsonCooker.probeList.add( toProbe( sp ));
        }
        return jsonCooker;
    }
    
    public static sweb.shared.model.Cooker toCooker( sweb.common.json.Cooker  jsonCooker )
    {
        
        sweb.shared.model.Cooker sharedCooker = new sweb.shared.model.Cooker();
        
        sharedCooker.setCookerName(jsonCooker.name);
        sharedCooker.setPitSensor( toStokerPitSensor( jsonCooker.pitProbe ));
        
        ArrayList<StokerProbe> alsp = new ArrayList<StokerProbe>();
        
        for ( sweb.common.json.Probe probe : jsonCooker.probeList )
        {
            alsp.add( toStokerProbe( probe ));
        }
        
        sharedCooker.setProbeList( alsp );
        
        return sharedCooker;
    }
    
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
