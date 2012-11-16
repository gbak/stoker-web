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

package sweb.server.log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.jfree.util.Log;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;

import sweb.server.config.StokerWebConfiguration;
import sweb.server.events.ConfigChangeEvent;
import sweb.server.events.DataPointEvent;
import sweb.server.log.exceptions.LogExistsException;
import sweb.server.log.exceptions.LogNotFoundException;
import sweb.server.log.file.ListLogFiles;
import sweb.server.log.file.StokerFile;
import sweb.server.monitors.PitMonitor;
import sweb.shared.model.Cooker;
import sweb.shared.model.CookerHelper;
import sweb.shared.model.LogItem;

import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.logfile.LogNote;

/*
 *   LogManager
 *   
 * 
 */

public class LogManagerImpl implements LogManager
{
  //  ConcurrentHashMap<String,SDataPoint> hmLatestData = new ConcurrentHashMap<String,SDataPoint>();

    // TODO: create interface to replace StokerFile, something like StokerLogInstance...
    
    HashMap<String,StokerFile> m_fileLogList = new HashMap<String,StokerFile>();
 //   ArrayList<BlowerEventListener> m_arListener = new ArrayList<BlowerEventListener>();
 //   private Set<DataPointEventListener> m_dpListener = Collections.newSetFromMap(new ConcurrentHashMap<DataPointEventListener,Boolean>());

    Timer updateTimer = new Timer();
    private PitMonitor m_pitMonitor;
    private EventBus   eventBus;
    private StokerWebConfiguration stokerWebConfiguration;
    private Provider<StokerFile> stokerFileProvider;
    
    private static final Logger logger = Logger.getLogger(LogManagerImpl.class.getName());
    
    @Inject
    public LogManagerImpl(PitMonitor pit, EventBus eventBus,
                           StokerWebConfiguration swc,
                           Provider<StokerFile> stokerFileProvider )
    {
        this.m_pitMonitor = pit;
        this.eventBus = eventBus;
        this.stokerWebConfiguration = swc;
        this.stokerFileProvider = stokerFileProvider;
        
        eventBus.register(this);
        
        RunTimer _runTimer = new RunTimer();
        updateTimer.schedule( _runTimer, 10000 );
    }

    /* Not sure I like this.
     * The stoker files can consume all temp points and then save off the last of each
     * When the timer dings, they can write the temps to to the log, if they
     * are set for the specific log.
     */

    private class RunTimer extends TimerTask
    {
         
       public void run()
       {
          Calendar c = Calendar.getInstance();

          DataPointEvent be = new DataPointEvent(this, true );
          for ( SDataPoint sdp : m_pitMonitor.getCurrentTemps())
          {
              be.addDataPoint(sdp);
          }
          // TODO: decide if to fire the event if there are no data points!
          // TODO: Removed for EventBus
          //controller.fireTempEvent(be);
          
          eventBus.post(be);
          
          

          c.add(Calendar.MINUTE,1);
          c.set(Calendar.SECOND, 0);
          updateTimer.schedule( new RunTimer(), c.getTime() );
       }
    }


    /**
     * Return all the data points for the given log name
     * @param logName name of log to retrieve data points for
     * @return
     */
    @Override
    public ArrayList<ArrayList<SDataPoint>> getAllDataPoints(String logName )
    {
        try
        {
            if ( logName == null || logName.length() == 0)
                logName = "Default";

            return m_fileLogList.get(logName).readAllDataPoints();
        }
        catch (IOException e)
        {
            logger.error("IO Exception calling getAllDataPoints: " + e.getStackTrace());
        }
        return new ArrayList<ArrayList<SDataPoint>>();
    }
    
    @Override
    public ArrayList<SDevice> getConfigSettings( String logName )
    {
       return m_fileLogList.get(logName).getConfigFromFile();    
    }
    
   /* public Set<Entry<String, SDataPoint>> getData()
    {
        return Collections.unmodifiableSet(hmLatestData.entrySet());
    }*/

    /**
     * Returns all the logs currently running on the server
     * @return ArrayList of LogItem 
     */
    @Override
    public ArrayList<LogItem> getLogList()
    {
        ArrayList<LogItem> li = new ArrayList<LogItem>();

        for ( StokerFile sf : m_fileLogList.values() )
        {
            LogItem l = new LogItem(sf.getCookerName(),sf.getName(), sf.getLogStartTime(), sf.getDeviceList());
            li.add(l);
        }
        Collections.sort(li, new Comparator<LogItem>(){

            public int compare(LogItem o1, LogItem o2)
            {
               if ( o1.getLogName().compareTo("Default") == 0)
                   return -1;

               return o1.getLogName().compareTo(o2.getLogName());
            }

        });
        return li;
    }

    @Override
    public String getLogFilePath(String strLogName )
    {
        StokerFile sf = m_fileLogList.get(strLogName);
        return sf.getFilePath();
    }
    @Override
    public String getLogFileName(String strLogName )
    {
        StokerFile sf = m_fileLogList.get(strLogName);
        return sf.getFileName();
    }
    @Override
    public boolean isLogRunning( String strLogName )
    {
       return m_fileLogList.containsKey(strLogName);
    }
    @Override
    public void startLog(String strCookerName, String strLogName) throws LogExistsException
    {
        LogItem li = new LogItem(strCookerName, strLogName, Calendar.getInstance().getTime());
        Log.info("Starting log: [" + strLogName + "]");
        startLog( li );
    }

