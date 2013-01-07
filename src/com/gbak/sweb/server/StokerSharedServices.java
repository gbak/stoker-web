package com.gbak.sweb.server;

import java.util.ArrayList;
import java.util.Calendar;


import com.gbak.sweb.server.alerts.AlertsManagerImpl;
import com.gbak.sweb.server.config.StokerWebConfiguration;
import com.gbak.sweb.server.log.LogManager;
import com.gbak.sweb.server.log.exceptions.LogExistsException;
import com.gbak.sweb.server.log.exceptions.LogNotFoundException;
import com.gbak.sweb.server.log.file.ListLogFiles;
import com.gbak.sweb.server.monitors.PitMonitor;
import com.gbak.sweb.server.weather.WeatherController;
import com.gbak.sweb.shared.model.ConfigurationSettings;
import com.gbak.sweb.shared.model.CookerList;
import com.gbak.sweb.shared.model.HardwareDeviceState;
import com.gbak.sweb.shared.model.LogItem;
import com.gbak.sweb.shared.model.HardwareDeviceState.Status;
import com.gbak.sweb.shared.model.data.SDataPoint;
import com.gbak.sweb.shared.model.devices.SDevice;
import com.gbak.sweb.shared.model.events.ControllerEventLight;
import com.gbak.sweb.shared.model.events.LogEvent;
import com.gbak.sweb.shared.model.events.ControllerEventLight.EventTypeLight;
import com.gbak.sweb.shared.model.events.LogEvent.LogEventType;
import com.gbak.sweb.shared.model.logfile.LogDir;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;


public class StokerSharedServices
{

    EventBus m_eventBus = null;
    PitMonitor m_pitMonitor = null;
    LogManager m_logManager = null;
    ClientMessenger m_ClientMessenger = null;
    WeatherController m_WeatherController = null;
    AlertsManagerImpl m_alertsManager = null;
    StokerWebConfiguration m_StokerWebConfig = null;
    
    @Inject
    public StokerSharedServices( StokerWebConfiguration config, 
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
    
    public Integer updateTempAndAlarmSettings(ArrayList<SDevice> asd)
            throws IllegalArgumentException
    {
       m_StokerWebConfig.updateConfig(asd);

        return new Integer(1);
    }
    
    public ConfigurationSettings getDeviceConfiguration() throws IllegalArgumentException
    {
        return new ConfigurationSettings( m_StokerWebConfig.getCookerList(), m_pitMonitor.getRawDevices() );
    }

    public Integer startLog(String strCookerName, String strLogName, ArrayList<SDevice> arSD ) throws IllegalArgumentException
    {
       
        Integer ret = new Integer(0);
        if ( ! m_logManager.isLogRunning(strLogName))
        {
            try
            {
                LogItem li = new LogItem(strCookerName, strLogName, Calendar.getInstance().getTime(), arSD);
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
           // logger.error("stopLog: provided log not found");
            e.printStackTrace();
        }
        return ret;
    }
    
    public LogDir getLogFileNames() throws IllegalArgumentException
    {
        return ListLogFiles.getAllLogFiles();
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

    public Integer attachToExistingLog(String cookerName, String selectedLog, String fileName)
            throws IllegalArgumentException
    {       
        return m_logManager.attachToExistingLog(cookerName, selectedLog, fileName);

    }

    public Status getStatus()
    {
        Status s = null;
    
        if ( m_pitMonitor.isActive() )
            s = Status.CONNECTED;
        else
            s = Status.DISCONNECTED;
        return s;
    }
    
    
    public Integer addNoteToLog(String note, ArrayList<String> logList)
            throws IllegalArgumentException
    {        
        m_logManager.addNoteToLog(note, logList);
        return 0;
    }

    public Integer updateStokerWebConfig(CookerList cookerList )
    {      
        // Save Cooker to property file as JSON
        m_StokerWebConfig.saveConfig(cookerList);

        m_ClientMessenger.push(
                new ControllerEventLight(
                        EventTypeLight.CONFIG_UPDATE_REFRESH));
        return new Integer(1);
    }

    public CookerList getStokerWebConfiguration()
            throws IllegalArgumentException
    {
        return m_StokerWebConfig.getCookerList();
    }
    
    
    
}
