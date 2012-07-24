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

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import sweb.server.StokerWebConstants;
import sweb.server.StokerWebProperties;
import sweb.server.controller.events.ConfigChangeEvent;
import sweb.server.controller.events.ConfigChangeEvent.EventType;

import sweb.shared.model.Cooker;
import sweb.shared.model.CookerHelper;
import sweb.shared.model.CookerList;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.stoker.StokerDeviceTypes.DeviceType;
import sweb.shared.model.stoker.StokerPitSensor;

public class StokerWebConfiguration
{
    private static final Logger logger = Logger.getLogger(StokerWebConfiguration.class.getName());

    private static final String strConfigFile = StokerWebProperties.getInstance().getProperty(StokerWebConstants.PROPS_STOKERWEB_DIR) + 
            File.separator + "CookerConfig.json";

    
    private HardwareDeviceConfiguration m_deviceConfiguration = null;
    private CookerList m_cookerList;
    private EventBus m_eventBus;
    
    @Inject
    public StokerWebConfiguration(HardwareDeviceConfiguration stokerConfig,
                                  EventBus eventBus ) 
    { 
        this.m_deviceConfiguration = stokerConfig;
        this.m_eventBus = eventBus;
       // init();
    }
    
    public void init()
    {
        
        loadConfig(); 

        if ( m_cookerList == null )
            m_cookerList = new CookerList();
        
        reconcile( m_deviceConfiguration );
        
    }
    
    private void reconcile( HardwareDeviceConfiguration stokerConfig )
    {
        /* This method checks to see if the devices listed in the saved stoker-web
         * configuration exist on the stoker hardware.  If the device does not
         * exist, it is removed from the cooker.
         * 
         * 
         */
        logger.debug("reconcile");
        
        HashMap<String,SDevice> hmStoker = new HashMap<String,SDevice>(); 
        for ( SDevice sd : stokerConfig.getAllDevices() )
        {
            hmStoker.put( sd.getID(),sd);
        }
       
        stokerConfig.getAllDevices();
       
        logger.debug("Looping over cookers");
       for ( Cooker cooker : m_cookerList.getCookerList() )
       {
           if ( logger.isDebugEnabled() && cooker.getCookerName() != null )
              logger.debug("Found cooker: " + cooker.getCookerName());
           
           StokerPitSensor cookerPitSensor = cooker.getPitSensor();
           
           if ( cookerPitSensor != null )
           {
              SDevice sdStoker = hmStoker.get( cookerPitSensor.getID());
              if ( sdStoker != null )
              {
                 if ( sdStoker.getProbeType() == DeviceType.PIT )
                 {
                     if ( ! cookerPitSensor.equals((StokerPitSensor) sdStoker))
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
           
           for ( SDevice cookerProbe : cooker.getProbeList() )
           {
               SDevice sdStoker = hmStoker.get( cookerProbe.getID() );
               
               if ( sdStoker == null )
               {
                   logger.warn("device: [" + cookerProbe.getName() + "] with id [" + cookerProbe.getID() + "] does not exist in stoker");
               }
               cooker.removeStokerProbe(cookerProbe.getID());
           }
       }
    }
    
    public void saveConfig(CookerList cookerList)
    {
        try 
        {  
            ObjectMapper mapper = new ObjectMapper();
            BufferedWriter out = new BufferedWriter(new FileWriter(strConfigFile));

            mapper.writeValue(out, cookerList);
            
            out.close();
            this.m_cookerList = cookerList;
            ArrayList<SDevice> arAllDevices = new ArrayList<SDevice>();
            for ( Cooker c : m_cookerList.getCookerList())
            {
               
               arAllDevices.addAll(CookerHelper.getDeviceList( c ));
            }
            m_deviceConfiguration.update(arAllDevices);
            m_eventBus.post( new ConfigChangeEvent( this, EventType.CONFIG_SAVED) );
            
        } 
        catch (IOException e) 
        {
            logger.error("Error writing " + strConfigFile + "\n" + e.getStackTrace() );
        }
    }
    
    public void loadConfig()
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            
            BufferedReader in = new BufferedReader(new FileReader(strConfigFile));
            m_cookerList = mapper.readValue(in, CookerList.class);
            in.close();
            m_eventBus.post( new ConfigChangeEvent( this, EventType.CONFIG_LOADED));
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public CookerList getCookerList()
    {
        return m_cookerList;
    }
  
}