    @Override
    public void startLog( LogItem logItem ) throws LogExistsException
    {
        if ( m_fileLogList.containsKey(logItem.getLogName())) throw new LogExistsException(logItem.getLogName());
        
        StokerFile sf = stokerFileProvider.get(); //new StokerFile( logItem );
        sf.init( logItem );  
     
        m_fileLogList.put( logItem.getLogName(), sf );
        Log.info("Starting log: [" + logItem.getLogName() + "]");
        sf.start();

    }

    @Override
    public String stopLog( String strLogName ) throws LogNotFoundException
    {
        // It is necessary to return the filename so the log can be requested once
        // it is complete.  Passing the logName is not enough since we are removing 
        // it from the hashmap below.
        
        String fileName = null;
        StokerFile sf = m_fileLogList.get(strLogName);
        if ( sf ==  null ) throw new LogNotFoundException( strLogName );
        fileName = sf.getFileName();
        sf.stop();
        if  ( m_fileLogList.remove(strLogName) == null )
            logger.error("log not found in hashmap");
        logger.info("Log stopped: " + strLogName );
        
        return fileName;
    }

    
    @Override
    public void stopAllLogs()
    {
       for ( StokerFile sf : m_fileLogList.values() )
       {
           sf.stop();
           m_fileLogList.remove(sf.getName());
           
       }
    }

    @Override
    public Integer attachToExistingLog( String cookerName, String selectedLog, String fileName )
    {
        // We are passed in a pruned filename, we need to lookup the full path.
        // to the given file.
        Integer returnVar = new Integer( 0 );

        StokerFile sf = m_fileLogList.get( selectedLog );
        if ( sf != null )
        {
           sf.attachToExistingLog( ListLogFiles.getFullPathForFile(fileName) );
           returnVar = new Integer(1);
        }
        else
        {
            if ( ! selectedLog.contains("Default"))
            {
                //String logName = fileName.substring(fileName.lastIndexOf("_")).replace(".log", "");
                StokerFile sfNew = stokerFileProvider.get(); // new StokerFile(cookerName, fileName );
                sfNew.init( cookerName, fileName );
                sfNew.start();
                logger.info("Attached to existing log file");

            }
        }

        return returnVar;

    }

    @Override
    public ArrayList<LogNote> getNotes(String logName)
    {
        return m_fileLogList.get( logName ).readAllNotes();
    }
    
    @Override
    public void addNoteToLog( String note, ArrayList<String> logList )
    {
        logger.info("Adding note to log: " + note );
        for ( String logName: logList )
        {
            StokerFile sf = m_fileLogList.get( logName );
            sf.addNote( note );
        }
    }
    
    @Subscribe
    public void detectConfigChange( ConfigChangeEvent cce )
    {
        // We need the configuration to do the following:
        // A.  Start a default log
        // B.  Detect changes in config to remove logs that have config changes 
        switch( cce.getEventType() )
        {
            case CONFIG_SAVED:
                rectifyLogs();
                break;
            case CONFIG_LOADED:
                logger.info("LogManagerImpl:detectConfigChange() CONFIG_LOADED");
                startDefaultLogs();
                break;
            case CONFIG_UPDATE_DETECTED:
                
                break;
            default:
        }
    }
    
    /**
     * Check for running logs that may no longer have a cooker named for them.
     * Configuration changes, manly cooker name changes, can orphan log files and make
     * them no longer accessible.  The method will also start a default log if none exists
     * for the cooker.
     */
    private void rectifyLogs()
    {
        HashMap<String,Cooker> hmCooker = new HashMap<String,Cooker>();
        for ( Cooker cooker : stokerWebConfiguration.getCookerList().getCookerList())
        {
            hmCooker.put( cooker.getCookerName(), cooker);
        }
        
       
        ArrayList<String> addList = new ArrayList<String>();
        
        for ( StokerFile f : m_fileLogList.values() )
        {
            String cookerName = f.getCookerName();
            if ( ! hmCooker.containsKey(cookerName))
            {
                try
                {
                    stopLog( f.getName() );
                }
                catch (LogNotFoundException e)
                {
                    // Doubt we'll ever get here...
                    e.printStackTrace();
                }
            }
           
            
        }
        
        for ( Cooker cooker : hmCooker.values() )
        {
            String strDefaultName = "Default_" + cooker.getCookerName();
            if ( ! m_fileLogList.containsKey( strDefaultName ))
            {
               startDefaultLog(cooker);    
            }
        }
        
        
        
    }
    
    private void startDefaultLogs()
    {
         for ( Cooker cooker : stokerWebConfiguration.getCookerList().getCookerList() )
         {
             startDefaultLog( cooker );
         }
    }
    
    private void startDefaultLog( Cooker cooker )
    {
        String strDefaultName = "Default_" + cooker.getCookerName();
        if ( ! isLogRunning(strDefaultName ))
        {
            try
            {
                LogItem li = new LogItem(cooker.getCookerName(), 
                                         strDefaultName,
                                         Calendar.getInstance().getTime(),
                                         CookerHelper.getDeviceList(cooker));
                startLog(li);
            }
            catch (LogExistsException e)
            {
                logger.error( "LogExistsException: [" + strDefaultName + "]");
                try { stopLog(strDefaultName); } catch (LogNotFoundException e1) { }
            }
        }
    }


    
  
}
