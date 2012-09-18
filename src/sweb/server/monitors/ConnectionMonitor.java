package sweb.server.monitors;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import sweb.server.config.StokerWebConfiguration;
import sweb.server.data.DataController;
import sweb.server.events.StateChangeEvent;
import sweb.server.events.StateChangeEvent.EventType;
import sweb.server.log.LogManager;
import sweb.server.monitors.stoker.StokerPitMonitor;
import sweb.server.weather.WeatherController;
import sweb.shared.model.HardwareDeviceState.Status;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import sweb.server.StokerWebConstants;
import sweb.server.StokerWebProperties;

public class ConnectionMonitor
{

    public enum MonitorState { OFFLINE, ONLINE };
    PitMonitor m_pitMonitor;
    EventBus   m_eventBus;
    StokerWebConfiguration m_stokerWebConfiguration;
    DataController m_dataController;
    WeatherController m_weatherController;
    LogManager m_logManager;
    MonitorState m_currentState = MonitorState.OFFLINE;
    
    int m_timeoutToExtendedLoss = 0;
    int m_timeoutToReconnect = 0;
    
    static Timer m_monitorTimer = new Timer();
    Date m_LastMessageTime = null;
    EventType m_lastEvent = EventType.NONE;
    
    AtomicBoolean starting = new AtomicBoolean(false);
    
    private static final Logger logger = Logger.getLogger(ConnectionMonitor.class.getName());
    
    @Inject
    private ConnectionMonitor( PitMonitor pitMonitor,
                                EventBus eventBus,
                                StokerWebConfiguration swc,
                                DataController dataController,
                                LogManager logManager,
                                WeatherController weatherController )
    {
        m_pitMonitor = pitMonitor;
        m_eventBus = eventBus;
        m_logManager = logManager;
        m_stokerWebConfiguration = swc;
        m_dataController = dataController;
        m_weatherController = weatherController;
        
        m_eventBus.register(this);
        
        getTimeoutValues();
        logger.debug("ConnectionMonitor constructor complete");
        
    }
    
    public void start()
    {
       if ( starting.compareAndSet(false, true))
       {
           logger.info("Connection monitor starting");
            startMonitor();
            if ( m_pitMonitor.start() )
            {
                m_currentState = MonitorState.ONLINE;
            }
            m_weatherController.start();
            
       }
       starting.set(false);
    }
    
    public void stop()
    {
        logger.info("Connection monitor ending");
       // m_weatherController.stop(); // Keep the weather running for the default screen?
        m_pitMonitor.stop();
        m_logManager.stopAllLogs();
        m_currentState = MonitorState.OFFLINE;
        logger.info("Connection monitor ended");
    }
    
    private boolean isRunning()
    {
        return m_dataController.isConnected();
    }
    
    private void startMonitor()
    {
        synchronized( m_monitorTimer )
        {
           ConnectionStatusTimerTask _runTimer = new ConnectionStatusTimerTask();
           m_monitorTimer.schedule( _runTimer, getNextRunDate() );
        }
    }

    private class ConnectionStatusTimerTask extends TimerTask
    {
       public void run()
       {
           synchronized( m_monitorTimer )
           {
               logger.debug("Connection monitor task timer running");
              if (m_pitMonitor.getState().getHardwareStatus() == Status.DISCONNECTED && m_currentState == MonitorState.ONLINE )
              {
                  logger.debug("Detected extended connection loss, posting message and calling stop");
                  // If were are here, then it must have been disconnected for 30 minutes.
                  m_eventBus.post(new StateChangeEvent( this, EventType.EXTENDED_CONNECTION_LOSS ));
                  stop();
              }
              else if ( m_currentState == MonitorState.OFFLINE  && starting.get() == false )
              {
                  logger.debug("Detected stoker online, calling start");
                  if ( m_pitMonitor.testConnection() && starting.get() == false )
                      start();
              }
    
              m_monitorTimer.schedule( new ConnectionStatusTimerTask(), getNextRunDate() );
           }
       }
    }

    private Date getNextRunDate()
    {
        int nextTime = 0;
        
        if ( m_currentState == MonitorState.ONLINE )
            nextTime = m_timeoutToExtendedLoss;  
        else
            nextTime = m_timeoutToReconnect;  // 15
            
        Calendar nextRun = Calendar.getInstance();
        nextRun.add( Calendar.MINUTE, nextTime);
        return nextRun.getTime();
    }
    
    @Subscribe
    public void restTimerOnStateChange(StateChangeEvent sce)
    {
        // If the same event comes in, don't reset the timer.
        if ( sce.getEventType() != m_lastEvent )
        {
            synchronized( m_monitorTimer )
            {
               m_monitorTimer.cancel();
               m_monitorTimer = new Timer();
               m_monitorTimer.schedule( new ConnectionStatusTimerTask(), getNextRunDate() );
            }
            m_lastEvent = sce.getEventType();
        }
    }
    

    private void getTimeoutValues()
    {
        if ( m_timeoutToExtendedLoss == 0 || m_timeoutToExtendedLoss == 0 )
        {
            try
            {
               m_timeoutToExtendedLoss = new Integer((String)StokerWebConstants.TIMEOUT_TO_EXTENDED_LOSS).intValue();
               m_timeoutToReconnect = new Integer((String)StokerWebConstants.TIMEOUT_TO_RECONNECT).intValue();
            
               m_timeoutToExtendedLoss = new Integer((String)StokerWebProperties.getInstance().get(StokerWebConstants.TIMEOUT_TO_EXTENDED_LOSS)).intValue();
               m_timeoutToReconnect = new Integer((String)StokerWebProperties.getInstance().get(StokerWebConstants.TIMEOUT_TO_RECONNECT)).intValue();

            }
            catch( NumberFormatException nfe )
            {
                   
            }
        }
    }
    
}
