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

import sweb.server.StokerWebProperties;
import sweb.server.controller.config.ConfigurationController;
import sweb.server.controller.config.stoker.StokerWebConfigurationController;
import sweb.server.controller.data.DataController;
import sweb.server.controller.data.DataOrchestrator;
import sweb.server.controller.data.telnet.StokerTelnetCommands;
import sweb.server.controller.data.telnet.StokerTelnetController;
import sweb.server.controller.events.ConfigControllerEvent;
import sweb.server.controller.events.ConfigControllerEventListener;
import sweb.server.controller.events.DataControllerEvent;
import sweb.server.controller.events.DataControllerEventListener;
import sweb.server.controller.events.DataControllerEvent.EventType;
import sweb.server.controller.events.WeatherChangeEventListener;
import sweb.server.controller.log.exceptions.LogExistsException;
import sweb.server.controller.log.exceptions.LogNotFoundException;
//import sweb.server.controller.notify.NotificationController;
import sweb.server.controller.weather.WeatherController;

/**
 * @author gary.bak
 *
 */
public class Controller
{
    private volatile static Controller m_Controller = null;
    private DataController m_DataController = null;
    private ConfigurationController m_ConfigurationController = null;
    private WeatherController m_WeatherController = null;
   // private NotificationController m_NotificationController = null;

 //   private ControllerEventListener m_ControllerListener = null;


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
        m_DataController= new StokerTelnetController();
        m_ConfigurationController = new StokerWebConfigurationController();
        m_WeatherController = new WeatherController();
    }

    public void init()
    {
       m_DataController.setDataStore(DataOrchestrator.getInstance());

       // TODO: implement start() and call it instead of now()
       m_ConfigurationController.setConfiguration(StokerConfiguration.getInstance());
      // m_ConfigurationController.setNow();

      // setupDefaultLog();

       m_DataController.addEventListener(new DataControllerEventListener()
       {

            public void actionPerformed(DataControllerEvent ce)
            {
               if ( ce.getEventType() == EventType.CONNECTION_ESTABLISHED)
               {
                   loadConfiguration();
                   setupDefaultLog();
               }

            }

       });


       m_DataController.start();
       m_WeatherController.start();
    }

    private void setupDefaultLog()
    {
        for( String s : StokerConfiguration.getInstance().getAllBlowerIDs() )
        {
            String strCookerName = StokerWebProperties.getInstance().getProperty( s );

            if ( strCookerName == null )  strCookerName = "";

            boolean bTryAgain = true;
            while ( bTryAgain )
            {
                if ( ! DataOrchestrator.getInstance().isLogRunning("Default"))
                {
                    try
                    {
                        DataOrchestrator.getInstance().startLog(strCookerName, "Default");
                        bTryAgain = false;
                    }
                    catch (LogExistsException e)
                    {
                        try { DataOrchestrator.getInstance().stopLog("Default"); } catch (LogNotFoundException e1) { }
                    }
                }

            }  // end while try again

        } // end for String
        if (StokerConfiguration.getInstance().getAllBlowerIDs().size() == 0 )
        {
            System.out.println("No Blowers configured!");  // TODO: log this message
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
        m_ConfigurationController.setConfiguration(StokerConfiguration.getInstance());
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
}
