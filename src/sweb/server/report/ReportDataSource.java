package sweb.server.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import sweb.server.report.JasperReportConstants.ReportConstants;

public class ReportDataSource
{
    private HashMap<String,Object> reportData = new HashMap<String,Object>();
    
    public void addReportValue( ReportConstants rc, Object value )
    {
        reportData.put(rc.toString(), value);
    }
    
    public Collection<Map<String,?>> getReportData()
    {
        Collection<Map<String,?>> array1 = new ArrayList<Map<String,?>>();
        array1.add( reportData );
        return array1;
    }
}
