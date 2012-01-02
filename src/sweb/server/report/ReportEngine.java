package sweb.server.report;

import java.util.ArrayList;

import sweb.server.controller.data.DataOrchestrator;
import sweb.shared.model.SBlowerDataPoint;
import sweb.shared.model.SDataPoint;
import sweb.shared.model.SDevice;
import sweb.shared.model.logfile.LogNote;

// Report
// Log Name
// Log Duration
// NUmber of temp setting change

/*
 * Report Details:
 * 
 * Log Name
 * Log DUration
 * Number of temp setting changes
 * Total Fan Time
 * Total fan on/off cycles
 * 
 * Data graph
 * 
 * Action list
 * Columns:  Action, Time, text
 * 
 * Action Types (Config, note, ... )
 * 
 */
public class ReportEngine
{

     
    public ReportEngine()
    {
        
    }
    
    public void generateReport( String logName )
    {
        ArrayList<ArrayList<SDataPoint>> dataList = DataOrchestrator.getInstance().getAllDataPoints(logName);
        ArrayList<SDevice> configList = DataOrchestrator.getInstance().getConfigSettings(logName);
        ArrayList<LogNote> noteList = DataOrchestrator.getInstance().getNotes(logName);
        
        FanStats fanStats = calculateFanStats( dataList );
        long Duration = getDuration( dataList );
        
    }
    
    /**
     * Returns the time difference between the first point in the list to the last point.
     * @param dataList list of data points
     * @return long difference between time of fist point in list to the last
     */
    private long getDuration( ArrayList<ArrayList<SDataPoint>> dataList )
    {
        if ( dataList.size() == 0 )
            return 0;
        
        SDataPoint first = dataList.get(0).get(0);
        SDataPoint last = dataList.get(dataList.size() - 1).get(0);

        return last.getCollectedDate().getTime() - first.getCollectedDate().getTime();
    }
    
    private FanStats calculateFanStats( ArrayList<ArrayList<SDataPoint>> dataList )
    {
        FanStats fanStats = new FanStats();
        
        for ( ArrayList<SDataPoint> arsdp : dataList )
        {
            for ( SDataPoint sdp : arsdp )
            {
                if ( sdp instanceof SBlowerDataPoint )
                {
                        fanStats.addCycle((SBlowerDataPoint)sdp);
                }
            }
        }
        return fanStats;
    }
    
    private class FanStats
    {
        private long totalRunTime = 0;
        private long FanCycles = 0;
        private boolean lastCycle = false;
        private long startTime;
        
        
        public void addCycle( SBlowerDataPoint b )
        {
            if ( b.isFanOn() )
            {
                if ( lastCycle == false )
                {
                    FanCycles++;
                    startTime = b.getCollectedDate().getTime();
                }
            }
            else
            {
                if ( lastCycle == true )
                {
                    long endTime = b.getCollectedDate().getTime(); 
                    long totalTime = endTime = startTime;
                    totalRunTime = totalRunTime + totalTime;
                }
            }
            lastCycle = b.isFanOn();
        }
    }
}
