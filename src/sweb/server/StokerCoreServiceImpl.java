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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

import sweb.client.StokerCoreService;
import sweb.server.controller.Controller;
import sweb.server.controller.StokerWebConfiguration;
import sweb.server.controller.StokerConfiguration;
import sweb.server.controller.data.DataOrchestrator;
import sweb.server.controller.events.ConfigControllerEvent;
import sweb.server.controller.events.ConfigControllerEventListener;
import sweb.server.controller.events.DataControllerEvent;
import sweb.server.controller.events.DataControllerEventListener;
import sweb.server.controller.events.DataPointEvent;
import sweb.server.controller.events.DataPointEventListener;
import sweb.server.controller.events.DataControllerEvent.EventType;
import sweb.server.controller.events.WeatherChangeEvent;
import sweb.server.controller.events.WeatherChangeEventListener;
import sweb.server.controller.log.ListLogFiles;
import sweb.server.controller.log.exceptions.LogExistsException;
import sweb.server.controller.log.exceptions.LogNotFoundException;
import sweb.server.security.LoginProperties;
import sweb.server.security.User;
import sweb.shared.model.CallBackRequestType;
import sweb.shared.model.Cooker;
import sweb.shared.model.CookerList;
import sweb.shared.model.HardwareDeviceStatus;
import sweb.shared.model.HardwareDeviceStatus.Status;
import sweb.shared.model.LogItem;
import sweb.shared.model.alerts.AlertModel;
import sweb.shared.model.data.SBlowerDataPoint;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.data.SProbeDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.events.ControllerEventLight;
import sweb.shared.model.events.ControllerEventLight.EventTypeLight;
import sweb.shared.model.events.LogEvent;
import sweb.shared.model.events.LogEvent.LogEventType;
import sweb.shared.model.logfile.LogDir;
import sweb.shared.model.weather.WeatherData;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class StokerCoreServiceImpl extends RemoteServiceServlet implements
        StokerCoreService, HttpSessionListener 
{

    //private ConcurrentMap<String,CometSession> webSessions = new ConcurrentHashMap<String, CometSession>();
    

    private static int i =0;
   
    private boolean haveDataConnection = false;

    private Timer readTimeDataTimer = null;
    private DataPointEventListener m_DPEL= null;

    DataControllerEventListener m_dcel = null;
    ConfigControllerEventListener m_ccel = null;
    WeatherChangeEventListener m_wcel = null;

    private static final Logger logger = Logger.getLogger(StokerCoreServiceImpl.class.getName());
    
    public HashMap<String,SDevice> getDeviceConfiguration()
            throws IllegalArgumentException
    {
        return StokerConfiguration.getInstance().data();
    }

    public void setupCallBack()
    {
       HttpSession httpSession = getThreadLocalRequest().getSession();
      // CustomSession httpSession = (CustomSession)getThreadLocalRequest().getSession();

       Controller.getInstance().getClientMessenger().addSession( httpSession );
       
       handleControllerEvents();

       if ( m_DPEL == null)
       {
           logger.info("Creating new listener.");
           m_DPEL = new DataPointEventListener() {

               public void stateChange(DataPointEvent dpe)
                {
                   ArrayList<SProbeDataPoint> aldp = dpe.getSProbeDataPoints();
                   if ( aldp != null)
                   {
                       for ( SProbeDataPoint sdp : aldp)
                       {
                           Controller.getInstance().getClientMessenger().push(sdp);
                       }
                   }

                   // The new data point here is a dummy, it is needed to create the sharp
                   // steps in the blower graph.
                   SBlowerDataPoint bdp = dpe.getSBlowerDataPoint();
                   if ( bdp != null  )
                   {
                      Controller.getInstance().getClientMessenger().push(bdp);
                   }

                }

           };
           Controller.getInstance().getDataOrchestrator().addListener( m_DPEL );
       }
    }

    public ArrayList<SDataPoint> getNewGraphDataPoints(String input) throws IllegalArgumentException
    {
        return Controller.getInstance().getDataOrchestrator().getLastDPs();

    }

    private void removeControllerEvents()
    {
        logger.info("Removing event listeners!");
        Controller.getInstance().removeDataEventListener( m_dcel );
        Controller.getInstance().removeConfigEventListener(m_ccel);
        Controller.getInstance().removeWeatherChangeEventListener(m_wcel);
    }

    private void handleControllerEvents()
    {

        if (m_dcel == null)
        {
            m_dcel = new DataControllerEventListener() {

                public void actionPerformed(DataControllerEvent ce)
                {
                    switch (ce.getEventType())
                    {
                        case CONNECTION_ESTABLISHED:
                            Controller.getInstance().getClientMessenger()
                                    .push(new ControllerEventLight(
                                            EventTypeLight.CONNECTION_ESTABLISHED));
                            break;
                        case NONE:
                            break;
                        case LOST_CONNECTION:
                            Controller.getInstance().getClientMessenger().push(
                                    new ControllerEventLight(
                                            EventTypeLight.LOST_CONNECTION));
                            break;
                        default:

                    }
                    if (ce.getEventType() == EventType.CONNECTION_ESTABLISHED)
                    {

                    }
                }

            };

            Controller.getInstance().addDataEventListener(m_dcel);

        }

        if (m_ccel == null)
        {
            m_ccel = new ConfigControllerEventListener() {

                public void actionPerformed(ConfigControllerEvent ce)
                {
                    switch (ce.getEventType())
                    {
                        case NONE:
                            break;
                        case CONFIG_UPDATE:
                            Controller.getInstance().getClientMessenger().push(
                                    new ControllerEventLight(
                                            EventTypeLight.CONFIG_UPDATE));
                            break;
                        default:
                    }
                }

            };

            Controller.getInstance().addConfigEventListener(m_ccel);
        }

        if (m_wcel == null)
        {
            m_wcel = new WeatherChangeEventListener() {

                public void weatherUpdated(WeatherChangeEvent wce)
                {
                    WeatherData wd = wce.getWeatherData();
                    if ( wd != null )
                       Controller.getInstance().getClientMessenger().push(wd);
                }

            };

            Controller.getInstance().addWeatherChangeEventListener(m_wcel);
        }
    }
    
    public void setAlertConfiguration( ArrayList<AlertModel> alertBaseList )
    {
       Controller.getInstance().setAlertConfiguration(alertBaseList);
       
    }
    
    public ArrayList<AlertModel> getAlertConfiguration()
    {
       ArrayList<AlertModel> ab = Controller.getInstance().getAlertConfiguration();
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
        // TODO Auto-generated method stub
        return null;
    }

    public ArrayList<ArrayList<SDataPoint>> getAllGraphDataPoints(String logName)
            throws IllegalArgumentException
    {

        return Controller.getInstance().getDataOrchestrator().getAllDataPoints( logName );
    }

    public ArrayList<LogItem> getLogList() throws IllegalArgumentException
    {
        return Controller.getInstance().getDataOrchestrator().getLogList();
    }

    public Integer startLog(String strCookerName, String strLogName, ArrayList<SDevice> arSD ) throws IllegalArgumentException
    {
       if ( ! loginGuard() )
          return -1;
       
        Integer ret = new Integer(0);
        if ( ! Controller.getInstance().getDataOrchestrator().isLogRunning(strLogName))
        {
            try
            {
                LogItem li = new LogItem(strCookerName, strLogName, arSD);
                Controller.getInstance().getDataOrchestrator().startLog( li );
                ret = 1;
                LogEvent le = new LogEvent(LogEventType.NEW, strCookerName, strLogName );
                Controller.getInstance().getClientMessenger().push( le );

            }
            catch (LogExistsException e)
            {
                try { Controller.getInstance().getDataOrchestrator().stopLog("Default"); } catch (LogNotFoundException e1) { ret = 0; }

            }
        }
        return ret;
    }

    public Integer stopLog(String strCookerName, String strLogName)
            throws IllegalArgumentException
    {
       if ( ! loginGuard() )
          return -1;
       
        Integer ret = new Integer(0);
        try
        {
            Controller.getInstance().getDataOrchestrator().stopLog(strLogName);
            ret = new Integer(1);
            // Create LogEvent and pass it back via comet stream
            LogEvent le = new LogEvent(LogEventType.DELETED, strCookerName, strLogName );
            Controller.getInstance().getClientMessenger().push( le );
        }
        catch (LogNotFoundException e)
        {
            // TODO Auto-generated catch block
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


    public Integer updateConfiguration(ArrayList<SDevice> asd)
            throws IllegalArgumentException
    {
       if ( ! loginGuard() )
          return -1;
       
        StokerConfiguration.getInstance().update( asd );
        Controller.getInstance().loadConfiguration();
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
       
        return Controller.getInstance().getDataOrchestrator().attachToExistingLog(cookerName, selectedLog, fileName);

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
        if ( Controller.getInstance().isDataControllerReady() )
            s = Status.CONNECTED;
        else
            s = Status.DISCONNECTED;

        Controller.getInstance().getClientMessenger().sessionPush( httpSession, new HardwareDeviceStatus( s, null ) );
    }

    /**
     * Force the server to push the latest data to the specific http session
     * It will get the data from the comet stream. 
     */
    private void forceLatestDataPush()
    {
        HttpSession httpSession = getThreadLocalRequest().getSession();
      //  CustomSession httpSession = (CustomSession)getThreadLocalRequest().getSession();
        
        for ( SDataPoint sdp : Controller.getInstance().getDataOrchestrator().getLastDPs())
           Controller.getInstance().getClientMessenger().sessionPush( httpSession, sdp);

        WeatherData wd = Controller.getInstance().getWeatherController().getWeather();
        if ( wd != null )
                Controller.getInstance().getClientMessenger().sessionPush( httpSession, wd );
    }

    
    /* (non-Javadoc)
     * @see sweb.client.StokerCoreService#addNoteToLog(java.lang.String, java.util.ArrayList)
     */
    public Integer addNoteToLog(String note, ArrayList<String> logList)
            throws IllegalArgumentException
    {
        if ( ! loginGuard() )
            return -1;
        
        Controller.getInstance().getDataOrchestrator().addNoteToLog(note, logList);
        return 0;
    }

    @Override
    public HashMap<String, String> getClientProperties()
            throws IllegalArgumentException
    {
        
        return StokerWebProperties.getInstance().getClientProperties();
    }
    
    
    
    
    private static class CustomSession extends SessionWrapper {
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
    }
        
    private class CustomSessionRequest extends HttpServletRequestWrapper {
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

    @Override
    public Integer updateStokerWebConfig(CookerList cookerList )
    {
        // TODO Auto-generated method stub
        
        // Save Cooker to property file as JSON
        StokerWebConfiguration.getInstance().saveConfig(cookerList);
        
        // Update Stoker
        // Restart necessary Server objects to reflect updated config
        // send refresh over comet stream to refresh clients.
        return new Integer(1);
    }
    
    
}