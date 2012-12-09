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

package com.gbak.sweb.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

import org.apache.log4j.Logger;

import com.gbak.sweb.server.report.ReportData;
import com.google.inject.Inject;
import com.google.inject.Provider;


public class ReportServlet extends HttpServlet
{
   private static final long serialVersionUID = 4044185269678824532L;
   private static final Logger logger = Logger.getLogger(ReportServlet.class.getName());

   private Provider<ReportData> reportDataProvider;
   
   @Inject
   ReportServlet(Provider<ReportData> reportDataProvider)
   {
      this.reportDataProvider = reportDataProvider;
      
   }
   
   
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
   {
      ServletOutputStream servletOutputStream = response.getOutputStream();
     // ServletContext sc = getServletConfig().getServletContext();
      
      InputStream reportStream = compileJasperReport();
      
      //InputStream reportStream = getServletConfig().getServletContext().getResourceAsStream(
      //        "/reports/CookReport.jasper");
      
      try
      {
          String queryString = request.getQueryString();
          String strReportName = queryString.substring(8);
          logger.info("Report Name selected: " + strReportName );
          
          //ReportData reportData = new ReportData( strReportName );
          ReportData reportData = reportDataProvider.get();
          reportData.init( strReportName );
          
          JRDataSource dataSource = new JRMapCollectionDataSource( reportData.getReportDataSource().getReportData() );
          
          HashMap<String,Object> params = reportData.getParams();
          
          InputStream imgInputStream = getServletConfig().getServletContext().getResourceAsStream("/reports/stokerweb5.png"); 
          params.put( "StokerWebImage", imgInputStream );
          
         logger.info("Generating Report for Log: " + strReportName );
         JasperRunManager.runReportToPdfStream(reportStream, servletOutputStream, params, dataSource);
         logger.info("Report generation complete, attempting to stream report");
         response.setContentType("application/pdf");
         logger.debug("Flushing output stream");
         servletOutputStream.flush();
         logger.debug("Flush complete ");
         servletOutputStream.close();
         logger.debug("report output stream closed");
      }
      catch (Exception e)
      {
         // display stack trace in the browser
         StringWriter stringWriter = new StringWriter();
         PrintWriter printWriter = new PrintWriter(stringWriter);
         e.printStackTrace(printWriter);
         response.setContentType("text/plain");
         response.getOutputStream().print(stringWriter.toString());

         logger.error("Caught exception in ReportServlet:doGet()");
         logger.error(e.getStackTrace());
      }
   }
   
   private InputStream compileJasperReport()
   {
    // Set class path for compiling XML templates
       System.setProperty(
           "jasper.reports.compile.class.path",
           getServletConfig().getServletContext().getRealPath("/WEB-INF/lib/jasperreports-4.5.0.jar")
               + System.getProperty("path.separator")
               + getServletConfig().getServletContext().getRealPath("/WEB-INF/classes/"));
       
    // Specify a default folder for storing
    // compiled XML templates
     //   System.setProperty(
     //       "jasper.reports.compile.temp",
     //       sc.getRealPath("/reports/"));
       
       String stokerWebDir = StokerWebProperties.getInstance().getProperty(StokerWebConstants.PROPS_STOKERWEB_DIR);
       logger.debug("Setting property: jasper.report.compile.temp to: [" + stokerWebDir + "]");
       System.setProperty("jasper.reports.compile.temp", stokerWebDir );
       
       String jasperFile = null;
        try
        {
            InputStream JRXML = getServletConfig().getServletContext().getResourceAsStream("/reports/CookReport.jrxml");
            String copyFile = stokerWebDir + File.separator + "CookReport.jrxml";
            jasperFile = stokerWebDir + File.separator + "CookReport.jasper";
            
            copyInputStreamToFile( JRXML, copyFile );
            
            logger.debug("Compiling report file: [" + copyFile + "]");
            logger.debug("Report output file: [" + jasperFile + "]");
            
            // this will speed up report creation, but may cause problems if the jasper file is stale
       //     if ( ! new File(jasperFile).exists())
            {
               JasperCompileManager.compileReportToFile(copyFile, jasperFile);
            }
        }
        catch (JRException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
      // return getServletConfig().getServletContext().getResourceAsStream(jasperFile);
        InputStream is = null;
        try
        {
            is =   new BufferedInputStream( new FileInputStream(jasperFile));
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
       return is;
   }
   
   private void copyInputStreamToFile( InputStream is, String fileName )
   {
       logger.debug("Copying report jrxml file from archive");
        try
        {
            File f = new File(fileName);
            OutputStream out = new FileOutputStream(f);
            byte buf[] = new byte[2048];
            int len;
            while ((len = is.read(buf)) > 0)
                out.write(buf, 0, len);
            out.close();
            is.close();
        }
        catch (IOException e)
        {
            logger.error("IO Exception while copying report file from archive");
        }
        logger.debug("Copy complete");
    }
}
