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

import sweb.client.StokerCoreService;
import sweb.server.alerts.AlertsManagerImpl;
import sweb.server.config.StokerWebConfiguration;

import sweb.server.events.DataPointEvent;
import sweb.server.events.ConfigChangeEvent;
import sweb.server.events.StateChangeEvent;
import sweb.server.events.WeatherChangeEvent;
import sweb.server.events.StateChangeEvent.EventType;

import sweb.server.log.LogManager;
import sweb.server.log.exceptions.LogExistsException;
import sweb.server.log.exceptions.LogNotFoundException;
import sweb.server.log.file.ListLogFiles;
import sweb.server.monitors.PitMonitor;
import sweb.server.security.LoginProperties;
import sweb.server.security.User;
import sweb.server.weather.WeatherController;

import sweb.shared.model.CookerList;
import sweb.shared.model.LogItem;
import sweb.shared.model.logfile.LogDir;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.events.LogEvent;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.alerts.AlertModel;
import sweb.shared.model.alerts.BrowserAlarmModel;
import sweb.shared.model.weather.WeatherData;
import sweb.shared.model.CallBackRequestType;
import sweb.shared.model.data.SProbeDataPoint;
import sweb.shared.model.HardwareDeviceState;
import sweb.shared.model.ConfigurationSettings;
import sweb.shared.model.data.SBlowerDataPoint;
import sweb.shared.model.HardwareDeviceState.Status;
import sweb.shared.model.events.LogEvent.LogEventType;
import sweb.shared.model.events.ControllerEventLight;
import sweb.shared.model.events.ControllerEventLight.EventTypeLight;

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
    LogManager m_logManager = null;
    ClientMessenger m_ClientMessenger = null;
    WeatherController m_WeatherController = null;
    AlertsManagerImpl m_alertsManager = null;
    StokerWebConfiguration m_StokerWebConfig = null;
    
    private static final Logger logger = Logger.getLogger(StokerCoreServiceImpl.class.getName());
    
    @Inject
    public StokerCoreServiceImpl( StokerWebConfiguration config, 
                                  PitMonitor pm,
                                  ClientMessenger cm,
                                  AlertsManagerImpl am,
                                  LogManager lm,
                                  WeatherController wc,
                                  EventBus bus)
    {
        this.m_StokerWebConfig = config;
        this.m_pitMonitor = pm;
        this.m_ClientMessenger = cm;
        this.m_WeatherController = wc;
        this.m_logManager = lm;
        this.m_alertsManager = am;
        this.m_eventBus = bus;
        
        m_eventBus.register(this);
        
    }
    
    public ConfigurationSettings getDeviceConfiguration()
            throws IllegalArgumentException
    {
        return new ConfigurationSettings( m_StokerWebConfig.getCookerList(), m_pitMonitor.getRawDevices() );
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
                break;
            case NONE:
                break;
            case LOST_CONNECTION:
                m_ClientMessenger.push(
                        new ControllerEventLight(
                                EventTypeLight.LOST_CONNECTION));
                break;
            case EXTENDED_CONNECTION_LOSS:
                m_ClientMessenger.push(
                        new ControllerEventLight(
                                EventTypeLight.EXTENDED_CONNECTION_LOSS));
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
        return m_logManager.getAllDataPoints(logName);
    }

    public ArrayList<LogItem> getLogList() throws IllegalArgumentException
    {
        return m_logManager.getLogList();
    }

    /**
     * Begin a new log
     */
    public Integer startLog(String strCookerName, String strLogName, ArrayList<SDevice> arSD ) throws IllegalArgumentException
    {
       if ( ! loginGuard() )
          return -1;
       
        Integer ret = new Integer(0);
        if ( ! m_logManager.isLogRunning(strLogName))
        {
            try
            {
                LogItem li = new LogItem(strCookerName, strLogName, arSD);
                m_logManager.startLog( li );
                ret = 1;
                LogEvent le = new LogEvent(LogEventType.NEW, strCookerName, strLogName );
                m_ClientMessenger.push( le );

            }
            catch (LogExistsException e)
            {
                try { m_logManager.stopLog("Default"); } catch (LogNotFoundException e1) { ret = 0; }

            }
        }
        return ret;
    }

    public String stopLog(String strCookerName, String strLogName)
            throws IllegalArgumentException
    {
       if ( ! loginGuard() )
          return "";
       
        String ret = new String();
        try
        {
            ret = m_logManager.stopLog(strLogName);
            // Create LogEvent and pass it back via comet stream
            LogEvent le = new LogEvent(LogEventType.DELETED, strCookerName, strLogName );
            m_ClientMessenger.push( le );
        }
        catch (LogNotFoundException e)
        {
            logger.error("stopLog: provided log not found");
            e.printStackTrace();
        }
        return ret;
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


       m_StokerWebConfig.updateConfig(asd);

        return new Integer(1);
    }

    public LogDir getLogFileNames() throws IllegalArgumentException
    {
        return ListLogFiles.getAllLogFiles();
    }

    public Integer attachToExistingLog(String cookerName, String selectedLog, String fileName)
            throws IllegalArgumentException
    {
       if ( ! loginGuard() )
          return -1;
       
        return m_logManager.attachToExistingLog(cookerName, selectedLog, fileName);

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
        Status s = null;
        if ( m_pitMonitor.isActive() )
            s = Status.CONNECTED;
        else
            s = Status.DISCONNECTED;

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
     * @see sweb.client.StokerCoreService#addNoteToLog(java.lang.String, java.util.ArrayList)
     */
    public Integer addNoteToLog(String note, ArrayList<String> logList)
            throws IllegalArgumentException
    {
        if ( ! loginGuard() )
            return -1;
        
        m_logManager.addNoteToLog(note, logList);
        return 0;
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
        // Save Cooker to property file as JSON
        m_StokerWebConfig.saveConfig(cookerList);

        m_ClientMessenger.push(
                new ControllerEventLight(
                        EventTypeLight.CONFIG_UPDATE_REFRESH));
        return new Integer(1);
    }

    @Override
    public CookerList getStokerWebConfiguration()
            throws IllegalArgumentException
    {
        return m_StokerWebConfig.getCookerList();
    }
    
    
}