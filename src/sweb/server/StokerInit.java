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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import sweb.server.controller.events.ConfigChangeEvent;
import sweb.server.controller.events.ConfigChangeEventListener;
import sweb.server.controller.events.CookerConfigChangeListener;
import sweb.server.controller.events.StateChangeEvent;
import sweb.server.controller.events.StateChangeEventListener;
import sweb.server.log.LogManager;
import sweb.server.security.LoginProperties;

public class StokerInit extends HttpServlet
{

    private static final long serialVersionUID = 4958759438289484633L;
  //  private Controller m_Controller = null;
  //  private StokerWebConfiguration m_CookerConfig = null;
    private LogManager m_logManager;
    
    private static final Logger logger = Logger.getLogger(LoginProperties.class.getName());


    @Inject
    public StokerInit(EventBus eventBus,
                      LogManager logManager )
    {
        eventBus.register(this);
        this.m_logManager = logManager;
        
        logger.debug("StokerInit()");
    }
    
    @Subscribe
    public void handleConfigChangeEvent( ConfigChangeEvent ce )
    {
        // TODO: Reset All
    }
    
    
    @Subscribe
    public void handleStateChangeEvent( StateChangeEvent ce )
    {
        if ( ce.getEventType() == StateChangeEvent.EventType.EXTENDED_CONNECTION_LOSS )
        {
            m_logManager.stopAllLogs();
        }
   
    }

}
