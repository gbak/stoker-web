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

package sweb.client.graph;

import java.util.ArrayList;
import java.util.HashMap;

import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.StockChart;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.Labels;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.SplinePlotOptions;

import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.devices.SDevice;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;

public class HighstockLineGraph extends StokerLineGraph
{
    private HashMap<String,Series>  mapSeries = new HashMap<String,Series>();
    String fanID = new String();
    StockChart chart = null;

    private HighstockLineGraph()
    {

    }

    public static native void setHighchartTimezone() /*-{
    $wnd.Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });
}-*/;

    public static native void setRangeSelector() /*-{
    $wnd.Highcharts.setOptions({
        rangeSelector: {
            enabled: false
        }
    });
}-*/;
    public HighstockLineGraph(int iWidth, int iHeight, ArrayList<SDevice> listDeviceList)
    {
        //mapDeviceList = hmDeviceList;
        // super( iWidth, iHeight, listDeviceList );

        chart = new StockChart();

        chart.setToolTip(new ToolTip().setShared(true));
        //chart.setLegend(new Legend().setEnabled(false));
        chart.setLegend(new Legend()
            .setAlign(Legend.Align.CENTER)
            .setVerticalAlign(Legend.VerticalAlign.TOP)
            .setY(0)
            .setFloating(false)
            .setBorderWidth(0)
           );
        chart.setHeight(iHeight);
        chart.setChartTitleText(null);

      //  RangeSelector rs = new RangeSelector();
    //    rs.setOption("rangeSelector/enabled", "false");
        chart.setOption("rangeSelector/enabled", "false");
        //chart.setRangeSelector(rs);



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
        chart.setToolTip(new ToolTip()
                .setFormatter(new ToolTipFormatter() {
                    public String format(ToolTipData toolTipData) {
                        return toolTipData.getSeriesName() + ": " + NumberFormat.getFormat("###.#").format(toolTipData.getYAsDouble()) +
                                ("Temperature".equals(toolTipData.getSeriesName()) ? "°F" : " ");
                    }
                })
            )  ;



        chart.getXAxis()
          //  .setType(Axis.Type.DATE_TIME)
            .setAxisTitleText(null);


        chart.getYAxis(0)
            .setAxisTitleText("Temperature")
            .setMin(0)
            .setMax(500)
            .setStartOnTick(false)
            .setShowFirstLabel(false)
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
            .setMin(0)
            .setOpposite(true)
            .setStartOnTick(false)
            .setShowFirstLabel(false)
            .setMax(5)
            .setLabels(new YAxisLabels().setEnabled(false));


        /*     .setLabels(new YAxisLabels()
                .setAlign(Labels.Align.RIGHT)
                .setX(3)
                .setY(16)

                .setFormatter(new AxisLabelsFormatter() {
                    public String format(AxisLabelsData axisLabelsData) {
                        return NumberFormat.getFormat("#").format(axisLabelsData.getValueAsDouble());
                    }
                }))  ;
       */


        for ( SDevice sd : listDeviceList)
        {
            Series s = chart.createSeries();

         //   s.setPoints(new Number[][]{
         //           {getTime("2011-09-03"), 1},
         //           {getTime("2011-09-04"), 2},
         //           {getTime("2011-09-05"), 3},
         //           {getTime("2011-09-06"), 4}},  false );

            mapSeries.put( sd.getID(), s);

            chart.addSeries(s
                    .setName(sd.getName())
                    .setType(Series.Type.SPLINE)
                    );

            //  .setPlotOptions(new SplinePlotOptions()
            //      .setColor("#89A54E")
              //));

            if ( sd.isProbe() )
            {
                s.setYAxis(0);
            }
            else
            {
                s.setYAxis(1);
            }

        }

        setHighchartTimezone();
        setRangeSelector();
       initWidget( chart );
    }


    private long getTime(String date) {
        return dateTimeFormat.parse(date).getTime();
    }

    static final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("yyyy-MM-dd");


    public void addData( SDataPoint sdp, boolean refresh )
    {
        System.out.println("in add Data");
        Series s = mapSeries.get( sdp.getDeviceID() );

        if ( s != null )
        {
            System.out.println("in add Data - passed null check ");

            if ( sdp.getDeviceID().compareTo(fanID) != 0 )
            {
              if  ( !refresh )
              {
                   if (! sdp.isTimedEvent())
                       return;
              }
            }
            s.addPoint(sdp.getCollectedDate().getTime(), sdp.getData(), !refresh , false, !refresh);


            Point[] ap = s.getPoints();
            System.out.println("ap size: " + ap.length);
            for ( int i = 0; i < ap.length; i++ )
            {
                Point p = ap[i];
                System.out.println( "X: [" + p.getX() + "] Y: [" + p.getY() + "]");
            }
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

                Number[][] fa = new Number[arSDP.size()][2];
                for ( int i = 0; i < arSDP.size(); i++ )
                {
                    System.out.println("DeviceID: " + arSDP.get(i).getDeviceID());
                    System.out.println("  Date: " + arSDP.get(i).getCollectedDate().toString());
                    fa[i][0] = arSDP.get(i).getCollectedDate().getTime();
                    fa[i][1] = arSDP.get(i).getData();
                  //  s.addPoint(arSDP.get(i).getCollectedDate().getTime(), arSDP.get(i).getData(), true , false, false);

                }
                System.out.println("Ending datapoint conversion");  // TODO: remove
      /*          s.setPoints(new Number[][]{
                        {getTime("2011-09-03"), 1},
                        {getTime("2011-09-04"), 2},
                        {getTime("2011-09-05"), 3},
                        {getTime("2011-09-06"), 4},
                        {getTime("2011-09-07"), 5}},  false );
  */              System.out.println("Datapoints Added");


                //for ( SDataPoint sdp : arSDP)
                //{
                 //  addData( sdp, refresh );
                //}
                //chart.redraw();
            }
        }
    }

    @Override
    public void setNewSize(int width, int height)
    {
        // TODO Auto-generated method stub
        
    }

}
