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

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

import org.apache.log4j.Logger;

import sweb.server.report.ReportData;

public class ReportServlet extends HttpServlet
{
   private static final long serialVersionUID = 4044185269678824532L;
   private static final Logger logger = Logger.getLogger(ReportServlet.class.getName());

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
          logger.info("Report Name selected: " + strReportName );
          
          ReportData reportData = new ReportData( strReportName );
          
          JRDataSource dataSource = new JRMapCollectionDataSource( reportData.getReportDataSource().getReportData() );
          
          HashMap<String,Object> params = reportData.getParams();
          
          InputStream imgInputStream = getServletConfig().getServletContext().getResourceAsStream("/reports/stokerweb5.png"); 
          params.put( "StokerWebImage", imgInputStream );
          
   
         JasperRunManager.runReportToPdfStream(reportStream, servletOutputStream, params, dataSource);
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

         logger.error(e.getStackTrace());
      }
   }
}
