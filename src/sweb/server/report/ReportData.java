package sweb.server.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

import sweb.server.controller.Controller;
import sweb.server.controller.data.DataOrchestrator;
import sweb.server.controller.log.ListLogFiles;
import sweb.server.controller.log.LogFileFormatter;
import sweb.server.controller.log.LogFileFormatter.LineType;
import sweb.server.controller.log.exceptions.LogNotFoundException;
import sweb.server.controller.log.exceptions.LogReadErrorException;
import sweb.server.report.JasperReportConstants.ReportConstants;
import sweb.server.report.TableEntry.ActionType;
import sweb.shared.model.data.SBlowerDataPoint;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.logfile.LogNote;
import sweb.shared.model.stoker.StokerFan;
import sweb.shared.model.stoker.StokerPitSensor;
import sweb.shared.model.stoker.StokerProbe;

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
    
    
    String strLogFilePath = null;
    
    private static final Logger logger = Logger.getLogger(ReportData.class.getName());
    
    public ReportData( String name ) throws LogNotFoundException, LogReadErrorException
    {
        String strLogNameShort;
        
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
           strLogFilePath = Controller.getInstance().getDataOrchestrator().getLogFilePath(name);
           strLogNameShort = Controller.getInstance().getDataOrchestrator().getLogFileName(name);
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
                                sb.append(sp.getPrintString());
                                break;
                            
                            case PIT:
                                StokerPitSensor sps = (StokerPitSensor)sd;
                                sb.append( sps.getPrintString());
                                break;
                            
                            case BLOWER:
                                StokerFan sf = (StokerFan)sd;
                                sb.append( sf.getPrintString());     
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
                        
                    case DATA:
                        for ( SDataPoint sdp : LogFileFormatter.parseLogDataLine( str, hmByLogDevNumAndDeviceID ))
                        {
                             TimeSeries ts = mapProbeChartPoints.get(sdp.getDeviceID());
                             if ( ts == null )
                             {
                                 String deviceName = hmByDeviceIDAndSDevice.get(sdp.getDeviceID()).getName();
                                 logger.debug("Creating new TimeSeries for device: [" + deviceName + "]");
                                 ts = new TimeSeries(deviceName, Second.class );
                                 mapProbeChartPoints.put( sdp.getDeviceID(), ts);
                                 
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
                        SDevice sd = LogFileFormatter.parseLogConfigLine( str );
                        hmByLogDevNumAndDeviceID.put("1", sd.getID() );
                        hmByDeviceIDAndSDevice.put(sd.getID(), sd );
                        break;
                
                    case WEATHER:
                        
                        break;
                
                    case NOTE:
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
