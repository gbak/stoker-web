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

import sweb.server.ClientMessenger;
import sweb.server.CometMessenger;
import sweb.server.StokerWebProperties;
import sweb.server.controller.alerts.AlertsController;
import sweb.server.controller.config.ConfigurationController;
import sweb.server.controller.config.stoker.StokerConfigurationController;
import sweb.server.controller.data.DataController;
import sweb.server.controller.data.DataOrchestrator;
import sweb.server.controller.data.telnet.StokerTelnetController;
import sweb.server.controller.events.ConfigControllerEventListener;
import sweb.server.controller.events.DataControllerEvent;
import sweb.server.controller.events.DataControllerEvent.EventType;
import sweb.server.controller.events.DataControllerEventListener;
import sweb.server.controller.events.WeatherChangeEventListener;
import sweb.server.controller.log.exceptions.LogExistsException;
import sweb.server.controller.log.exceptions.LogNotFoundException;
import sweb.server.controller.weather.WeatherController;
import sweb.shared.model.alerts.AlertModel;

/**
 * @author gary.bak
 *
 */
public class Controller
{
    private volatile static Controller m_Controller = null;
    private DataController m_DataController = null;
    private DataOrchestrator m_DataOrchestrator = null;
    private ConfigurationController m_ConfigurationController = null;
    private WeatherController m_WeatherController = null;
    private AlertsController m_AlertsController = null;
    private ClientMessenger m_ClientMessenger = null;
    private StokerConfiguration m_StokerConfiguration = null;
    private StokerWebConfiguration m_StokerWebConfiguration = null;

    private static final Logger logger = Logger.getLogger(Controller.class.getName());
    
 //   private ControllerEventListener m_ControllerListener = null;


    public static boolean isNull()
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
    }

    private Controller()
    {
        synchronized ( Controller.class)
        {
            m_DataController= new StokerTelnetController();
            m_ConfigurationController = new StokerConfigurationController();
            m_WeatherController = new WeatherController();
            m_DataOrchestrator = new DataOrchestrator();
            m_ClientMessenger = new CometMessenger();
            m_StokerConfiguration = new StokerConfiguration();
            m_StokerWebConfiguration = new StokerWebConfiguration(m_StokerConfiguration);
        }
    }

    public void init()
    {
        logger.info("Controller init called");
       m_AlertsController = new AlertsController();
       m_DataController.setDataStore(m_DataOrchestrator);
       m_ConfigurationController.setConfiguration(m_StokerConfiguration);

       m_DataController.addEventListener(new DataControllerEventListener()
       {

            public void actionPerformed(DataControllerEvent ce)
            {
               if ( ce.getEventType() == EventType.CONNECTION_ESTABLISHED)
               {
                   logger.info("Loading Stoker Configuration");
                   loadConfiguration();
                   
                   logger.info("Setting default log");
                   setupDefaultLog();
               }

            }

       });


       m_DataController.start();
       m_WeatherController.start();
    }

    public void resetAll()
    {
        synchronized ( Controller.class)
        {
            // this gets called when the stoker configuration has changed from the client side.
            // A full flush needs to be done on the server and restart\
            
            m_DataController= new StokerTelnetController();
            m_StokerConfiguration = new StokerConfiguration();
            m_ConfigurationController = new StokerConfigurationController();
            m_ConfigurationController.setConfiguration( m_StokerConfiguration );
            m_WeatherController = new WeatherController();
            m_AlertsController = null;
            m_ClientMessenger = new CometMessenger();
    
            init();
        }
        
    }
    
    public DataOrchestrator getDataOrchestrator()
    {
        return m_DataOrchestrator;
    }
    
    public ClientMessenger getClientMessenger()
    {
       return m_ClientMessenger;    
    }
    public StokerConfiguration getStokerConfiguration()
    {
        return m_StokerConfiguration;
    }
    
    private void setupDefaultLog()
    {
        for( String s : m_StokerConfiguration.getAllBlowerIDs() )
        {
            String strCookerName = StokerWebProperties.getInstance().getProperty( s );

            if ( strCookerName == null )  strCookerName = "";

            boolean bTryAgain = true;
        //    while ( bTryAgain )
        //    {
            String strDefaultName = "Default_" + strCookerName;
                if ( ! m_DataOrchestrator.isLogRunning(strDefaultName ))
                {
                    try
                    {
                        m_DataOrchestrator.startLog(strCookerName, strDefaultName);
                        bTryAgain = false;
                    }
                    catch (LogExistsException e)
                    {
                        try { m_DataOrchestrator.stopLog(strDefaultName); } catch (LogNotFoundException e1) { }
                    }
                }

         //   }  // end while try again

        } // end for String
        if (m_StokerConfiguration.getAllBlowerIDs().size() == 0 )
        {
            logger.error("No Blowers configured!");
        }
    }
    /**
     *  Request the StokerConfigurationController to retrieve the configuration data.
     *
     *   This in turn should fire the ConfigControllerEvent.EventType.CONFIG_UPDATE event.
     */
    public void loadConfiguration()
    {
        m_ConfigurationController.setNow();
    }

    public void updateConfiguration()
    {
        m_ConfigurationController.setConfiguration(m_StokerConfiguration);
    }

    public void addDataEventListener( DataControllerEventListener cl)
    {
        m_DataController.addEventListener(cl);
    }

    public void removeDataEventListener( DataControllerEventListener cl)
    {
        m_DataController.removeEventListener(cl);
    }

    public void addConfigEventListener( ConfigControllerEventListener configControllerEventListener)
    {
        m_ConfigurationController.addEventListener(configControllerEventListener);
    }

    public void removeConfigEventListener( ConfigControllerEventListener configControllerEventListener)
    {
        m_ConfigurationController.removeEventListener(configControllerEventListener);
    }

    public boolean isDataControllerReady()
    {
       return m_DataController.isReady();
    }

   public void addWeatherChangeEventListener( WeatherChangeEventListener wcel )
   {
       m_WeatherController.addEventListener(wcel );
   }

   public void removeWeatherChangeEventListener( WeatherChangeEventListener wcel )
   {
       m_WeatherController.removeEventListener(wcel );
   }

   public WeatherController getWeatherController()
   {
       return m_WeatherController;
   }

   public DataController getDataController()
   {
       return m_DataController;
   }
   
   public ArrayList<AlertModel> getAlertConfiguration()
   {
      return m_AlertsController.getConfiguration();
      
   }
   
   public void setAlertConfiguration( ArrayList<AlertModel> alertBaseList )
   {
      m_AlertsController.setConfiguration(alertBaseList);
   }
   
   public Set<String> getAvailableDeliveryMethods()
   {
       return m_AlertsController.getAvailableDeliveryMethods();
   }
}
