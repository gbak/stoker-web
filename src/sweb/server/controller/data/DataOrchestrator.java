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

package sweb.server.controller.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import sweb.server.controller.Controller;
import sweb.server.controller.events.BlowerEvent;
import sweb.server.controller.events.BlowerEventListener;
import sweb.server.controller.events.DataControllerEvent;
import sweb.server.controller.events.DataControllerEventListener;
import sweb.server.controller.events.DataPointEvent;
import sweb.server.controller.events.DataPointEventListener;
import sweb.server.controller.log.ListLogFiles;
import sweb.server.controller.log.StokerFile;
import sweb.server.controller.log.exceptions.LogExistsException;
import sweb.server.controller.log.exceptions.LogNotFoundException;
import sweb.shared.model.LogItem;
import sweb.shared.model.SBlowerDataPoint;
import sweb.shared.model.SDataPoint;
import sweb.shared.model.SDevice;
import sweb.shared.model.SProbeDataPoint;

/*
 *   Class to pull all stoker messages and persist data
 */

public class DataOrchestrator
{
    private volatile static DataOrchestrator sds = null;
 //   BlockingDeque<SDataPoint> deqStokerMessages = new LinkedBlockingDeque<SDataPoint>();
    HashMap<String,SDataPoint> hmLatestData = new HashMap<String,SDataPoint>();


    HashMap<String,StokerFile> fileLogList = new HashMap<String,StokerFile>();
    ArrayList<BlowerEventListener> m_arListener = new ArrayList<BlowerEventListener>();
    Set<DataPointEventListener> m_dpListener = new HashSet<DataPointEventListener>();

    Timer updateTimer = new Timer();

    private DataOrchestrator()
    {
        RunTimer _runTimer = new RunTimer();
        updateTimer.schedule( _runTimer, 10000 );
    }

    public static DataOrchestrator getInstance()
    {
        if ( sds == null)
        {
            synchronized ( DataOrchestrator.class)
            {
                if ( sds == null )
                {
                    sds = new DataOrchestrator();
                }
            }
        }
        return sds;
    }

    private class RunTimer extends TimerTask
    {
       public void run()
       {
          Calendar c = Calendar.getInstance();

          DataPointEvent be = new DataPointEvent(this, true );
          for ( SDataPoint sdp : getLastDPs())
          {
              be.addDataPoint(sdp);
          }
          // TODO: decide if to fire the event if there are no data points!
          fireStateChange(be);

          c.add(Calendar.MINUTE,1);
          c.set(Calendar.SECOND, 0);
          updateTimer.schedule( new RunTimer(), c.getTime() );
       }
    }

    public void addDataPoint( SDataPoint dp)
    {

        SDataPoint last =  hmLatestData.get(dp.getDeviceID());
        if ( last!= null)
        {
            if ( last.compare(dp) == false )
            {
                DataPointEvent be = new DataPointEvent(this, false, dp );
                fireStateChange(be);
            }
        }


        hmLatestData.put(dp.getDeviceID(),dp);
    }

    public ArrayList<SDataPoint> getLastDPs()
    {
        ArrayList<SDataPoint> ar = null;

        // The Client should not be checking for data if the controller is down, but just in case.
        if ( Controller.getInstance().isDataControllerReady())
        {
           ar = new ArrayList<SDataPoint>(hmLatestData.values());
        }
        else
           ar = new ArrayList<SDataPoint>();

        return ar;
    }

    /**
     * Return all the data points for the given log name
     * @param logName name of log to retrieve data points for
     * @return
     */
    public ArrayList<ArrayList<SDataPoint>> getAllDataPoints(String logName )
    {
        try
        {
            if ( logName == null || logName.length() == 0)
                logName = "Default";

            return fileLogList.get(logName).readAllDataPoints();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new ArrayList<ArrayList<SDataPoint>>();
    }

    public Set<Entry<String, SDataPoint>> getData()
    {
        return Collections.unmodifiableSet(hmLatestData.entrySet());
    }

    public ArrayList<LogItem> getLogList()
    {
       // return new ArrayList<String>( fileLogList.keySet());
        ArrayList<LogItem> li = new ArrayList<LogItem>();

        for ( StokerFile sf : fileLogList.values() )
        {
            LogItem l = new LogItem(sf.getCookerName(),sf.getName(), sf.getDeviceList());
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

    public boolean isLogRunning( String strLogName )
    {
       return fileLogList.containsKey(strLogName);
    }

    public void startLog(String strCookerName, String strLogName) throws LogExistsException
    {
        LogItem li = new LogItem(strCookerName, strLogName);
        startLog( li );
        //if ( fileLogList.containsKey(strLogName)) throw new LogExistsException(strLogName);
        //StokerFile sf = new StokerFile( strLogName );
        //fileLogList.put( strLogName, sf );
        //sf.start();
    }
    /*
    public void startLog( String strLogName, ArrayList<SDevice> asd ) throws LogExistsException
    {
        if ( fileLogList.containsKey(strLogName)) throw new LogExistsException(strLogName);
        StokerFile sf = new StokerFile( strLogName, asd );
        fileLogList.put( strLogName, sf );
        sf.start();

    }
    */

    public void startLog( LogItem logItem ) throws LogExistsException
    {
        if ( fileLogList.containsKey(logItem.getLogName())) throw new LogExistsException(logItem.getLogName());
        StokerFile sf = new StokerFile( logItem );
        fileLogList.put( logItem.getLogName(), sf );
        sf.start();

    }

    public void stopLog( String strLogName ) throws LogNotFoundException
    {
        StokerFile sf = fileLogList.get(strLogName);
        if ( sf ==  null ) throw new LogNotFoundException( strLogName );
        fileLogList.remove(sf);
        sf.stop();
    }

    public void stopAllLogs()
    {
       for ( StokerFile sf : fileLogList.values() )
       {
           sf.stop();
       }
    }

    public Integer attachToExistingLog( String cookerName, String selectedLog, String fileName )
    {
        // We are passed in a pruned filename, we need to lookup the full path.
        // to the given file.
        Integer returnVar = new Integer( 0 );

        StokerFile sf = fileLogList.get( selectedLog );
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
                StokerFile sfNew = new StokerFile(cookerName, fileName );
                sfNew.start();

            }
        }

        return returnVar;

    }

    protected void fireStateChange( BlowerEvent be )
    {
        for ( BlowerEventListener listener : m_arListener )
        {
            listener.stateChange(be);
        }
    }


    public void addListener( BlowerEventListener bel )
    {
        m_arListener.add( bel );
    }

    protected void fireStateChange( DataPointEvent dpe )
    {
       synchronized ( this )
       {
           for ( DataPointEventListener listener : m_dpListener )
           {
               // Store the desired listener type ALL, UPDATED, TIMED
               // in the listener object,
   
               listener.stateChange(dpe);
           }
       }
    }


    public void addListener( DataPointEventListener dpe )
    {
       synchronized ( this )
       {
           m_dpListener.add( dpe );
       }
    }

    public void removeListener(DataPointEventListener dpe)
    {
       synchronized ( this )
       {
          for ( DataPointEventListener d : m_dpListener )
          {
              if ( d == dpe )
              {
                  m_dpListener.remove(d);
              }
          }
       }
    }

}
