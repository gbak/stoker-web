/**
 *  Stoker-web
 *
 *  Copyright (C) 2011  Gary Bak
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

package sweb.server.controller;

import java.util.ArrayList;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import sweb.server.ClientMessenger;
import sweb.server.CometMessenger;
import sweb.server.StokerWebProperties;
import sweb.server.controller.alerts.AlertManager;
import sweb.server.controller.alerts.AlertsManagerImpl;
import sweb.server.controller.config.ConfigurationController;
import sweb.server.controller.config.stoker.StokerHardwareDevice;
import sweb.server.controller.data.DataController;
import sweb.server.controller.data.telnet.StokerTelnetController;
import sweb.server.controller.events.BlowerEvent;
import sweb.server.controller.events.BlowerEventListener;
import sweb.server.controller.events.ConfigChangeEvent;
import sweb.server.controller.events.ConfigChangeEventListener;
import sweb.server.controller.events.DataPointEvent;
import sweb.server.controller.events.DataPointEventListener;
import sweb.server.controller.events.StateChangeEvent;
import sweb.server.controller.events.StateChangeEvent.EventType;
import sweb.server.controller.events.StateChangeEventListener;
import sweb.server.controller.events.WeatherChangeEventListener;
import sweb.server.controller.log.exceptions.LogExistsException;
import sweb.server.controller.log.exceptions.LogNotFoundException;
import sweb.server.controller.weather.WeatherController;
import sweb.server.log.LogManager;
import sweb.server.monitors.PitMonitor;
import sweb.shared.model.Cooker;
import sweb.shared.model.CookerList;
import sweb.shared.model.HardwareDeviceStatus;
import sweb.shared.model.LogItem;
import sweb.shared.model.alerts.AlertModel;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.logfile.LogNote;

/**
 * @author gary.bak
 *
 */
public class Controller implements PitMonitor, LogManager, AlertManager
{
    private PitMonitor m_PitMonitor = null;
    private LogManager m_LogManager = null;
    
  //  private ConfigurationController m_ConfigurationController = null;
 //   private WeatherController m_WeatherController = null;
    private AlertsManagerImpl m_AlertsController = null;
  //  private ClientMessenger m_ClientMessenger = null;

    private static final Logger logger = Logger.getLogger(Controller.class.getName());
    
 //   private ControllerEventListener m_ControllerListener = null;


 /*   public static boolean isNull()
    {
        return m_Controller == null;
    }
    
    public static Controller getInstance()
    {
        if ( m_Controller == null)
        {
            synchronized ( Controller.class)
            {
                if ( m_Controller == null )
                {
                    m_Controller = new Controller();
                }
            }
        }
        return m_Controller;
    }*/

    @Inject
    private Controller(PitMonitor pitMonitor,
                       LogManager logManager,
                       AlertsManagerImpl alerts )
    {
        m_PitMonitor = pitMonitor;
        m_LogManager = logManager;
        m_AlertsController = alerts;
        
        init();
        // TODO:  this will not work correctly.  Reset all need to clear out
        // these objects so they can be created again.  
      
    }
    
    private void setupListeners()
    {
        addStateChangeListener( new StateChangeEventListener() {

            public void actionPerformed(StateChangeEvent ce)
            {
               if ( ce.getEventType() == StateChangeEvent.EventType.EXTENDED_CONNECTION_LOSS )
               {
                   stopAllLogs();
               }

            }

        });    
    }
    
    public void init()
    {
        logger.info("Controller init called");
    
        if ( ! m_PitMonitor.isConfigRequired() )
        { 
            setupListeners();
            setupDefaultLog();
        }
          

   /*    m_DataController.addEventListener(new StateChangeEventListener()
       {

            public void actionPerformed(StateChangeEvent ce)
            {
               if ( ce.getEventType() == EventType.CONNECTION_ESTABLISHED)
               {
                   logger.info("Loading Stoker Configuration");
                   loadConfiguration();
                   m_StokerWebConfiguration.init();
                   
                   logger.info("Setting default log");
                   setupDefaultLog();
               }

            }

       });*/


    //   m_WeatherController.start();
    }


