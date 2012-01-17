package sweb.server.report;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.Rotation;


import sweb.server.report.JasperReportConstants.ReportConstants;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import net.sf.jasperreports.renderers.JCommonDrawableRenderer;

public class JFreeChartReportScriptlet extends JRDefaultScriptlet
{


   /**
    *
    */
   public void setReportName(String strReportName)
   {
       System.out.println("In JFreeChartReportScriptlet: " + strReportName );
   }
    
   public void afterReportInit() throws JRScriptletException
   {
      final String chartTitle = null;
     // final XYDataset dataset = createDataset1();
      for ( String ks : this.parametersMap.keySet() )
      {
          System.out.println("parametersMap Keyset: " + ks );
      }
      if (!this.parametersMap.containsKey("ChartDataSource")) {
          System.err.println("Report doesn't have 'ChartDataSource' parameter!");
          return;
          }
      
      HashMap mapTS = (HashMap<String, TimeSeriesCollection>)super.getParameterValue(ReportConstants.CHART_DATA_SOURCE.toString() );
      final XYDataset dataset1 = (XYDataset) mapTS.get( "axis1");
      final XYDataset dataset2 = (XYDataset) mapTS.get("axis2");
     // final XYDataset dataset = (XYDataset)super.getParameterValue(ReportConstants.CHART_DATA_SOURCE.toString() );
      
       
      final JFreeChart chart = ChartFactory.createTimeSeriesChart(
          null, 
          null, //"Date", 
          null, //"Price Per Unit",
          dataset1, 
          true, 
          true, 
          false
      );

     
      
//      final StandardLegend legend = (StandardLegend) chart.getLegend();
  //    legend.setDisplaySeriesShapes(true);
      
      final XYPlot plot = chart.getXYPlot();
     // final NumberAxis axis2 = new NumberAxis("Blower");
     // axis2.setAutoRangeIncludesZero(false);
      final SymbolAxis sa = new SymbolAxis("Blower",
              new String[]{"OFF","ON","","","","","",""});
      
      
      //axis2.setDomainAxis( sa );
     // TickUnits tu = new TickUnits();
     // tu.add(new Tick)
      
      //axis2.setRange(0, 8);
      //plot.setRangeAxis(1, axis2);
     plot.setRangeAxis(1, sa );
      
      plot.setDataset(1, dataset2);
      plot.mapDatasetToRangeAxis(1, 1);
      XYStepRenderer xyr = new XYStepRenderer();
      
      xyr.setSeriesPaint(0, Color.black);
      plot.setRenderer(1, xyr);
      
     // final XYItemRenderer renderer = plot.getRenderer();
  //    renderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
  //    if (renderer instanceof StandardXYItemRenderer) {
  //        final StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
      //    rr.setPlotShapes(true);
     //     rr.setShapesFilled(true);
   //   }
      
   //  final StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
    //  renderer2.setSeriesPaint(0, Color.black);
   ////   renderer2.setPlotShapes(true);
   //   renderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
   //   plot.setRenderer(1, renderer2);
      
      final DateAxis axis = (DateAxis) plot.getDomainAxis();
      axis.setDateFormatOverride(new SimpleDateFormat("dd HH:mm"));
      
      /*   */
      this.setVariableValue("Chart", new JCommonDrawableRenderer(chart));
   }
   
}
