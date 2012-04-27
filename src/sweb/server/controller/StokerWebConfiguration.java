package sweb.server.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

import com.google.inject.Inject;


import sweb.server.CometMessenger;
import sweb.server.controller.events.CookerConfigChangeListener;
import sweb.server.controller.events.DataControllerEvent;
import sweb.server.controller.events.DataControllerEventListener;
import sweb.shared.model.Cooker;
import sweb.shared.model.CookerList;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.stoker.StokerDeviceTypes.DeviceType;
import sweb.shared.model.stoker.StokerFan;
import sweb.shared.model.stoker.StokerPitSensor;

public class StokerWebConfiguration
{
    private static final Logger logger = Logger.getLogger(StokerWebConfiguration.class.getName());
    
  //  private volatile static StokerWebConfiguration stokerWebConfiguration = null;
    
    private HardwareDeviceConfiguration deviceConfiguration = null;
    private CookerList cookerList;
    
    private ArrayList<CookerConfigChangeListener> arListener = new ArrayList<CookerConfigChangeListener>();
    
   /* public static StokerWebConfiguration getInstance()
    {
        if ( stokerWebConfiguration == null)
        {
            synchronized ( Controller.class)
            {
                if ( stokerWebConfiguration == null )
                {
                    stokerWebConfiguration = new StokerWebConfiguration();
                }
            }
        }
        return stokerWebConfiguration;
    }
    */
    
    @Inject
    public StokerWebConfiguration(HardwareDeviceConfiguration stokerConfig) 
    { 
        this.deviceConfiguration = stokerConfig;

    }
    
    public void init()
    {
        
        loadConfig(); 

        if ( cookerList == null )
            cookerList = new CookerList();
        
        reconcile( deviceConfiguration );
        
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
       for ( Cooker cooker : cookerList.getCookerList() )
       {
           if ( logger.isDebugEnabled() && cooker.getCookerName() != null )
              logger.debug("Found cooker: " + cooker.getCookerName());
           
           StokerPitSensor cookerPitSensor = cooker.getStokerPitSensor();
           
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
           
           for ( SDevice cookerProbe : cooker.getStokerProbeList() )
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
            BufferedWriter out = new BufferedWriter(new FileWriter("CookerConfig.json"));
            mapper.writeValue(out, cookerList);
            
            out.close();
            this.cookerList = cookerList;
        } 
        catch (IOException e) 
        {
            logger.error("Error writing CookerConig.json:\n" + e.getStackTrace() );
        }
    }
    
    public void loadConfig()
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            BufferedReader in = new BufferedReader(new FileReader("CookerConfig.json"));
            cookerList = mapper.readValue(in, CookerList.class);
            in.close();
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
            logger.error("Error mapping CookerConfig.json in loadConfig");
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public CookerList getCookerList()
    {
        return cookerList;
    }
    
    
    public void addChangeListener( CookerConfigChangeListener listener )
    {
        synchronized( this )
        {
           arListener.add( listener );
        }
    }

    public void removeChangeListener( CookerConfigChangeListener listener )
    {
        synchronized( this )
        {
            arListener.remove(listener);

        }
    }

    protected void fireActionPerformed()
    {
        synchronized( this )
        {
            for ( CookerConfigChangeListener listener : arListener )
            {
                listener.actionPerformed();
            }
        }
    }
}
