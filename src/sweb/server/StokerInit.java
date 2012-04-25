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

package sweb.server;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import sweb.server.controller.Controller;
import sweb.server.controller.StokerWebConfiguration;
import sweb.server.controller.data.DataOrchestrator;
import sweb.server.controller.events.CookerConfigChangeListener;
import sweb.server.controller.events.DataControllerEvent;
import sweb.server.controller.events.DataControllerEventListener;
import sweb.server.security.LoginProperties;

public class StokerInit extends HttpServlet
{

    private static final long serialVersionUID = 4958759438289484633L;
  //  private Controller m_Controller = null;
    private StokerWebConfiguration m_CookerConfig = null;
    private static final Logger logger = Logger.getLogger(LoginProperties.class.getName());

    @Inject
    public StokerInit(StokerWebConfiguration stokerWebConfiguration)
    {
        if ( m_CookerConfig == null )
        {
            m_CookerConfig = stokerWebConfiguration;
            m_CookerConfig.addChangeListener( new CookerConfigChangeListener() {

                @Override
                public void actionPerformed()
                {
                    Controller.getInstance().resetAll();
                    
                }
                
            });
        }
        // m_Controller is a singleton and the variable is not required, but instead used to
        // know if it has been initialized already, this is called on browser refresh
        // and we only want this to be executed once and only once
        if ( Controller.isNull() )
        {         

            Controller.getInstance().addDataEventListener( new DataControllerEventListener() {

                public void actionPerformed(DataControllerEvent ce)
                {
                   if ( ce.getEventType() == DataControllerEvent.EventType.EXTENDED_CONNECTION_LOSS )
                   {
                       Controller.getInstance().getDataOrchestrator().stopAllLogs();
                   }

                }

            });

            // This will initialize the Data and the configuration controller
            // Once the config listener completes it will fire the event
            // and the default log file will start recording.

            Controller.getInstance().init();

        }
    }
}
