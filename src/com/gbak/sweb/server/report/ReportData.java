/**
 *  Stoker-web
 *
 *  Copyright (C) 2012  Gary Bak
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

package com.gbak.sweb.server.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.joda.time.Interval;
import org.joda.time.Period;

import com.gbak.sweb.server.log.LogManager;
import com.gbak.sweb.server.log.exceptions.LogNotFoundException;
import com.gbak.sweb.server.log.exceptions.LogReadErrorException;
import com.gbak.sweb.server.log.file.ListLogFiles;
import com.gbak.sweb.server.log.file.LogFileFormatter;
import com.gbak.sweb.server.log.file.LogFileFormatter.LineType;
import com.gbak.sweb.server.report.JasperReportConstants.ReportConstants;
import com.gbak.sweb.server.report.TableEntry.ActionType;
import com.gbak.sweb.shared.model.data.SBlowerDataPoint;
import com.gbak.sweb.shared.model.data.SDataPoint;
import com.gbak.sweb.shared.model.devices.SDevice;
import com.gbak.sweb.shared.model.devices.stoker.StokerFan;
import com.gbak.sweb.shared.model.devices.stoker.StokerPitProbe;
import com.gbak.sweb.shared.model.devices.stoker.StokerProbe;
import com.gbak.sweb.shared.model.logfile.LogNote;
import com.google.inject.Inject;


public class ReportData
{
    ArrayList<ArrayList<SDataPoint>> dataList = null;
    ArrayList<SDevice> configList = null;
    ArrayList<LogNote> noteList = null;

    ReportDataSource rds = new ReportDataSource();
    TableDataSource tableData = new TableDataSource();

    // This needs to be keyed by device ID.  Device name would be ideal, but some idiot may 
    // give a probe a duplicate name, if even possible on the stoker, can't take the chance.
    HashMap<String,TimeSeries> mapProbeChartPoints = new HashMap<String,TimeSeries>();
    
    String strBlowerID = new String();
    private LogManager logManager = null;
    
    String strLogFilePath = null;
    
    private static final Logger logger = Logger.getLogger(ReportData.class.getName());
    
    @Inject
    private ReportData(LogManager logManager ) 
    { 
        this.logManager = logManager;
        
    }
    
    public void init( String file ) throws LogNotFoundException, LogReadErrorException
    {
        String strLogNameShort;
        String name = "";
        try
        {
            name = URLDecoder.decode(file,"UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            
            e.printStackTrace();
        }
        
        logger.debug("ReportData: passed in log name: [" + name + "]");
        if ( name.endsWith(".log"))
        {
            logger.debug("log file ends in .log");
            strLogNameShort = name;
            strLogFilePath = ListLogFiles.getFullPathForFile( name );
            
        }
        else
        {
            logger.debug("Log file does not end in .log");
           strLogFilePath = logManager.getLogFilePath(name); //Controller.getInstance().getDataOrchestrator().getLogFilePath(name);
           strLogNameShort = logManager.getLogFileName(name); //Controller.getInstance().getDataOrchestrator().getLogFileName(name);
        }
        logger.debug("Full path is: " + strLogFilePath );
        logger.debug("Short log name: " + strLogNameShort );
        
        if ( strLogFilePath == null )
            throw new LogNotFoundException(strLogFilePath);
        
        rds.addReportValue(ReportConstants.LOG_NAME, strLogNameShort.substring(15));
        logger.debug("About to call processFile()");
        
        processFile( strLogFilePath );
        logger.debug("processFile complete" );
    }
    
    public ReportDataSource getReportDataSource()
    {
        return rds;
    }
    
    public HashMap<String,Object> getParams()
    {
        HashMap<String,Object> params = new HashMap<String,Object>();
        params.put( ReportConstants.TABLE_DATA_SOURCE.toString(), tableData);
        
        HashMap<String,TimeSeriesCollection> mapTS = new HashMap<String,TimeSeriesCollection>();
        
        TimeSeriesCollection tscAxis1 = new TimeSeriesCollection();
        TimeSeriesCollection tscAxis2 = new TimeSeriesCollection();
        
        if ( strBlowerID.length() == 0)
           logger.warn("No blower detected in log file.");
        
        for ( Entry<String,TimeSeries> set : mapProbeChartPoints.entrySet() )
        {
            if ( set.getKey().compareTo(strBlowerID) == 0 )
            {
               tscAxis2.addSeries( set.getValue());    
            }
            else
            {
               tscAxis1.addSeries( set.getValue() );
            }
        }
        /*for ( TimeSeries ts: mapProbeChartPoints.values())
        {
            tsc.addSeries(ts);
        }*/
        
        mapTS.put( "axis1", tscAxis1 );
        mapTS.put( "axis2", tscAxis2 );
        params.put( ReportConstants.CHART_DATA_SOURCE.toString(), mapTS );  // create TimeSeriesCollection and add TimeSeries to it
        
        return params;
    }
    
    private void processFile( String logFilePath ) throws LogReadErrorException, LogNotFoundException
    {
        logger.debug("processFile : [" + logFilePath + "]");
        
        File tempfile = new File( logFilePath );
        if ( ! tempfile.exists() )
        {
            throw new LogReadErrorException( logFilePath );
        }
        
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(logFilePath));
            String str;
            HashMap<String,String> hmByLogDevNumAndDeviceID = new HashMap<String,String>();
            HashMap<String,SDevice> hmByDeviceIDAndSDevice = new HashMap<String,SDevice>();
            boolean bFirstDPFound = false;
            Date endDate = null;
            Date startDate = null;
            LineType lastType = null;
            long fanStart = 0;
            long fanAccumulator = 0;
            int fanCycle = 0;
            
            while ((str = in.readLine()) != null)
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug("line read from file: [" + str + "]");   
                }
                
                // This section before the case statement is for collecting the config information that
                // was previously collected in the case.  We run through the file and when a CONFIG
                // is detected it reads and then once a non config point is reached this code
                // is executed and processes the config.
                
               StringBuilder sb = new StringBuilder();
                LineType type = LogFileFormatter.getLineType(str.substring(0, 2));
                if ( lastType == LineType.CONFIG && type != LineType.CONFIG )
                {
                    for ( SDevice sd : hmByDeviceIDAndSDevice.values())
                    {
                        if ( sd.getID().compareToIgnoreCase("0") == 0 )   // TODO: This is the cooker name, it needs to be added to the report.
                            continue;  
                        
                        switch (sd.getProbeType())
                        {
                            case FOOD:
                                StokerProbe sp = (StokerProbe)sd;
                                sb.append(sp.printString());
                                break;
                            
                            case PIT:
                                StokerPitProbe sps = (StokerPitProbe)sd;
                                sb.append( sps.printString());
                                break;
                            
                            case BLOWER:
                                StokerFan sf = (StokerFan)sd;
                                sb.append( sf.printString());     
                                break;
                        }
                        
                    }

                    TableEntry te = new TableEntry(ActionType.CONFIG, endDate, sb.toString());
                    tableData.addTableEntry(te);
                }
                
                lastType = type;
                switch ( type )
                {
                    case BLOWER:  
                        logger.debug("processFile: type=BLOWER"); 
                        // Yes this is kind of redundant, but it keeps it out of the data section.
                        for ( SDataPoint sdp : LogFileFormatter.parseLogDataLine( str, hmByLogDevNumAndDeviceID ))
                        {
                            if ( strBlowerID.length() == 0 )
                            {
                                // save the blower ID so it can be identified and the Data points can be
                                // split out of the master hash map for a new graph axis.  getParams() call above.
                                strBlowerID = sdp.getDeviceID();
                            }
                            
                           SBlowerDataPoint sbdp = (SBlowerDataPoint) sdp;   
                           if ( sbdp.isFanOn() )
                           {
                               fanStart = sbdp.getCollectedDate().getTime();
                           }
                           else
                           {
                               fanCycle++;
                               long seconds = sbdp.getCollectedDate().getTime() - fanStart;
                               fanAccumulator = fanAccumulator + seconds  ;
                           }
                        
                        }
                        break;
                        
                    case DATA:
                        logger.debug("processFile: type=DATA");
                        for ( SDataPoint sdp : LogFileFormatter.parseLogDataLine( str, hmByLogDevNumAndDeviceID ))
                        {

                            String deviceID = sdp.getDeviceID();
                            logger.debug("deviceID: " + deviceID );
                            
                            SDevice device1 = hmByDeviceIDAndSDevice.get( deviceID);
                            logger.debug("device1 is null?: " + device1 == null ? "true" : "false" );
                            
                            String deviceName = device1.getName();
                            logger.debug("device1 name: " + device1.getName() );
                            
                             TimeSeries ts = mapProbeChartPoints.get(sdp.getDeviceID() + deviceName);
                             if ( ts == null )
                             {
                                 
                                 logger.debug("Creating new TimeSeries for device: [" + deviceName + "]");
                                 ts = new TimeSeries(deviceName, Second.class );
                                 mapProbeChartPoints.put( sdp.getDeviceID() + deviceName, ts);
                                 
                             }
                             RegularTimePeriod s = new Second( sdp.getCollectedDate() );
                             try
                             {
                                ts.add( s, sdp.getData() );
                             }
                             catch ( org.jfree.data.general.SeriesException  se )
                             {
                                 ts.update( s, sdp.getData());
                                 logger.warn("Overwriting point in graph data");
                             }
                        
                             if ( bFirstDPFound == false)
                             {
                                 startDate = sdp.getCollectedDate();
                                 fanStart = startDate.getTime();  // this is needed in case the fan is running when 
                                                                  // stokerweb comes online.  
                                 rds.addReportValue(ReportConstants.START_DATE, startDate );
                                 
                                 bFirstDPFound = true;
                             }
                             endDate = sdp.getCollectedDate();
                        }
                        
                        break;
                    
                    case CONFIG:
                        logger.debug("processFile: type=CONFIG");
                        SDevice sd = LogFileFormatter.parseLogConfigLine( str );
                        String deviceNumInLog = LogFileFormatter.getDeviceNumber(str);
                        hmByLogDevNumAndDeviceID.put(deviceNumInLog, sd.getID() );
                        hmByDeviceIDAndSDevice.put(sd.getID(), sd );
                        break;
                
                    case WEATHER:
                        logger.debug("processFile: type=WEATHER");
                        break;
                
                    case NOTE:
                        logger.debug("processFile: type=NOTE");
                        LogNote note = LogFileFormatter.parseNoteLine( str);
                        if ( note != null )
                        {
                            TableEntry te = new TableEntry( ActionType.NOTE, note.getNoteDate(), note.getNote());
                            tableData.addTableEntry( te );
                        }
                            
                        break;
                }
                SDevice sd = LogFileFormatter.parseLogConfigLine( str );
                
            }
            in.close();
            
            // Cook Duration:
            if ( endDate != null && startDate != null )
            {
                rds.addReportValue(ReportConstants.END_DATE, endDate );
                Interval interval = new Interval(startDate.getTime(), endDate.getTime());
                Period period = interval.toPeriod();
                StringBuilder sbInterval = new StringBuilder();
                int year = period.getYears();
                int month = period.getMonths();
                int days = period.getDays();
                int hours = period.getHours();
                int minutes = period.getMinutes();
                int seconds = period.getSeconds();
                
                if ( days == 1)
                    sbInterval.append(days + " Day ");
                else if ( days > 1)
                    sbInterval.append(days + " Days ");
                
                if ( hours == 1 )
                   sbInterval.append(hours + " Hour " );
                else if ( hours > 1)
                    sbInterval.append(hours + " Hours " );
                
                if ( minutes == 1 )
                    sbInterval.append(minutes + " Minute ");
                else if ( minutes > 1 )
                    sbInterval.append(minutes + " Minutes ");
                
                if ( days == 0 && hours == 0 && minutes == 0 && seconds > 0)  //  why not, just to prevent confusion
                    sbInterval.append(seconds + " Seconds ");
                
                rds.addReportValue(ReportConstants.COOK_DURATION, sbInterval.toString());
            }
            
            // Number of fan cycles
            rds.addReportValue( ReportConstants.FAN_CYCLES, new Integer(fanCycle));
            
            // fan Run time statistics
            StringBuilder sbFan = new StringBuilder();
            Interval fanInterval = new Interval( (long)0, fanAccumulator );
            Period fanPeriod = fanInterval.toPeriod();
            int fanDays = fanPeriod.getDays();
            int fanHour = fanPeriod.getHours();
            int fanMinutes = fanPeriod.getMinutes();
            int fanSeconds = fanPeriod.getSeconds();

            if ( fanDays == 1)
                sbFan.append(fanDays + " Day ");
            else if ( fanDays > 1 )
                sbFan.append(fanDays + " Days ");
            
             if ( fanHour == 1 )
                 sbFan.append(fanHour + " Hour " );
             else if ( fanHour > 1 )
                 sbFan.append(fanHour + " Hours " );
             
             if ( fanMinutes == 1 )
                 sbFan.append(fanMinutes + " Minute ");
             else if ( fanMinutes > 1 )
                 sbFan.append(fanMinutes + " Minutes ");
             
             if (  fanSeconds > 0) 
                 sbFan.append(fanSeconds + " Seconds ");
             
            rds.addReportValue( ReportConstants.FAN_TIME, sbFan.toString());

        }
        catch (FileNotFoundException e)
        {
            logger.error("Log file not found: [" + logFilePath + "], Exception: " + e.getStackTrace());
            throw new LogNotFoundException( logFilePath );
        }
        catch (IOException e)
        {
            logger.error("IO Exception reading log file: [" + logFilePath + "], Exception: " + e.getStackTrace());
        }   
    }
}
