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

import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import net.sf.jasperreports.renderers.JCommonDrawableRenderer;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import com.gbak.sweb.server.report.JasperReportConstants.ReportConstants;


public class JFreeChartReportScriptlet extends JRDefaultScriptlet
{

    private static final Logger logger = Logger.getLogger(JFreeChartReportScriptlet.class.getName());

    public void setReportName(String strReportName)
    {
        logger.debug("In JFreeChartReportScriptlet: " + strReportName);
    }

    public void afterReportInit() throws JRScriptletException
    {
        final String chartTitle = null;

        for (String ks : this.parametersMap.keySet())
        {
            logger.debug("parametersMap Keyset: " + ks);
        }
        if (!this.parametersMap.containsKey("ChartDataSource"))
        {
            logger.error("Report doesn't have 'ChartDataSource' parameter!");
            return;
        }

        @SuppressWarnings("unchecked")
        HashMap<String, TimeSeriesCollection> mapTS = (HashMap<String, TimeSeriesCollection>) super
                .getParameterValue(ReportConstants.CHART_DATA_SOURCE.toString());
        final XYDataset dataset1 = (XYDataset) mapTS.get("axis1");
        final XYDataset dataset2 = (XYDataset) mapTS.get("axis2");
        
        if ( dataset1 == null )
           logger.warn("axis1 missing from graph");
        
        if ( dataset2 == null)
            logger.warn("Axis2, blower data is missing from graph");
        
        // final XYDataset dataset =
        // (XYDataset)super.getParameterValue(ReportConstants.CHART_DATA_SOURCE.toString()
        // );

        final JFreeChart chart = ChartFactory.createTimeSeriesChart(null, null, // "Date",
                null, 
                dataset1, true, true, false);

        final XYPlot plot = chart.getXYPlot();
        final SymbolAxis sa = new SymbolAxis("Blower", new String[]
                              { "OFF", "ON", "", "", "", "", "", "" });
        
        Font f = new Font("Lucida Sans",Font.PLAIN, 12);

        sa.setLabelFont(f);
        chart.getLegend().setItemFont(f);
        plot.setRangeAxis(1, sa);
        

        plot.setDataset(1, dataset2);
        plot.mapDatasetToRangeAxis(1, 1);
        XYStepRenderer xyr = new XYStepRenderer();

        xyr.setSeriesPaint(0, Color.black);
        plot.setRenderer(1, xyr);

        final DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("dd HH:mm"));

        this.setVariableValue("Chart", new JCommonDrawableRenderer(chart));
    }

}
