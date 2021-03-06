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

package com.gbak.sweb.server.log.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.log4j.Logger;


import com.gbak.sweb.server.StokerWebConstants;
import com.gbak.sweb.server.StokerWebProperties;
import com.gbak.sweb.server.events.ConfigChangeEvent;
import com.gbak.sweb.server.events.DataPointEvent;
import com.gbak.sweb.server.events.WeatherChangeEvent;
import com.gbak.sweb.server.log.file.LogFileFormatter.LineType;
import com.gbak.sweb.server.monitors.PitMonitor;
import com.gbak.sweb.shared.model.LogItem;
import com.gbak.sweb.shared.model.data.SBlowerDataPoint;
import com.gbak.sweb.shared.model.data.SDataPoint;
import com.gbak.sweb.shared.model.data.SProbeDataPoint;
import com.gbak.sweb.shared.model.devices.SDevice;
import com.gbak.sweb.shared.model.logfile.LogNote;
import com.gbak.sweb.shared.model.weather.WeatherData;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class StokerFile
{
    public static final String m_strLogExtension = ".log";

    public static String strFileNamePattern = "yyyyMMdd_HH_mm_";

    private String m_strCookerName = null;
    private String m_strLogName = null;
    private String m_strLogFileName = null;
    private File m_outfile = null;
    private HashMap<String,SDevice> m_hmSD = new HashMap<String,SDevice>();
    private HashMap<String,String> m_hmSDIndex = new HashMap<String,String>();
    private Date m_startDate = null;
    private PitMonitor m_pitMonitor;
    private EventBus eventBus;

    private static final Logger logger = Logger.getLogger(StokerFile.class.getName());
    
    
    public void init( String strCookerName,  String strLogFileName )
    {
        String file = ListLogFiles.getFullPathForFile(strLogFileName);
        init( strCookerName, 
              strLogFileName, 
              getStartDateFromExistingFile( file ),
              getConfigFromExistingFile( file ));
    }
   
    public void init( LogItem li )
    {
        init( li.getCookerName(), li.getLogName(), li.getStartDate(), li.getLogItems());
    }

    public void init( String strCookerName, String strLogName, Date startDate, ArrayList<SDevice> asd)
    {
        this.m_strCookerName = strCookerName;
        this.m_strLogName = strLogName;
        
        if ( startDate == null )
            this.m_startDate = Calendar.getInstance().getTime();
        else
            this.m_startDate = startDate;
        
        try
        {
            m_strLogFileName = strBuildFileName(strLogName);
            new File(dirName(m_strLogFileName)).mkdirs();
            m_outfile = new File( m_strLogFileName );


        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        // if no devices were set to be logged, log everything
        if ( asd == null)
        {
           asd = m_pitMonitor.getRawDevices();
        }

        int x = 1;

        for ( SDevice sd : asd )
        {
           Formatter f = new Formatter( Locale.US);

           m_hmSD.put( sd.getID(), sd);
           m_hmSDIndex.put(sd.getID(),f.format("%02d", x).toString() );
           x++;
        }

    }

    private String dirName( String s )
    {
        return s.substring(0, s.lastIndexOf(File.separatorChar));
    }

    public String getName()
    {
        return m_strLogName;
    }
    
    @Inject
    private StokerFile( PitMonitor pit, 
                        EventBus eventBus )
    {
        this.m_pitMonitor = pit;
        this.eventBus = eventBus;
        
        this.eventBus.register(this);
    }
    
    private void writeHeader()
    {
       Writer outputHeader = null;

        try
        {
            outputHeader = new BufferedWriter(new FileWriter(m_outfile, true));
            outputHeader.write(LogFileFormatter.generateLogHeader(m_strCookerName, new ArrayList<SDevice>(m_hmSD.values()), m_hmSDIndex));
            outputHeader.close();
        }
        catch (IOException e1)
        {
            logger.error("Exception writing log header: " +  e1.getStackTrace());
        }
    }

    @Subscribe
    public void handleDataPointEvent( DataPointEvent de )
    {
        synchronized (this)
        {
            Writer output = null;
            try
            {
                output = new BufferedWriter(new FileWriter(m_outfile,
                        true));

                ArrayList<SProbeDataPoint> arPDP = de
                        .getSProbeDataPoints();

                if (arPDP != null && arPDP.size() > 0
                        && de.isTimedEvent())
                {
                    output.write(LogFileFormatter.logDataDate(arPDP
                            .get(0).getCollectedDate()));

                    for (SProbeDataPoint sd : arPDP)
                    {
                        if (m_hmSD.get(sd.getDeviceID()) != null)
                        {
                            output.write(LogFileFormatter
                                    .logPointSeperator());
                            output.write(LogFileFormatter.logData(sd,
                                    m_hmSDIndex));
                        }
                    }
                    output.write(LogFileFormatter.logEnd());
                }

                // Blower device
                SBlowerDataPoint bdp = de.getSBlowerDataPoint();
                if (bdp != null && !de.isTimedEvent()) // don't log the blower timed events
                {
                    output.write(LogFileFormatter.logBlowerDate(bdp
                            .getCollectedDate()));

                    if (m_hmSD.get(bdp.getDeviceID()) != null)
                    {
                        output.write(LogFileFormatter
                                .logPointSeperator());
                        output.write(LogFileFormatter.logData(bdp,
                                m_hmSDIndex));
                    }
                    output.write(LogFileFormatter.logEnd());
                }

            }
            catch (IOException e)
            {
                logger.error("IOException writing to output file: " + e.getStackTrace());
            }
            finally
            {
                try
                {
                    output.close();
                }
                catch (IOException e)
                {
                    logger.error("Exception closing output file: " + e.getStackTrace());
                }
            }
        }
    }
    
    @Subscribe
    public void handleWeatherChangeEvent( WeatherChangeEvent wce)
    {
        synchronized (this)
        {
            Writer output = null;
            try
            {
                output = new BufferedWriter(new FileWriter(m_outfile,
                        true));

                WeatherData wd = wce.getWeatherData();
                output.write(LogFileFormatter.logWeatherDate(Calendar.getInstance().getTime()));
                output.write(LogFileFormatter.logPointSeperator());
                output.write(LogFileFormatter.logWeather(wd));
                output.write(LogFileFormatter.logEnd());

            }
            catch (IOException e)
            {
                logger.error("IOException writing to output file: " + e.getStackTrace());
            }
            finally
            {
                try
                {
                    output.close();
                }
                catch (IOException e)
                {
                    logger.error("Exception closing output file: " + e.getStackTrace());
                }
            }
        }
    }
    
    @Subscribe
    public void handleConfigChangeEvent( ConfigChangeEvent ce )
    {
        for ( String s : m_hmSD.keySet() )
        {
            SDevice sd = m_pitMonitor.getDeviceByID(s);
            if ( sd != null )
            {
                m_hmSD.put( s, sd);
            }
        }
        
        writeHeader();    
    }
    
    public void start()
    {
        writeHeader();
    }

    public void stop()
    {
        eventBus.unregister(this);
    }

    public String getCookerName()
    {
        return m_strCookerName;
    }

    public ArrayList<SDevice> getDeviceList()
    {
        return new ArrayList<SDevice>( m_hmSD.values());
    }

    private static String strBuildFileName( String strLogName )
    {
        Calendar cal = Calendar.getInstance();
        Format dateFormatter;

        if ( strLogName == null || strLogName.length() == 0)
            strLogName = "default";


        dateFormatter = new SimpleDateFormat(strFileNamePattern );
        String strFileName = dateFormatter.format(cal.getTime()) + strLogName.replace(" ", "_") + m_strLogExtension;

        // This should come out to cookLogs/2011/08
        String strDirPattern = "yyyy" +File.separator+ "MM";
        dateFormatter = new SimpleDateFormat( strDirPattern );
        String strDirName = dateFormatter.format( cal.getTime());

        StringBuilder sb = new StringBuilder();

        sb.append(StokerWebProperties.getInstance().getProperty(StokerWebConstants.PROPS_STOKERWEB_DIR));
        sb.append(File.separator);
        sb.append(StokerWebProperties.getInstance().getProperty(StokerWebConstants.PROPS_LOGS_DIR));  // Error here, null
        sb.append(File.separator);
        sb.append(StokerWebConstants.PATH_COOK_LOG);
        sb.append(File.separator);
        sb.append(strDirName);
        sb.append(File.separator);
        sb.append(strFileName);

        logger.info("Output log Filename: " + sb.toString());
        return sb.toString();
    }

    /**
     * Reads all data points from the the file and returns them.
     * @return Returns the data points for the same device in the same ArrayList.  This was
     * done to improve the performance when creating the graph.
     * @throws IOException
     */
    public ArrayList<ArrayList<SDataPoint>> readAllDataPoints() throws IOException
    {
        ArrayList<ArrayList<SDataPoint>> arDP = new ArrayList<ArrayList<SDataPoint>>();

        HashMap<String,ArrayList<SDataPoint>> hmDeviceSilos =  new HashMap<String,ArrayList<SDataPoint>>();

        // This instance knows the details of the file already,
        // we can start reading data points.
        HashMap<String,String> hm = new HashMap<String,String>();

        // Reverse the key value pairs in the hashmap so we can
        // lookup by the 2 digit number assigned to the deviceID
        for ( Entry<String, String> es : m_hmSDIndex.entrySet() )
        {
           hm.put( es.getValue(), es.getKey() );

           // Initializing this here saves some null/create logic in the loop
           hmDeviceSilos.put( es.getKey(),new ArrayList<SDataPoint>());
        }


        try
        {
            BufferedReader in = new BufferedReader(new FileReader(m_strLogFileName));
            String str;
            
            // Create a map to store the name of the probe as the log is read.  If a new config
            // line appears, then update the name in the map.
            HashMap<String,String> currentProbeNameMap = new HashMap<String,String>();
            
            while ((str = in.readLine()) != null) 
            {
                // Test the line type, if it is a config line, check to see if the
                // name of the probe, for the specific DeviceID is the same as it was 
                // before.  If it is not or the probe does not exist in the map,then add it.
                
                LineType type = LogFileFormatter.getLineType(str.substring(0, 2));
                if ( type == LineType.CONFIG && type != LineType.CONFIG )
                {
                    SDevice sdConfig = LogFileFormatter.parseLogConfigLine(str);
                    String name = currentProbeNameMap.get(sdConfig.getID());
                    if (( name == null) || (name.compareTo(sdConfig.getName()) != 0 ))
                        currentProbeNameMap.put( sdConfig.getID(), sdConfig.getName());
                }
                
                for ( SDataPoint sdp : LogFileFormatter.parseLogDataLine( str, hm ))
                {
                    sdp.setDeviceName( currentProbeNameMap.get( sdp.getDeviceID() ));
                    hmDeviceSilos.get(sdp.getDeviceID()).add(sdp);
                }
            }
            in.close();

        }
        catch (FileNotFoundException e)
        {
            logger.error("File Not found [" + m_strLogFileName + "] in readAllDataPoints: " + e.getStackTrace());
        }

        for ( ArrayList<SDataPoint> ar : hmDeviceSilos.values() )
        {
            arDP.add( ar );
        }

        return arDP;
    }

    public boolean compareLoosly( ArrayList<SDevice> arSD )
    {
        if ( arSD.size() != m_hmSD.size())
            return false;

        for ( SDevice sd : arSD )
        {
            if ( ! m_hmSD.containsKey( sd.getID() ))
            {
                return false;
            }

        }
        return true;
    }

    public void addNote( String note )
    {
        synchronized (this)
        {
            Writer output = null;
            try
            {
                output = new BufferedWriter(new FileWriter(m_outfile, true));

                output.write(LogFileFormatter.logNoteDate(Calendar.getInstance().getTime()));
                output.write(LogFileFormatter.logPointSeperator() + LogFileFormatter.logNote(note) );
                
            }
            catch (IOException e)
            {
                logger.error("IO Exception on file [" + m_outfile + "] in addNote: " + e.getStackTrace());
            }
            finally
            {
                try
                {
                    output.close();
                }
                catch (IOException e)
                {
                    logger.error("IO Exception closig file [" + m_outfile + "] in addNote: " + e.getStackTrace());
                }
            }
        }
        
    }
     
    public ArrayList<LogNote> readAllNotes()
    {
        ArrayList<LogNote> notes = new ArrayList<LogNote>();
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(m_strLogFileName));
            String str;
            while ((str = in.readLine()) != null) {
               
                LogNote note = LogFileFormatter.parseNoteLine( str);
                if ( note != null )
                    notes.add( note );
                
            }
            in.close();

        }
        catch (FileNotFoundException e)
        {
            logger.error("File Not found [" + m_strLogFileName + "] in readAllNotes: " + e.getStackTrace());
        }
        catch (IOException e)
        {
            logger.error("IO Exception on file [" + m_outfile + "] in readAllNotes: " + e.getStackTrace());
        }
        
        return notes;
    }
    
    public boolean attachToExistingLog( String fileName )
    {
        synchronized( this )
        {
            File tempfile = new File( fileName );
            if ( ! tempfile.exists() )
            {
                return false;
            }

            ArrayList<SDevice> arSD = getConfigFromExistingFile( fileName );

            // Check to make sure the configuration matches
            if ( ! compareLoosly(arSD))
            {
                logger.warn("StokerFile.java - attchToExistingLog() - Configuration does not match!");
                return false;
            }

            // Thinking about this further, this may not be required if
            // the graph supports adding or removing a series while the graph
            // is running, removing can be a simple as not adding any more data points
            // but adding a new series is the question.  It is definitely not
            // setup to do this now.

            m_outfile = tempfile;
            m_strLogFileName = fileName;
        }

        return true;
    }

    public Date getLogStartTime()
    {
       return m_startDate;    
    }
    
    public long getElapsedLogTimeMinutes()
    {
        Date now = Calendar.getInstance().getTime();
        long elapsedSeconds = now.getTime() - m_startDate.getTime();
        return elapsedSeconds;
    }
    
    public ArrayList<SDevice> getConfigFromFile()
    {
        return getConfigFromExistingFile(m_strLogFileName);
    }
    
    public static Date getStartDateFromExistingFile( String s )
    {
        Date d = null;
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(s));
            String str;
            while ((str = in.readLine()) != null)
            {
                LineType type = LogFileFormatter.getLineType(str.substring(0, 2));
                if ( type == LineType.DATA )
                {
                    d = LogFileFormatter.parseDateFromDataLine( str );
                    break;
                }
                
            }
            in.close();

        }
        catch (FileNotFoundException e)
        {
            logger.error("File Not found [" + s + "] in getStartDateFromExistingFile: " + e.getStackTrace());
        }
        catch (IOException e)
        {
            logger.error("IO Exception on file [" + s + "] in getStartDateFromExistingFile: " + e.getStackTrace());
        }
        return d;
    }
    public static ArrayList<SDevice> getConfigFromExistingFile( String s )
    {
        ArrayList<SDevice> arSD = new ArrayList<SDevice>();
        HashMap<String,SDevice> hmSD = new HashMap<String,SDevice>();
        
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(s));
            String str;
            while ((str = in.readLine()) != null)
            {
               // LogFileFormatter.parseLogDataLine( str, hm, arDP );
                SDevice sd = LogFileFormatter.parseLogConfigLine( str );
                
                // Log lines that are not config devices will return null
                // the ID of 0 is the cooker Name line.
                if ( sd != null && sd.getID().compareTo("0") != 0)
                {
                   hmSD.put( sd.getID(), sd);
                }
                
            }
            in.close();
            arSD = new ArrayList<SDevice>( hmSD.values());

        }
        catch (FileNotFoundException e)
        {
            logger.error("File Not found [" + s + "] in getConfigFromExistingFile: " + e.getStackTrace());
        }
        catch (IOException e)
        {
            logger.error("IO Exception on file [" + s + "] in getConfigFromExistingFile: " + e.getStackTrace());
        }

        return arSD;
    }

    public String getFilePath()
    {
        return m_strLogFileName;
    }
    
    public String getFileName()
    {
        return m_strLogFileName.substring(m_strLogFileName.lastIndexOf(File.separatorChar) + 1);
    }
    
 
    public void test()
    {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        // write c lines  ( cookers)
        for ( int i = 0; i < 5; i++ )
        {
            formatter.format("c|%2s|%s\n",i,"Name");



        }
        logger.debug("test String: " + sb.toString());


    }
    /*
    public static void main(String[] args)
    {
        StokerFile sf = new StokerFile();
        sf.strBuildFileName( "Gary" );
        sf.test();

    }
    */
}
