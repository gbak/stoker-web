package com.gbak.sweb.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.gbak.sweb.common.json.Blower;
import com.gbak.sweb.common.json.ConfigurationSettings;
import com.gbak.sweb.common.json.Device;
import com.gbak.sweb.common.json.DeviceDataList;
import com.gbak.sweb.common.json.LogItem;
import com.gbak.sweb.common.json.LogItemCount;
import com.gbak.sweb.common.json.LogItemCountList;
import com.gbak.sweb.common.json.PitProbe;
import com.gbak.sweb.common.json.Probe;
import com.gbak.sweb.shared.model.data.SDataPoint;
import com.gbak.sweb.shared.model.devices.SDevice;
import com.gbak.sweb.shared.model.devices.stoker.StokerFan;
import com.gbak.sweb.shared.model.devices.stoker.StokerPitProbe;
import com.gbak.sweb.shared.model.devices.stoker.StokerProbe;
import com.gbak.sweb.shared.model.devices.stoker.StokerProbe.AlarmType;


public class ConvertUtils
{

 
    public static ConfigurationSettings toConfigurationSettings( com.gbak.sweb.shared.model.ConfigurationSettings gwtConfig )
    {
        ConfigurationSettings cs = new ConfigurationSettings();
        if ( gwtConfig != null && gwtConfig.getCookerList() != null )
        {
            for ( com.gbak.sweb.shared.model.Cooker c : gwtConfig.getCookerList().getCookerList())
            {
               cs.cookerList.add(ConvertUtils.toCooker(c));
            }
        }
        for ( SDevice sd : gwtConfig.getAvailableDevices())
        {
            cs.deviceList.add( toDevice(sd));
        }
        return cs;
    }
    
    public static ArrayList<SDevice> toSDeviceList( DeviceDataList ddl )
    {
        ArrayList<SDevice> deviceList = new ArrayList<SDevice>();
        
       for ( Device d : ddl.devices )
       {
           deviceList.add( toDevice(d));
       }
       return deviceList;
    }
    
