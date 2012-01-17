package sweb.server.report;

public class JasperReportConstants
{

   // public static final String LOG_NAME = "logName";
    
    public enum ReportConstants 
    {
        LOG_NAME { public String toString() { return "logName"; } },
    
        START_DATE { public String toString() { return "startDate"; } },
    
        END_DATE { public String toString() {   return "endDate"; } },
    
        COOK_DURATION { public String toString() { return "cookDuration"; } },
    
        FAN_CYCLES { public String toString() { return "fanCycles"; } },
    
        FAN_TIME { public String toString() { return "fanTime"; } },
        
        TABLE_DATA_SOURCE { public String toString() { return "TableDataSource"; } },
        
        CHART_DATA_SOURCE { public String toString() { return "ChartDataSource"; } }
        
   }
    
}
