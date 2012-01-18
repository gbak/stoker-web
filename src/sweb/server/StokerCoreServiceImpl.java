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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

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
import sweb.shared.model.alerts.AlertModel;
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
/**
 * @author gary.bak
 *
 */
/**
 * @author gary.bak
 *
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

    public HashMap<String,SDevice> getConfiguration()
            throws IllegalArgumentException
    {
        return StokerConfiguration.getInstance().data();
    }

    public void setupCallBack()
    {
       HttpSession httpSession = getThreadLocalRequest().getSession();

       ClientMessagePusher.getInstance().addSession( httpSession );
       
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
                           ClientMessagePusher.getInstance().push(sdp);
                       }
                   }

                   // The new data point here is a dummy, it is needed to create the sharp
                   // steps in the blower graph.
                   SBlowerDataPoint bdp = dpe.getSBlowerDataPoint();
                   if ( bdp != null  )
                   {
                       if (! bdp.isTimedEvent() )
                       {
                           SBlowerDataPoint newBDP = new SBlowerDataPoint(bdp);
                           newBDP.setBlowerState(!bdp.isFanOn());  // TODO: check this gbak
                           newBDP.setTotalRuntime(-1);
                           ClientMessagePusher.getInstance().push(newBDP);
    
                           Calendar cal = Calendar.getInstance();
                           cal.setTime(bdp.getCollectedDate());
                           cal.add(Calendar.MILLISECOND, 10);
                           bdp.setCollectedDate(cal.getTime());
                           ClientMessagePusher.getInstance().push(bdp);
                       }
                       else
                       {
                           // if the event is triggered by time and the status is the same as the
                           // previous, we don't need the dummy data point.
                           
                           // This can be a bug if this event happens to run before the live event
                           // after the fan switches on.
                           ClientMessagePusher.getInstance().push(bdp);
                       }
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
                        ClientMessagePusher.getInstance().push( new ControllerEventLight(EventTypeLight.CONNECTION_ESTABLISHED));
                       break;
                    case NONE:
                        break;
                    case LOST_CONNECTION:
                        ClientMessagePusher.getInstance().push( new ControllerEventLight(EventTypeLight.LOST_CONNECTION));
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
                        ClientMessagePusher.getInstance().push( new ControllerEventLight(EventTypeLight.CONFIG_UPDATE));
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
                ClientMessagePusher.getInstance().push( wce.getWeatherData() );
            }

        };

        Controller.getInstance().addWeatherChangeEventListener(m_wcel);
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
        // What a mess this is
        // This method was originally just this:  return DataOrchestrator.getInstance().getAllDataPoints( logName );
        // but had to be modified to move the dummy fan values out of the LogFileFormatter
        // since that method was also used to read the log files.
        
        ArrayList<ArrayList<SDataPoint>> outer = new ArrayList<ArrayList<SDataPoint>>();
        for ( ArrayList<SDataPoint> ar1 : DataOrchestrator.getInstance().getAllDataPoints( logName ))
        {
            ArrayList<SDataPoint> ar2 = new ArrayList<SDataPoint>();
            boolean skipped = false;
            boolean blower = false;
            for ( SDataPoint sd : ar1 )
            {
                
               if ( blower == true || sd instanceof SBlowerDataPoint ) 
               {
                   SBlowerDataPoint sdp2 = (SBlowerDataPoint) sd;
                   blower = true;
                   SBlowerDataPoint sdp3 = new SBlowerDataPoint( sdp2.getDeviceID(), sdp2.getCollectedDate(), !sdp2.isFanOn() );    
                   ar2.add( sdp3 );

                   Calendar cal = Calendar.getInstance();
                   cal.setTime(sdp2.getCollectedDate());
                   cal.add(Calendar.MILLISECOND, 10);
                   sdp3 = new SBlowerDataPoint( sdp2.getDeviceID(), sdp2.getCollectedDate(), sdp2.isFanOn());
                   ar2.add( sdp3 );
               }
               else
               {
                   skipped = true;
                   break;
               }
               
            }
            if ( skipped == true )
            {
                outer.add( ar1 );
            }
            else
            {
                outer.add( ar2 );
            }
            
        }
        
        return outer;
        //return DataOrchestrator.getInstance().getAllDataPoints( logName );
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
                ClientMessagePusher.getInstance().push( le );

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
            ClientMessagePusher.getInstance().push( le );
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

    /**
     * Requests the status of the DataController.  Results are returned in the Comet Stream. 
     */
    private void getStatus()
    {
        HttpSession httpSession = getThreadLocalRequest().getSession();

        Status s = null;
        if ( Controller.getInstance().isDataControllerReady() )
            s = Status.CONNECTED;
        else
            s = Status.DISCONNECTED;

        ClientMessagePusher.getInstance().sessionPush( httpSession, new HardwareDeviceStatus( s, null ) );
    }

    /**
     * Force the server to push the latest data to the specific http session
     * It will get the data from the comet stream. 
     */
    private void forceLatestDataPush()
    {
        HttpSession httpSession = getThreadLocalRequest().getSession();
        
        for ( SDataPoint sdp : DataOrchestrator.getInstance().getLastDPs())
           ClientMessagePusher.getInstance().sessionPush( httpSession, sdp);

        WeatherData wd = Controller.getInstance().getWeatherController().getWeather();
                ClientMessagePusher.getInstance().sessionPush( httpSession, wd );
    }

    
    /* (non-Javadoc)
     * @see sweb.client.StokerCoreService#addNoteToLog(java.lang.String, java.util.ArrayList)
     */
    public Integer addNoteToLog(String note, ArrayList<String> logList)
            throws IllegalArgumentException
    {
        if ( ! loginGuard() )
            return -1;
        
        DataOrchestrator.getInstance().addNoteToLog(note, logList);
        return 0;
    }

    
}