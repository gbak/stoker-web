package sweb.server.report;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

import sweb.server.report.JasperReportConstants.ReportConstants;

import net.sf.jasperreports.charts.JRXyDataset;
import net.sf.jasperreports.charts.JRXySeries;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDatasetRun;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRExpressionCollector;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.design.JRVerifier;
import net.sf.jasperreports.engine.type.IncrementTypeEnum;
import net.sf.jasperreports.engine.type.ResetTypeEnum;

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
    
   /* public Collection<Map<String,?>> processReport()
    {
        Collection<Map<String,?>> array1 = new ArrayList<Map<String,?>>();
        
        HashMap<String,Object> source = new HashMap<String,Object>();
        
        source.put(ReportConstants.LOG_NAME.toString(), new String("Report Title"));
        source.put("startDate", Calendar.getInstance().getTime());
        
        array1.add( source );
        return array1;
    }*/
}