    public static ArrayList<SDevice> toSDeviceList( ArrayList<Device> deviceList)
    {
        ArrayList<SDevice> dl = new ArrayList<SDevice>();
        for ( Device d : deviceList )
        {
            dl.add( toDevice(d));
        }
        return dl;
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
    
    
    public static com.gbak.sweb.common.json.Cooker toCooker( com.gbak.sweb.shared.model.Cooker sharedCooker )
    {
        com.gbak.sweb.common.json.Cooker jsonCooker = new com.gbak.sweb.common.json.Cooker();
        
        jsonCooker.name = sharedCooker.getCookerName();
        jsonCooker.pitProbe = toPitProbe( sharedCooker.getPitSensor() );
        
        for ( com.gbak.sweb.shared.model.devices.stoker.StokerProbe sp : sharedCooker.getProbeList() )
        {
            jsonCooker.probeList.add( toProbe( sp ));
        }
        return jsonCooker;
    }
    
    public static com.gbak.sweb.shared.model.Cooker toCooker( com.gbak.sweb.common.json.Cooker  jsonCooker )
    {
        
        com.gbak.sweb.shared.model.Cooker sharedCooker = new com.gbak.sweb.shared.model.Cooker();
        
        sharedCooker.setCookerName(jsonCooker.name);
        sharedCooker.setPitSensor( toStokerPitSensor( jsonCooker.pitProbe ));
        
        ArrayList<StokerProbe> alsp = new ArrayList<StokerProbe>();
        
        for ( com.gbak.sweb.common.json.Probe probe : jsonCooker.probeList )
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
        if ( sp != null)
           return new Probe( sp.getID(),
                             sp.getName(),
                             String.valueOf(sp.getTargetTemp()),
                             String.valueOf(sp.getLowerTempAlarm()),
                             String.valueOf(sp.getUpperTempAlarm()),
                             convertAlarmType(sp.getAlarmEnabled()),
                             String.valueOf(sp.getCurrentTemp()));
        else
            return new Probe();
    }
    
    public static Probe toProbe( StokerProbe sp, SDataPoint sdp )
    {
        if ( sp != null)
           return new Probe( sp.getID(),
                             sp.getName(),
                             String.valueOf(sp.getTargetTemp()),
                             String.valueOf(sp.getLowerTempAlarm()),
                             String.valueOf(sp.getUpperTempAlarm()),
                             convertAlarmType(sp.getAlarmEnabled()),
                             String.valueOf(sdp != null ? sdp.getData() : "0"));
        else
            return new Probe();
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
        return sf == null ? null : new Blower(sf.getID(), sf.getName(), sf.isFanOn(), sf.getTotalRuntime());
    }
    
     public static com.gbak.sweb.common.base.constant.AlarmType convertAlarmType( AlarmType at )
     { 
         com.gbak.sweb.common.base.constant.AlarmType newType = com.gbak.sweb.common.base.constant.AlarmType.NONE;;
         switch ( at )
         {
             case ALARM_FOOD:
                 newType = com.gbak.sweb.common.base.constant.AlarmType.ALARM_FOOD;
                 break;
             case ALARM_FIRE:
                 newType = com.gbak.sweb.common.base.constant.AlarmType.ALARM_FIRE;
                 break;
         }
         return newType;
     }
     
     public static AlarmType convertAlarmType( com.gbak.sweb.common.base.constant.AlarmType at )
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
     

     public static com.gbak.sweb.shared.model.LogItem toLogItem( LogItem log )
     {
        ArrayList<SDevice> deviceList = new ArrayList<SDevice>();
        for ( Device d : log.deviceList)
        {
            deviceList.add(toDevice(d));
        }
        return new com.gbak.sweb.shared.model.LogItem(log.cookerName, log.logName, log.startDate, deviceList);
     }

     public static ArrayList<com.gbak.sweb.shared.model.LogItem> toLogItemList2( ArrayList<LogItem> logList)
     {
         ArrayList<com.gbak.sweb.shared.model.LogItem> ll = new ArrayList<com.gbak.sweb.shared.model.LogItem>();
         for ( LogItem l : logList)
             ll.add( toLogItem(l) );
         return ll;
     }
     
     public static LogItem toLogItem( com.gbak.sweb.shared.model.LogItem l )
     {
         ArrayList<Device> deviceList = new ArrayList<Device>();
         for ( SDevice sd : l.getLogItems())
         {
             if ( sd instanceof StokerFan )
                 continue;
             Device d = toDevice(sd);
             d.cooker = l.getCookerName();
             deviceList.add( d );
         }
         LogItem item = new LogItem( l.getLogName(), l.getCookerName(), l.getStartDate(), deviceList );
         return item;
     }
     
     public static ArrayList<LogItem> toLogItemList( ArrayList<com.gbak.sweb.shared.model.LogItem> list, String cookerName )
     {
         ArrayList<LogItem> logList = new ArrayList<LogItem>();
         for ( com.gbak.sweb.shared.model.LogItem li : list )
         {
             if ( cookerName == null || cookerName.length() == 0 ||  li.getCookerName().equals(cookerName))
                logList.add( toLogItem(li) );
         }
         return logList;
     }
     
     public static LogItemCount toLogItemCountList( ArrayList<com.gbak.sweb.shared.model.LogItem> list)
     {
         HashMap<String,Integer> logCount= new HashMap<String,Integer>();
         
         for ( com.gbak.sweb.shared.model.LogItem li : list )
         {
             Integer i = logCount.get(li.getCookerName());
             if ( i == null )
             {
                 i = new Integer(1);
             }
             else
             {
                 i = new Integer(i + 1);
             }
             logCount.put(li.getCookerName(), i );
         }
         LogItemCountList licList = new LogItemCountList();
         
         /*for ( Entry<String,Integer> s : logCount.entrySet())
         {
             LogItemCount lic = new LogItemCount();
             lic.cookerName = s.getKey();
             lic.count = s.getValue();
             licList.countList.add(lic);
         }*/
         
         LogItemCount lic = new LogItemCount();
         lic.logItemCount = logCount;
         return lic;
     }
}