    public void resetAll()
    {
        // TODO: this is so incomplete!
        
        synchronized ( Controller.class)
        {

            
       //     m_HardwareConfiguration = new HardwareDeviceConfiguration();
        //    m_ConfigurationController = new StokerConfigurationController();
       //     m_ConfigurationController.setConfiguration( m_StokerConfiguration );
         //   m_WeatherController = new WeatherController();
            m_AlertsController = null;
        //    m_ClientMessenger = new CometMessenger();
    
            init();
        }
        
    }
    

    public CookerList getStokerConfiguration()
    {
         return m_PitMonitor.getCookers();
    }
    
    private void setupDefaultLog()
    {
       // for( String s : m_HardwareConfiguration.getAllBlowerIDs() )
        for ( Cooker cooker : m_PitMonitor.getCookers().getCookerList())
        {
            //String strCookerName = StokerWebProperties.getInstance().getProperty( s );
            String strCookerName = cooker.getCookerName();
            
            if ( strCookerName == null )  strCookerName = "";

            boolean bTryAgain = true;
        //    while ( bTryAgain )
        //    {
            String strDefaultName = "Default_" + strCookerName;
                if ( ! isLogRunning(strDefaultName ))
                {
                    try
                    {
                        startLog(strCookerName, strDefaultName);
                        bTryAgain = false;
                    }
                    catch (LogExistsException e)
                    {
                        try { stopLog(strDefaultName); } catch (LogNotFoundException e1) { }
                    }
                }

         //   }  // end while try again

        } // end for String

    }
    /**
     *  Request the StokerConfigurationController to retrieve the configuration data.
     *
     *   This in turn should fire the ConfigControllerEvent.EventType.CONFIG_UPDATE event.
     */
/*    public void loadConfiguration()
    {
        m_HardwareConfiguration.loadNow();
    }*/
    
    @Override
    public void updateSettings(ArrayList<SDevice> sdc)
    {
        //TODO: write this
    }


    public void addConfigEventListener( ConfigChangeEventListener configControllerEventListener)
    {
        m_PitMonitor.addConfigChangeListener(configControllerEventListener);
       // m_ConfigurationController.addEventListener(configControllerEventListener);
    }

    /*public void removeConfigEventListener( ConfigControllerEventListener configControllerEventListener)
    {
        m_ConfigurationController.removeEventListener(configControllerEventListener);
    }



 /*  public void addWeatherChangeEventListener( WeatherChangeEventListener wcel )
   {
       m_WeatherController.addEventListener(wcel );
   }

   public void removeWeatherChangeEventListener( WeatherChangeEventListener wcel )
   {
       m_WeatherController.removeEventListener(wcel );
   }*/

   /*public WeatherController getWeatherController()
   {
       return m_WeatherController;
   }*/


    @Override
   public ArrayList<AlertModel> getAlertConfiguration()
   {
      return m_AlertsController.getConfiguration();
      
   }
   
   @Override
   public void setAlertConfiguration( ArrayList<AlertModel> alertBaseList )
   {
      m_AlertsController.setConfiguration(alertBaseList);
   }
   
   public Set<String> getAvailableDeliveryMethods()
   {
       return m_AlertsController.getAvailableDeliveryMethods();
   }

    @Override
    public HardwareDeviceStatus getState()
    {
        return m_PitMonitor.getState();
    }
    
    @Override
    public boolean isActive()
    {
        return m_PitMonitor.isActive();
    }
    
    @Override
    public boolean isConfigRequired()
    {
        return m_PitMonitor.isConfigRequired(); 
    }
    
    @Override
    public ArrayList<SDevice> getRawDevices()
    {
        return m_PitMonitor.getRawDevices();
    }
    
    @Override
    public CookerList getCookers()
    {
        return m_PitMonitor.getCookers();
    }
    
    @Override
    public void updateCooker(CookerList cookerList)
    {
        m_PitMonitor.updateCooker( cookerList );
    }
    
    @Override
    public ArrayList<SDataPoint> getCurrentTemps()
    {
        return m_PitMonitor.getCurrentTemps();
    }
    
