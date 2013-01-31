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

package com.gbak.sweb.server;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;





import com.gbak.sweb.client.StokerCoreService;
import com.gbak.sweb.server.alerts.AlertsManagerImpl;
import com.gbak.sweb.server.config.StokerWebConfiguration;
import com.gbak.sweb.server.events.ConfigChangeEvent;
import com.gbak.sweb.server.events.DataPointEvent;
import com.gbak.sweb.server.events.StateChangeEvent;
import com.gbak.sweb.server.events.WeatherChangeEvent;
import com.gbak.sweb.server.events.StateChangeEvent.EventType;
import com.gbak.sweb.server.log.LogManager;
import com.gbak.sweb.server.log.exceptions.LogExistsException;
import com.gbak.sweb.server.log.exceptions.LogNotFoundException;
import com.gbak.sweb.server.log.file.ListLogFiles;
import com.gbak.sweb.server.monitors.PitMonitor;
import com.gbak.sweb.server.security.LoginProperties;
import com.gbak.sweb.server.security.User;
import com.gbak.sweb.server.weather.WeatherController;
import com.gbak.sweb.shared.model.CallBackRequestType;
import com.gbak.sweb.shared.model.ConfigurationSettings;
import com.gbak.sweb.shared.model.CookerList;
import com.gbak.sweb.shared.model.HardwareDeviceState;
import com.gbak.sweb.shared.model.LogItem;
import com.gbak.sweb.shared.model.HardwareDeviceState.Status;
import com.gbak.sweb.shared.model.alerts.AlertModel;
import com.gbak.sweb.shared.model.alerts.BrowserAlarmModel;
import com.gbak.sweb.shared.model.data.SBlowerDataPoint;
import com.gbak.sweb.shared.model.data.SDataPoint;
import com.gbak.sweb.shared.model.data.SProbeDataPoint;
import com.gbak.sweb.shared.model.devices.SDevice;
import com.gbak.sweb.shared.model.events.ControllerEventLight;
import com.gbak.sweb.shared.model.events.LogEvent;
import com.gbak.sweb.shared.model.events.ControllerEventLight.EventTypeLight;
import com.gbak.sweb.shared.model.events.LogEvent.LogEventType;
import com.gbak.sweb.shared.model.logfile.LogDir;
import com.gbak.sweb.shared.model.weather.WeatherData;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class StokerCoreServiceImpl extends RemoteServiceServlet implements StokerCoreService, 
                                                                           HttpSessionListener 
{

    EventBus m_eventBus = null;
    PitMonitor m_pitMonitor = null;
    ClientMessenger m_ClientMessenger = null;
    WeatherController m_WeatherController = null;
    AlertsManagerImpl m_alertsManager = null;
    StokerSharedServices m_stokerSharedServices = null;
    
    EventTypeLight m_lastEvent = EventTypeLight.NONE;
    
    private static final Logger logger = Logger.getLogger(StokerCoreServiceImpl.class.getName());
    
    @Inject
    public StokerCoreServiceImpl( PitMonitor pm,
                                  ClientMessenger cm,
                                  AlertsManagerImpl am,
                                  WeatherController wc,
                                  StokerSharedServices ssc,
                                  EventBus bus)
    {

        this.m_pitMonitor = pm;
        this.m_ClientMessenger = cm;
        this.m_WeatherController = wc;
        this.m_alertsManager = am;
        this.m_eventBus = bus;
        this.m_stokerSharedServices = ssc;
        
        m_eventBus.register(this);
        
    }
    
    public ConfigurationSettings getDeviceConfiguration()
            throws IllegalArgumentException
    {
        return m_stokerSharedServices.getDeviceConfiguration();
    }

    public void setupCallBack()
    {
       HttpSession httpSession = getThreadLocalRequest().getSession();
      // CustomSession httpSession = (CustomSession)getThreadLocalRequest().getSession();

       m_ClientMessenger.addSession( httpSession );
    }

    public ArrayList<SDataPoint> getNewGraphDataPoints(String input) throws IllegalArgumentException
    {
        return m_pitMonitor.getCurrentTemps();
    }

    @Subscribe
    public void handleDataPointEvents( DataPointEvent dpe )
    {
        ArrayList<SProbeDataPoint> aldp = dpe.getSProbeDataPoints();
        if ( aldp != null)
        {
            for ( SProbeDataPoint sdp : aldp)
            {
                m_ClientMessenger.push(sdp);
            }
        }

        SBlowerDataPoint bdp = dpe.getSBlowerDataPoint();
        if ( bdp != null  )
        {
            m_ClientMessenger.push(bdp);
        }
    }
    
    @Subscribe
    public void handleStateChangeEvents( StateChangeEvent ce)
    {
        switch (ce.getEventType())
        {
            case CONNECTION_ESTABLISHED:
                m_ClientMessenger
                        .push(new ControllerEventLight(
                                EventTypeLight.CONNECTION_ESTABLISHED));
                m_lastEvent = EventTypeLight.CONNECTION_ESTABLISHED;
                break;
            case NONE:
                m_lastEvent = EventTypeLight.NONE;
                break;
            case LOST_CONNECTION:
                m_ClientMessenger.push(
                        new ControllerEventLight(
                                EventTypeLight.LOST_CONNECTION));
                m_lastEvent = EventTypeLight.LOST_CONNECTION;
                break;
            case EXTENDED_CONNECTION_LOSS:
                m_ClientMessenger.push(
                        new ControllerEventLight(
                                EventTypeLight.EXTENDED_CONNECTION_LOSS));
                m_lastEvent = EventTypeLight.EXTENDED_CONNECTION_LOSS;
            default:

        }
 
    }
    
    @Subscribe
    public void handleConfigChangeEvents( ConfigChangeEvent ce )
    {
        switch (ce.getEventType())
        {
            case NONE:
                break;
            case CONFIG_UPDATE_DETECTED:
                m_ClientMessenger.push(
                        new ControllerEventLight(
                                EventTypeLight.CONFIG_UPDATE));
                break;
            default:
        }
    }
    
    @Subscribe
    public void handleWeatherChangeEvents( WeatherChangeEvent wce )
    {
        WeatherData wd = wce.getWeatherData();
        if ( wd != null )
            m_ClientMessenger.push(wd);
    }
    
    @Subscribe
    public void handleAlertEvents( BrowserAlarmModel bam )
    {
        logger.debug("Pushing BrowserAlarmModel: " + bam.getMessage() );
        m_ClientMessenger.push( bam );
    }

    public void setAlertConfiguration( ArrayList<AlertModel> alertBaseList )
    {
       m_alertsManager.setConfiguration(alertBaseList);
       
    }
    
    public ArrayList<AlertModel> getAlertConfiguration()
    {
       ArrayList<AlertModel> ab = m_alertsManager.getConfiguration();
       return ab;
    }
    
    /**
     * Escape an html string. Escaping data received from the client helps to
     * prevent cross-site script vulnerabilities.
     *
     * @param html the html string to escape
     * @return the escaped string
     */
    private String escapeHtml(String html)
    {
        if (html == null)
        {
            return null;
        }
        return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }

    private boolean loginGuard()
    {  
       User u = getUserAlreadyFromSession();
       if ( u == null )
          return false;
       
       return u.isLoggedIn();   
    }
    
    public String login(String name, String password) throws IllegalArgumentException
    {
       User user = new User();

       if ( LoginProperties.getInstance().validateLoginID(name, password) )
       {
           user.setLoggedIn(true);
           return storeUserInSession(user);

       }

       return null;
   }

    
    private String storeUserInSession(User user)
    {
       HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
       HttpSession session = httpServletRequest.getSession();
       session.setAttribute("user", user);
       return session.getId();
    }
    
    private User getUserAlreadyFromSession()
    {

       User user = null;
       HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
       HttpSession session = httpServletRequest.getSession();
       Object userObj = session.getAttribute("user");
       if (userObj != null && userObj instanceof User) {
        user = (User) userObj;
       }
       return user;

    }

    private User loginFromSessionServer()
    {
       User user = getUserAlreadyFromSession();
       return user;
    }

    public void logout()
    {
       deleteUserFromSession();
    }

    private void deleteUserFromSession()
    {
       HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
       HttpSession session = httpServletRequest.getSession();
       session.removeAttribute("user");
    }

    public Long countDownServer() throws IllegalArgumentException
    {
        // TODO Count down timer logic.  Countdown the time until the telnet controller
        // will reconnect to the server.  This is only applicable if the stoker is 
        // disconnected.  The timer works and is implemented, but i'd be nice to display
        // this on the browser as well as a connect now button.
        return null;
    }

    public ArrayList<ArrayList<SDataPoint>> getAllGraphDataPoints(String logName)
            throws IllegalArgumentException
    {
        return m_stokerSharedServices.getAllGraphDataPoints(logName);
    }

    public ArrayList<LogItem> getLogList() throws IllegalArgumentException
    {
        return m_stokerSharedServices.getLogList();
    }

    /**
     * Begin a new log
     */
    public Integer startLog(String cookerName, String logName, ArrayList<SDevice> deviceList ) throws IllegalArgumentException
    {
       if ( ! loginGuard() )
          return -1;
       
        return m_stokerSharedServices.startLog( cookerName,  logName,  deviceList);
    }

    public String stopLog(String cookerName, String logName)
            throws IllegalArgumentException
    {
       if ( ! loginGuard() )
          return "";
       
        return m_stokerSharedServices.stopLog( cookerName,  logName );
    }

    public Integer validateSession(String sessionID)
            throws IllegalArgumentException
    {
        if ( loginGuard() )
           return new Integer(1);
        
       return new Integer(0);
    }


    public Integer updateTempAndAlarmSettings(ArrayList<SDevice> asd)
            throws IllegalArgumentException
    {
       if ( ! loginGuard() )
          return -1;

        return m_stokerSharedServices.updateTempAndAlarmSettings(asd);
    }

    public LogDir getLogFileNames() throws IllegalArgumentException
    {
        return m_stokerSharedServices.getLogFileNames();
    }

    public Integer attachToExistingLog(String cookerName, String selectedLog, String fileName)
            throws IllegalArgumentException
    {
       if ( ! loginGuard() )
          return -1;
       
        return m_stokerSharedServices.attachToExistingLog(cookerName, selectedLog, fileName);

    }

    public void sessionCreated(HttpSessionEvent arg0)
    {
        logger.info("Http Session Created");

    }

    public void sessionDestroyed(HttpSessionEvent arg0)
    {
        logger.info("Http Session Destroyed");
        HttpSession hs = arg0.getSession();
      //  CustomSession httpSession = (CustomSession)arg0.getSession();
        
   //     ClientMessagePusher.getInstance().removeSession(hs); // This was causing a stack overflow error
                                         // cleanup work should be done in removeSession.
                                                            
      //  removeControllerEvents();  // Is this correct?  gbak

    }

    public void cometRequest(CallBackRequestType cometRequestType)
            throws IllegalArgumentException
    {
        switch ( cometRequestType.getRequestType() )
        {
            case GET_STATUS:
                getStatus();
                break;
            case FORCE_DATA_PUSH:
                forceLatestDataPush();
                break;
        }
    }

    /**
     * Requests the status of the DataController.  Results are returned in the Comet Stream. 
     */
    private void getStatus()
    {
        HttpSession httpSession = getThreadLocalRequest().getSession();
      //  CustomSession httpSession = (CustomSession)getThreadLocalRequest().getSession();
        Status s = m_stokerSharedServices.getStatus();
        
        m_ClientMessenger.sessionPush( httpSession, new HardwareDeviceState( s, null ) );
    }

    /**
     * Force the server to push the latest data to the specific http session
     * It will get the data from the comet stream. 
     */
    private void forceLatestDataPush()
    {
        HttpSession httpSession = getThreadLocalRequest().getSession();
      //  CustomSession httpSession = (CustomSession)getThreadLocalRequest().getSession();
        
        //for ( SDataPoint sdp : Controller.getInstance().getDataOrchestrator().getLastDPs())
        for ( SDataPoint sdp : m_pitMonitor.getCurrentTemps())
            m_ClientMessenger.sessionPush( httpSession, sdp);

        WeatherData wd = m_WeatherController.getWeather();
        if ( wd != null )
            m_ClientMessenger.sessionPush( httpSession, wd );
    }

    
    /* (non-Javadoc)
     * @see com.gbak.sweb.client.StokerCoreService#addNoteToLog(java.lang.String, java.util.ArrayList)
     */
    public Integer addNoteToLog(String note, ArrayList<String> logList)
            throws IllegalArgumentException
    {
        if ( ! loginGuard() )
            return -1;
        
        return m_stokerSharedServices.addNoteToLog(note, logList);
    }

    @Override
    public HashMap<String, String> getClientProperties()
            throws IllegalArgumentException
    {
        
        return StokerWebProperties.getInstance().getClientProperties();
    }
    
    
    
    
    /*private static class CustomSession extends SessionWrapper {
        public CustomSession (HttpSession delegate, String prefix) {
            super (delegate);
            this.prefix = prefix;
        }
            
        @Override
        public Object getAttribute (String name) {
            return super.getAttribute (this.prefix + "_" + name);
        }
            
        @Override
        public Enumeration<String> getAttributeNames () {
            List<String> names = new ArrayList<String> ();
            for (@SuppressWarnings("unchecked")
            Enumeration<String> e = super.getAttributeNames (); e.hasMoreElements (); ) {
                String name = e.nextElement ();
                if (name.startsWith (this.prefix)) {
                    names.add (name);
                }
            }
                
            final Iterator<String> i = names.iterator ();
            return new Enumeration<String> () {
                @Override
                public boolean hasMoreElements () {
                    return i.hasNext ();
                }

                @Override
                public String nextElement () {
                    return i.next ();
                }
            };
        }
            
        @Override
        public void removeAttribute (String name) {
            super.removeAttribute (this.prefix + "_" + name);
        }
            
        private String prefix;
    }*/
        
   /* private class CustomSessionRequest extends HttpServletRequestWrapper {
        public CustomSessionRequest (HttpServletRequest delegate) {
            super (delegate);
        }
            
        @Override
        public HttpSession getSession (boolean create) {
            HttpSession session = super.getSession (create);
            if (null == session && !create) {
                return null;
            }
            return new CustomSession (session, this.getPathInfo ());
        }
    }
        
    @Override
    protected void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet (new CustomSessionRequest (request), response);
    }
*/
    @Override
    public Integer updateStokerWebConfig(CookerList cookerList )
    {      
        
        return m_stokerSharedServices.updateStokerWebConfig(cookerList);
    }

    @Override
    public CookerList getStokerWebConfiguration() throws IllegalArgumentException
    {
        CookerList cl = m_stokerSharedServices.getStokerWebConfiguration(); 
        
        cl.setStatus(m_lastEvent);
        return cl;
    }
    
    
}