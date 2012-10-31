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

package sweb.server.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import sweb.server.StokerWebConstants;
import sweb.server.StokerWebProperties;
import sweb.server.events.ConfigChangeEvent;
import sweb.server.events.ConfigChangeEvent.EventType;
import sweb.shared.model.Cooker;
import sweb.shared.model.CookerHelper;
import sweb.shared.model.CookerList;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.devices.stoker.StokerFan;
import sweb.shared.model.devices.stoker.StokerPitProbe;
import sweb.shared.model.devices.stoker.StokerProbe;
import sweb.shared.model.devices.stoker.StokerProbe.AlarmType;
import sweb.shared.model.stoker.StokerDeviceTypes.DeviceType;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class StokerWebConfiguration
{
    private static final Logger logger = Logger.getLogger(StokerWebConfiguration.class.getName());

    private static final String strConfigFile = StokerWebProperties.getInstance().getProperty(StokerWebConstants.PROPS_STOKERWEB_DIR) + 
            File.separator + "CookerConfig.json";

    private HashMap<String,SDevice> m_deviceCache = new HashMap<String,SDevice>();
    private HashMap<String,String> m_CookerCache = new HashMap<String,String>();
    private HardwareDeviceConfiguration m_deviceConfiguration = null;
    private CookerList m_cookerList;
    private EventBus m_eventBus;
    
    @Inject
    public StokerWebConfiguration(HardwareDeviceConfiguration stokerConfig,
                                  EventBus eventBus ) 
    { 
        this.m_deviceConfiguration = stokerConfig;
        this.m_eventBus = eventBus;
        eventBus.register(this);
       // init();
    }
     
    @Subscribe
    public void listenForConfigInit( ConfigChangeEvent cce)
    {
       if ( cce.getEventType() == EventType.CONFIG_INIT)
       {
           init();
       }
    }
    
    public void init()
    {
        
        if ( loadConfig() == false )
            return;

        if ( m_cookerList == null )
            m_cookerList = new CookerList();
        
        reconcile( );
        m_eventBus.post( new ConfigChangeEvent( this, EventType.CONFIG_LOADED));
    }
    
    private void loadDeviceCacheFromCookerList()
    {
        m_deviceCache.clear();
        m_CookerCache.clear();
        for ( Cooker c : m_cookerList.getCookerList() )
        {
            StokerPitProbe sps = c.getPitSensor();
            if ( sps != null )
            { 
                m_deviceCache.put( sps.getID(),sps);
                m_CookerCache.put( sps.getID(), c.getCookerName());
                StokerFan sf = sps.getFanDevice();
                if ( sf != null )
                {
                    m_deviceCache.put( sf.getID(), sf);
                    m_CookerCache.put( sf.getID(), c.getCookerName());
                }
            }
            for ( StokerProbe sp : c.getProbeList() )
            {
               m_deviceCache.put( sp.getID(), sp );
               m_CookerCache.put( sp.getID(), c.getCookerName());
            }
        }    
    }
    
    /**
     * Gets the SDevice by its ID.  This method will query the hardware controller
     * directly since the configuration may be incomplete if the .json file is missing
     * or can't be read.  
     * 
     * @param id String ID of requested device
     * @return Returns device requested, null if it does not exist.
     */
    public SDevice getDeviceByID( String id )
    {
        if ( m_cookerList == null || m_cookerList.getCookerList().size() == 0 )
           return m_deviceConfiguration.getDevice(id);
        
        return m_deviceCache.get(id );
    }
    
    /**
     * Returns Cooker Name which probe with given ID is associated with.
     * @param id
     * @return
     */
    public String getCookerNameForDeviceID( String id )
    {
        if ( m_cookerList == null || m_cookerList.getCookerList().size() == 0 )
           return "";
        
        return m_CookerCache.get(id );
    }
    
    private void reconcile()
    {
        /* This method checks to see if the devices listed in the saved stoker-web
         * configuration exist on the stoker hardware.  If the device does not
         * exist, it is removed from the cooker.
         * 
         * 
         */
        logger.debug("reconcile");
        
        HashMap<String,SDevice> hmStoker = new HashMap<String,SDevice>(); 
        for ( SDevice sd : m_deviceConfiguration.getAllDevices() )
        {
            hmStoker.put( sd.getID().toUpperCase(),sd);
        }
       
    //    m_deviceConfiguration.getAllDevices();
       
        logger.debug("Looping over cookers");
       for ( Cooker cooker : m_cookerList.getCookerList() )
       {
           if ( logger.isDebugEnabled() && cooker.getCookerName() != null )
              logger.debug("Found cooker: " + cooker.getCookerName());
           
           StokerPitProbe cookerPitSensor = cooker.getPitSensor();
           
           if ( cookerPitSensor != null )
           {
              SDevice sdStoker = hmStoker.get( cookerPitSensor.getID().toUpperCase());
              if ( sdStoker != null )
              {
                 if ( sdStoker.getProbeType() == DeviceType.PIT )
                 {
                     if ( ! cookerPitSensor.equals((StokerPitProbe) sdStoker))
                     {
                         cooker.setPitSensor(null );
                     }
                 }
                  /*
                  switch (sdStoker.getProbeType())
                  {
                      case PIT:
                          StokerPitSensor sps = (StokerPitSensor)sdStoker;
                          StokerFan sf =  sps.getFanDevice();
                          if ( sf != null )
                          {
                              if ( sf.getID().compareTo(cookerPitSensor.getFanDevice().getID()) != 0)
                              {
                                  logger.warn("Fan device does not match StokerWeb and stoker configuration for pit sensor: " + cookerPitSensor.getName());
                                  
                              }
                          }
                          break;
                      case BLOWER:
                          
                      case FOOD:
                          
                      default:
                  }
                  */
                  
              }
              else
              {
                  logger.warn("StokerWeb cookerPitSensor not found on stoker device");
                  cooker.setPitSensor(null);
              }
           }
           
           // Remove probes from Cooker that do not exist on the hardware device.
           ArrayList<String> removeProbes = new ArrayList<String>();
           
           for ( SDevice cookerProbe : cooker.getProbeList() )
           {
               SDevice sdStoker = hmStoker.get( cookerProbe.getID().toUpperCase() );
               
               if ( sdStoker == null )
               {
                   logger.warn("device: [" + cookerProbe.getName() + "] with id [" + cookerProbe.getID().toUpperCase() + "] does not exist in stoker");
                   removeProbes.add( cookerProbe.getID());
               }
              // cooker.removeStokerProbe(cookerProbe.getID());  //. cant remove probes from list we are looping over
               
           }
           for ( String s : removeProbes )
           {
               cooker.removeStokerProbe(s);
           }
       }
       m_eventBus.post( new ConfigChangeEvent( this, EventType.CONFIG_LOADED) );
       loadDeviceCacheFromCookerList();
       
    }
    
    
    public String updateConfig( ArrayList<SDevice> arsd)
    {
        String strResponse = "";
     //   strResponse = validate( arsd );
    //    if ( strResponse.length() == 0 )
     //   {
           m_cookerList.update( arsd );
           save();
      //  }
      return strResponse;
       
    }
    
    private void save()
    {
        try 
        {  
            ObjectMapper mapper = new ObjectMapper();
            BufferedWriter out = new BufferedWriter(new FileWriter(strConfigFile));

            mapper.writeValue(out, m_cookerList );
            
            out.close();

            ArrayList<SDevice> arAllDevices = new ArrayList<SDevice>();
            for ( Cooker c : m_cookerList.getCookerList())
            {
               
               arAllDevices.addAll(CookerHelper.getDeviceList( c ));
            }
          //  removeAlarmsIfLocal( arAllDevices );
            m_deviceConfiguration.update(arAllDevices);
            m_eventBus.post( new ConfigChangeEvent( this, EventType.CONFIG_SAVED) );
            
        } 
        catch (IOException e) 
        {
            logger.error("Error writing " + strConfigFile + "\n" + e.getStackTrace() );
        }
        
        loadDeviceCacheFromCookerList();
    }
    
    private void removeAlarmsIfLocal( ArrayList<SDevice> deviceList )
    {
        String alarmLocation = StokerWebProperties.getInstance().getProperty(StokerWebConstants.PROPS_ALARM_SETTINGS_LOCATION);
        
        if ( alarmLocation == null )
            return;
        
        if ( alarmLocation.compareToIgnoreCase("local") == 0)
            for ( SDevice sd : deviceList )
                if ( sd.isProbe() )
                    ((StokerProbe)sd).setAlarmEnabled(AlarmType.NONE);

    }
    
    public void saveConfig(CookerList cookerList)
    {
        this.m_cookerList = cookerList;
        this.save();
    }
    
    public boolean loadConfig() 
    {
        ObjectMapper mapper = new ObjectMapper();
        boolean bReturn = false;
        
        try
        {
            
            BufferedReader in = new BufferedReader(new FileReader(strConfigFile));
            m_cookerList = mapper.readValue(in, CookerList.class);
            in.close();
            
            bReturn = true;
        }
        catch (FileNotFoundException e)
        {
            logger.error("Stokerweb config file [" + strConfigFile + "] was not found");
            e.printStackTrace();
        }
        catch (JsonParseException e)
        {
            logger.error("Error parsing CookerConfig.json in loadConfig");    
        }
        catch (JsonMappingException e)
        {
            logger.error("Error mapping CookerConfig.json in loadConfig\n" + e.getMessage());
            
        }
        catch (IOException e)
        {
            logger.error("IO Exception reading stokerweb config file [" + strConfigFile + "]");
            e.printStackTrace();
        }
        
        return bReturn;
    }
    
    public CookerList getCookerList()
    {
        return m_cookerList;
    }
  
}
