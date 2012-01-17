package sweb.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.data.xy.XYDataset;

import sweb.server.report.JFreeChartReportScriptlet;
import sweb.server.report.ReportData;
import sweb.server.report.ReportDataSource;
import sweb.server.report.TableDataSource;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

public class ReportServlet extends HttpServlet
{
   private static final long serialVersionUID = 4044185269678824532L;

   protected void doGet(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
   {
      ServletOutputStream servletOutputStream = response.getOutputStream();
      InputStream reportStream = getServletConfig().getServletContext().getResourceAsStream(
            "/reports/CookReport.jasper");
      try
      {
          String queryString = request.getQueryString();
          String strReportName = queryString.substring(4);
          System.out.println("Report Name selected: " + strReportName );
          
          ReportData reportData = new ReportData( strReportName );
          

          // old code, remove
          //ReportDataSource rds = new ReportDataSource();
        //  JRDataSource dataSource = new JRMapCollectionDataSource( rds.processReport() );
          
          JRDataSource dataSource = new JRMapCollectionDataSource( reportData.getReportDataSource().getReportData() );

          /*
          TableDataSource tableData = new TableDataSource();
          XYDataset  chartDataSource = JFreeChartReportScriptlet.createDataset1();
          
          HashMap<String,Object> params = new HashMap<String,Object>();
          params.put( "TableDataSource", tableData);
          params.put( "ChartDataSource", chartDataSource);*/
   
         JasperRunManager.runReportToPdfStream(reportStream, servletOutputStream, reportData.getParams(), dataSource);
         response.setContentType("application/pdf");
         servletOutputStream.flush();
         servletOutputStream.close();
      }
      catch (Exception e)
      {
         // display stack trace in the browser
         StringWriter stringWriter = new StringWriter();
         PrintWriter printWriter = new PrintWriter(stringWriter);
         e.printStackTrace(printWriter);
         response.setContentType("text/plain");
         response.getOutputStream().print(stringWriter.toString());

      }
   }
}