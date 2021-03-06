/**
 *  Stoker-web
 *
 *  Copyright (C) 2011  Gary Bak
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

package com.gbak.sweb.client.graph;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.Labels;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.SplinePlotOptions;



import com.gbak.sweb.shared.model.data.SDataPoint;
import com.gbak.sweb.shared.model.devices.SDevice;
import com.gbak.sweb.shared.model.devices.stoker.StokerFan;
import com.gbak.sweb.shared.model.stoker.StokerDeviceTypes.DeviceType;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;


public class HighChartLineGraph extends StokerLineGraph

{
    private HashMap<String,Series>  mapSeries = new HashMap<String,Series>();
    String fanID = new String();
  
    final Chart chart = new Chart();


    private HighChartLineGraph()
    {

    }
    
    public static native void setHighchartTimezone() /*-{
    $wnd.Highcharts.setOptions({
        global: {
           useUTC: false,        
         
           }
           
         });
    }-*/;

    public void setNewSize(int width, int height)
    {
        chart.setSize( width, height, false );
    }
    
    public HighChartLineGraph(int iWidth, int iHeight, ArrayList<SDevice> listDeviceList, ArrayList<ArrayList<SDataPoint>> initData )
    {
        
       HashMap<String,SDevice> deviceMap = new HashMap<String,SDevice>();
       for ( SDevice device: listDeviceList )
       {
           deviceMap.put( device.getID(), device);
       }
       
        chart.setZoomType(Chart.ZoomType.X);
        chart.setToolTip(new ToolTip().setShared(true));
        //chart.setLegend(new Legend().setEnabled(false));
        chart.setLegend(new Legend()
            .setAlign(Legend.Align.CENTER)
            .setVerticalAlign(Legend.VerticalAlign.TOP)
            .setY(0)
            .setFloating(false)
            .setBorderWidth(0)
           );
        chart.setHeight(iHeight - 5);
        chart.setWidth( iWidth );
        
        chart.setChartTitleText(null);

        chart.setReflow(true);
        
      //  chart.setHeight100();
        //chart.setHeight(400);
      ///  chart.setWidth100( );
   
        setHighchartTimezone();

      // chart.setOption("/global/useUTC", "false");

        chart.setSplinePlotOptions(new SplinePlotOptions()
                .setLineWidth(2)
                .setHoverStateLineWidth(3)
                .setMarker(new Marker()
                    .setEnabled(false)
                    .setHoverState(new Marker()
                        .setEnabled(true)
                        .setSymbol(Marker.Symbol.CIRCLE)
                        .setRadius(5)
                        .setLineWidth(1)
                    )
                ));
        
        chart.setLinePlotOptions(new LinePlotOptions()
                .setLineWidth(1)
                .setHoverStateLineWidth(2)
                .setMarker(new Marker()
                   .setEnabled(false)
                   .setHoverState(new Marker()
                        .setEnabled(true)
                        .setSymbol(Marker.Symbol.DIAMOND)
                        .setRadius(3)
                        .setLineWidth(1)
                        )
                 ));
        chart.setToolTip(new ToolTip()
                .setFormatter(new ToolTipFormatter() {
                    public String format(ToolTipData toolTipData) {
                        return  DateTimeFormat.getFormat("MMMM d HH:mm:ss").format( new Date(toolTipData.getXAsLong()) ) + "<br/><b>" +
                                 toolTipData.getSeriesName() + ": " + NumberFormat.getFormat("###.#").format(toolTipData.getYAsDouble()) +
                                ("Temperature".equals(toolTipData.getSeriesName()) ? "°F" : " ") + "</b>";
                    }
                })
            )  ;
        chart.getXAxis()
            .setType(Axis.Type.DATE_TIME)
          //  .setMaxZoom(14 * 24 * 3600000) // fourteen days
            .setAxisTitleText(null);

        chart.getYAxis(0)
            .setAxisTitleText("Temperature")
            .setMin(0.6)
            .setStartOnTick(false)
            .setShowFirstLabel(false)
          //  .setPlotLines( chart.getYAxis(0).createPlotLine( ).setValue(190))
            .setLabels(new YAxisLabels()
                .setAlign(Labels.Align.LEFT)
                .setX(3)
                .setY(16)
                .setFormatter(new AxisLabelsFormatter() {
                    public String format(AxisLabelsData axisLabelsData) {
                        return NumberFormat.getFormat("###").format(axisLabelsData.getValueAsDouble());
                    }
                }))  ;

        
        chart.getYAxis(1)
            .setAxisTitleText("Blower")
            //.setMin(0.6)
            .setOpposite(true)
            .setGridLineWidth(0)
            .setStartOnTick(false)
            .setShowFirstLabel(false)
            
            .setMax(20)
             .setLineWidth(1)
             
            .setLabels(new YAxisLabels().setEnabled(false));
        
        
        for ( ArrayList<SDataPoint> dpList : initData)
        {
            if ( dpList.size() > 0 )
            {
                String deviceID = dpList.get(0).getDeviceID();
                SDevice sd = deviceMap.get( deviceID );
                Series s = chart.createSeries();
                s.setPoints( convertToArray(dpList));
                addNewSeries( sd,s );
                deviceMap.remove(deviceID );  // Remove this so it does not get added below
                mapSeries.put( sd.getID(), s);
            }
        }

        // Loop over the remaining items in the device map and add a series for them.
        // only the probes that did not have data should be in the list.
        
        for ( SDevice sd : deviceMap.values())
        {
            Series s = chart.createSeries();
            
            mapSeries.put( sd.getID(), s);

            addNewSeries( sd, s );

        }

       initWidget( chart );
    }


    private void addNewSeries( SDevice sd, Series s )
    {
        if ( sd.getProbeType() == DeviceType.BLOWER )
        {
            chart.addSeries(s
                    .setName(sd.getName())
                    .setType(Series.Type.LINE)
                    .setOption("step", true )
                    
                    );
        }
        else
        {
        chart.addSeries(s
                .setName(sd.getName())
                .setType(Series.Type.SPLINE)
                
                );
        }    
        
        if ( sd.isProbe() )
        {
            s.setYAxis(0);
        }
        else
        {
            if ( sd instanceof StokerFan )
            {
                fanID = sd.getID();
            }
            s.setYAxis(1);
        }
        
    }
    
    private long getTime(String date) {
        return dateTimeFormat.parse(date).getTime();
    }

    static final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("yyyy-MM-dd");


    private Number[][] convertToArray( ArrayList<SDataPoint> data )
    {
        Number[][] number = new Number[data.size()][2];
        int i = 0;
        for ( SDataPoint sdp : data )
        {
            number[i][0] = sdp.getCollectedDate().getTime();
            number[i][1] = sdp.getData();
            i++;
        }
        
        return number;
    }

    public void addData( SDataPoint sdp, boolean refresh )
    {
        Series s = mapSeries.get( sdp.getDeviceID() );

        if ( s != null )
        {
            if ( sdp.getDeviceID().compareTo(fanID) != 0 )
            {
              if  ( !refresh )
              {
                   if (! sdp.isTimedEvent() )
                       return;
              }
            }
            s.addPoint(sdp.getCollectedDate().getTime(), sdp.getData(), !refresh , false, !refresh);

        }
    }

    public void redraw()
    {
        chart.redraw();
    }

    public void addData( ArrayList<SDataPoint> arSDP)
    {
        if ( arSDP.size() > 0 )
        {
            // All the elements in the array should be for the same deviceID
            Series s = mapSeries.get( arSDP.get(0).getDeviceID() );

            if ( s != null )
            {
                System.out.println("Starting datapoint conversion");  // TODO: remove

                for ( int i = 0; i < arSDP.size(); i++ )
                {
                    System.out.println("DeviceID: " + arSDP.get(i).getDeviceID());
                    System.out.println("  Date: " + arSDP.get(i).getCollectedDate().toString());
                    s.addPoint(arSDP.get(i).getCollectedDate().getTime(), arSDP.get(i).getData(), false , false, false);

                }
                System.out.println("Ending datapoint conversion");  // TODO: remove
      //          s.setPoints(fa,  false );
                System.out.println("Datapoints Added");  // TODO: remove


                //for ( SDataPoint sdp : arSDP)
                //{
                 //  addData( sdp, refresh );
                //}
               // chart.redraw();
            }
        }
    }

}