    @Override
    public void addTempListener(DataPointEventListener dataListener)
    {
        m_PitMonitor.addTempListener( dataListener );
    }
    
    @Override
    public void removeTempListener(DataPointEventListener dataListener)
    {
        m_PitMonitor.removeTempListener( dataListener );
    }
    
    @Override
    public void fireTempEvent(DataPointEvent dataEvent)
    {
        m_PitMonitor.fireTempEvent( dataEvent );
    }
    
    @Override
    public void addConfigChangeListener(ConfigChangeEventListener configListener)
    {
        m_PitMonitor.addConfigChangeListener( configListener );
        
    }
    
    @Override
    public void removeConfigChangeListener(ConfigChangeEventListener configListener)
    {
        m_PitMonitor.removeConfigChangeListener( configListener );
    }
    
    @Override
    public void fireConfigEvent(ConfigChangeEvent configEvent)
    {
        m_PitMonitor.fireConfigEvent( configEvent );
    }
    
    @Override
    public void addStateChangeListener(StateChangeEventListener stateChangeListener)
    {
        m_PitMonitor.addStateChangeListener( stateChangeListener );
    }
    
    @Override
    public void removeStateChangeListener(
            StateChangeEventListener stateChangeListener)
    {
        m_PitMonitor.removeStateChangeListener(stateChangeListener);
    }
    
    @Override
    public void fireChangeEvent(StateChangeEvent changeEvent)
    {
        m_PitMonitor.fireChangeEvent(changeEvent);
    }
    
    @Override
    public void addBlowerChangeListener(BlowerEventListener blowerListener)
    {
        m_PitMonitor.addBlowerChangeListener(blowerListener);
    }
    
    @Override
    public void removeBlowerChangeListener(BlowerEventListener blowerListener)
    {
        m_PitMonitor.removeBlowerChangeListener(blowerListener);
    }
    
    @Override
    public void fireBlowerEvent(BlowerEvent blowerEvent)
    {
        m_PitMonitor.fireBlowerEvent(blowerEvent);
    }

    // LogManager Classes
    
    @Override
    public ArrayList<ArrayList<SDataPoint>> getAllDataPoints(String logName)
    {
        return m_LogManager.getAllDataPoints(logName);
    }

    @Override
    public ArrayList<SDevice> getConfigSettings(String logName)
    {
       return m_LogManager.getConfigSettings(logName);
    }

    @Override
    public ArrayList<LogItem> getLogList()
    {
       return m_LogManager.getLogList();
    }

    @Override
    public String getLogFilePath(String strLogName)
    {
        return m_LogManager.getLogFilePath(strLogName);
    }

    @Override
    public String getLogFileName(String strLogName)
    {
        return m_LogManager.getLogFileName(strLogName);
    }

    @Override
    public boolean isLogRunning(String strLogName)
    {
       return m_LogManager.isLogRunning(strLogName);
    }

    @Override
    public void startLog(String strCookerName, String strLogName)
            throws LogExistsException
    {
       m_LogManager.startLog(strCookerName, strLogName);
    }

    @Override
    public void startLog(LogItem logItem) throws LogExistsException
    {
        m_LogManager.startLog(logItem);
    }

    @Override
    public void stopLog(String strLogName) throws LogNotFoundException
    {
       m_LogManager.stopLog(strLogName);
    }

    @Override
    public void stopAllLogs()
    {
        m_LogManager.stopAllLogs();
    }

    @Override
    public Integer attachToExistingLog(String cookerName, String selectedLog,
            String fileName)
    {
        return m_LogManager.attachToExistingLog(cookerName, selectedLog, fileName);
    }

    @Override
    public ArrayList<LogNote> getNotes(String logName)
    {
        return m_LogManager.getNotes(logName);
    }

    @Override
    public void addNoteToLog(String note, ArrayList<String> logList)
    {
       m_LogManager.addNoteToLog(note, logList);
    }

    @Override
    public SDevice getDeviceByID(String ID)
    {
        return m_PitMonitor.getDeviceByID(ID);
    }


}
