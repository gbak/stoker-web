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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import net.zschech.gwt.comet.server.CometServlet;
import net.zschech.gwt.comet.server.CometSession;
import sweb.client.StokerCoreService;
import sweb.server.controller.Controller;
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
import sweb.server.controller.weather.WeatherController;
import sweb.server.security.LoginProperties;
import sweb.server.security.User;
import sweb.shared.model.CallBackRequestType;
import sweb.shared.model.HardwareDeviceStatus;
import sweb.shared.model.HardwareDeviceStatus.Status;
import sweb.shared.model.LogItem;
import sweb.shared.model.SBlowerDataPoint;
import sweb.shared.model.SDataPoint;
import sweb.shared.model.SDevice;
import sweb.shared.model.SProbeDataPoint;
import sweb.shared.model.StokerDeviceTypes;
import sweb.shared.model.alerts.Alert;
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

    private ConcurrentMap<String,CometSession> webSessions = new ConcurrentHashMap<String, CometSession>();
    //private ConcurrentMap<String,HttpSession> loginSessions = new ConcurrentHashMap<String,HttpSession>();

    private static int i =0;
   
    private boolean haveDataConnection = false;

    private Timer readTimeDataTimer = null;
    private DataPointEventListener m_DPEL= null;

    DataControllerEventListener m_dcel = null;
    ConfigControllerEventListener m_ccel = null;
    WeatherChangeEventListener m_wcel = null;

    public HashMap<String,SDevice> getConfiguration()
            throws IllegalArgumentException
    {
        return StokerConfiguration.getInstance().data();
    }

    public void setupCallBack()
    {
       HttpSession httpSession = getThreadLocalRequest().getSession();
       CometSession cometSession = CometServlet.getCometSession( httpSession );

       if ( webSessions.putIfAbsent(httpSession.getId(), cometSession) != null )
       {
          //httpSession.invalidate();
          System.out.println("User already on");
          webSessions.remove(httpSession.getId());
          webSessions.put(httpSession.getId(), cometSession);
       }

       handleControllerEvents();

       if ( m_DPEL == null)
       {
           System.out.println("Creating new listener.");
           m_DPEL = new DataPointEventListener() {

               public void stateChange(DataPointEvent dpe)
                {
                   ArrayList<SProbeDataPoint> aldp = dpe.getSProbeDataPoints();
                   if ( aldp != null)
                   {
                       for ( SProbeDataPoint sdp : aldp)
                       {
                          enqueueCometMessage(sdp);
                       }
                   }

                   SBlowerDataPoint bdp = dpe.getSBlowerDataPoint();
                   if ( bdp != null && ! bdp.isTimedEvent() )
                   {
                       SBlowerDataPoint newBDP = new SBlowerDataPoint(bdp);
                       newBDP.setBlowerState(!bdp.isFanOn());
                       enqueueCometMessage(newBDP);

                       Calendar cal = Calendar.getInstance();
                       cal.setTime(bdp.getCollectedDate());
                       cal.add(Calendar.MILLISECOND, 10);
                       bdp.setCollectedDate(cal.getTime());
                       enqueueCometMessage(bdp);
                   }

                }

           };
           DataOrchestrator.getInstance().addListener( m_DPEL );
       }
    }

    public ArrayList<SDataPoint> getNewGraphDataPoints(String input) throws IllegalArgumentException
    {
        return DataOrchestrator.getInstance().getLastDPs();

    }

    private void enqueueCometMessage( Serializable message)
    {
        for ( Map.Entry<String, CometSession> entry: webSessions.entrySet())
        {
            if ( entry.getValue().isValid() )
            {
               entry.getValue().enqueue( message );
            }
            else
            {
                System.out.println("Removing invalid comet session");
                entry.getValue().invalidate();
                webSessions.remove(entry.getKey());
            }

        }
    }

    private void removeControllerEvents()
    {
        System.out.println("Removing event listeners!");
        Controller.getInstance().removeDataEventListener( m_dcel );
        Controller.getInstance().removeConfigEventListener(m_ccel);
        Controller.getInstance().removeWeatherChangeEventListener(m_wcel);
    }

    private void handleControllerEvents()
    {

        DataControllerEventListener m_dcel = new DataControllerEventListener() {

            public void actionPerformed(DataControllerEvent ce)
            {
                switch( ce.getEventType())
                {
                    case CONNECTION_ESTABLISHED:
                        enqueueCometMessage( new ControllerEventLight(EventTypeLight.CONNECTION_ESTABLISHED));
                       break;
                    case NONE:
                        break;
                    case LOST_CONNECTION:
                        enqueueCometMessage( new ControllerEventLight(EventTypeLight.LOST_CONNECTION));
                        break;
                    default:

                }
               if ( ce.getEventType() == EventType.CONNECTION_ESTABLISHED)
               {

               }
            }

        };

        Controller.getInstance().addDataEventListener(m_dcel);

        ConfigControllerEventListener m_ccel = new ConfigControllerEventListener() {

            public void actionPerformed(ConfigControllerEvent ce)
            {
                switch( ce.getEventType())
                {
                    case NONE:
                        break;
                    case CONFIG_UPDATE:
                        enqueueCometMessage( new ControllerEventLight(EventTypeLight.CONFIG_UPDATE));
                        break;
                    default:
                }
            }

           };

        Controller.getInstance().addConfigEventListener(m_ccel);

        WeatherChangeEventListener m_wcel = new WeatherChangeEventListener()
        {

            public void weatherUpdated(WeatherChangeEvent wce)
            {
                enqueueCometMessage( wce.getWeatherData() );
            }

        };

        Controller.getInstance().addWeatherChangeEventListener(m_wcel);
    }
    
    public void setAlertConfiguration( ArrayList<Alert> alertBaseList )
    {
       Controller.getInstance().setAlertConfiguration(alertBaseList);
       
    }
    
    public ArrayList<Alert> getAlertConfiguration()
    {
       ArrayList<Alert> ab = Controller.getInstance().getAlertConfiguration();
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

        // Implement me.  Need to read the Default StokerFile and send
        // all the data points.
        return DataOrchestrator.getInstance().getAllDataPoints( logName );
    }

    public ArrayList<LogItem> getLogList() throws IllegalArgumentException
    {
        return DataOrchestrator.getInstance().getLogList();
    }

    public Integer startLog(String strCookerName, String strLogName, ArrayList<SDevice> arSD ) throws IllegalArgumentException
    {
       if ( ! loginGuard() )
          return -1;
       
        Integer ret = new Integer(0);
        if ( ! DataOrchestrator.getInstance().isLogRunning(strLogName))
        {
            try
            {
                LogItem li = new LogItem(strCookerName, strLogName, arSD);
                DataOrchestrator.getInstance().startLog( li );
                ret = 1;
                LogEvent le = new LogEvent(LogEventType.NEW, strCookerName, strLogName );
                enqueueCometMessage( le );

            }
            catch (LogExistsException e)
            {
                try { DataOrchestrator.getInstance().stopLog("Default"); } catch (LogNotFoundException e1) { ret = 0; }

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
            DataOrchestrator.getInstance().stopLog(strLogName);
            ret = new Integer(1);
            // Create LogEvent and pass it back via comet stream
            LogEvent le = new LogEvent(LogEventType.DELETED, strCookerName, strLogName );
            enqueueCometMessage( le );
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
       
        return DataOrchestrator.getInstance().attachToExistingLog(cookerName, selectedLog, fileName);

    }

    public void sessionCreated(HttpSessionEvent arg0)
    {
        System.out.println("Http Session Created");

    }

    public void sessionDestroyed(HttpSessionEvent arg0)
    {
        System.out.println("Http Session Destroyed");

        removeControllerEvents();

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

    private void getStatus()
    {
        HttpSession httpSession = getThreadLocalRequest().getSession();
        CometSession cometSession = CometServlet.getCometSession( httpSession );
        if ( cometSession == null )
            return;

        Status s = null;
        if ( Controller.getInstance().isDataControllerReady() )
            s = Status.CONNECTED;
        else
            s = Status.DISCONNECTED;

        cometSession.enqueue( new HardwareDeviceStatus( s, null ) );
    }

    private void forceLatestDataPush()
    {
        HttpSession httpSession = getThreadLocalRequest().getSession();
        CometSession cometSession = CometServlet.getCometSession( httpSession );
        if ( cometSession == null )
            return;

        for ( SDataPoint sdp : DataOrchestrator.getInstance().getLastDPs())
           cometSession.enqueue(sdp);

        WeatherData wd = Controller.getInstance().getWeatherController().getWeather();
        cometSession.enqueue( wd );
    }

    public Integer addNoteToLog(String note, ArrayList<String> logList)
            throws IllegalArgumentException
    {
        if ( ! loginGuard() )
            return -1;
        
        DataOrchestrator.getInstance().addNoteToLog(note, logList);
        return 0;
    }

    
}