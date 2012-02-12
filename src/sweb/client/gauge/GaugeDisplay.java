package sweb.client.gauge;

import java.util.HashMap;

import sweb.client.gauge.InstantTempDisplay.TempAlert;
import sweb.shared.model.stoker.StokerProbe;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.Gauge;
import com.google.gwt.visualization.client.visualizations.Gauge.Options;

public class GaugeDisplay extends InstantTempDisplay 
{

    DataTable data;
    Gauge g = null;
    Options options = null;

    // TODO: move these to stokerweb.properties
    // These are client properties, they'll need to be pushed somehow.
    int iMinGaugeTemp = 0;
    int iMaxGaugeTemp = 400;
    int iGaugeStepping = 50;
    
    String strColorGreen = "#109618";
    String strColorYellow = "#FF9900";
    String strColorRed = "#DC3912";
    
    HashMap<String,String> properties = null;

    public GaugeDisplay(HashMap<String,String> p)
    {
       g = new Gauge();
    
       properties = p;
       
       options = Options.create();
       data = DataTable.create();
       
      initWidget( g );
    }
    
    private void initDataTable(String name)
    {
       data.addColumn(ColumnType.STRING, "Label");
       data.addColumn(ColumnType.NUMBER, "Value");
       data.addRows(1);
       data.setValue(0, 0, name);
       data.setValue(0, 1, 0);


  }

    private Options initOptions()
    {
       


       options.setWidth(190);
       options.setHeight(200);

       String gaugeMax = properties.get("client.tempDisplayGauge.gaugeMax");
       String gaugeMin = properties.get("client.tempDisplayGauge.gaugeMin");
       String gaugeStepping = properties.get("client.tempDisplayGauge.gaugeStepping");
       
       if ( gaugeMax != null )
       {
           try
           {
              iMaxGaugeTemp = Integer.parseInt( gaugeMax );
           }
           catch ( NumberFormatException nfe)
           {
               System.out.println("Invald number for gaugeMax");
           }
       }
        
       if ( gaugeMin != null )
       {
           try
           {
              iMinGaugeTemp = Integer.parseInt( gaugeMin );
           }
           catch ( NumberFormatException nfe)
           {
               System.out.println("Invald number for gaugeMin");
           }
       }
       if ( gaugeStepping != null )
       {
           try
           {
               iGaugeStepping = Integer.parseInt( gaugeStepping );
           }
           catch ( NumberFormatException nfe)
           {
               System.out.println("Invald number for gaugeStepping");
           }
       }

       int isize = ((iMaxGaugeTemp - iMinGaugeTemp) / iGaugeStepping) + 1;
       String[] sa = new String[isize];
       int iTemp = iMinGaugeTemp;

       for ( int i = 0; i < isize; i++ )
       {
          sa[i] = new Integer( iTemp ).toString();
          iTemp = iTemp + iGaugeStepping;
       }
       //options.setMajorTicks("0","100","200","300","400","500","600","700","800");
       options.setMajorTicks(sa);
       options.setMinorTicks(5);
       options.set("min", new Integer(iMinGaugeTemp).toString());
       options.set("max", new Integer(iMaxGaugeTemp).toString());

      // setAlarmRange();

       return options;
    }
    
    @Override
    public void init(StokerProbe sp)
    {
        initDataTable("");  // this label will go inside the gauge.  The probe name would be nice,
                            // it crowds the gauge.
        initOptions();
        localProbe = sp;
        
    }

    @Override
    public void setAlarmRange(StokerProbe stokerProbe)
    {

            if ( stokerProbe.getAlarmEnabled().equals(StokerProbe.AlarmType.ALARM_FIRE))
            {
                int iTempAlarmLow = stokerProbe.getLowerTempAlarm();
                int iTempAlarmHigh = stokerProbe.getUpperTempAlarm();

               if ( iTempAlarmHigh > 0)
               {
                  options.setRedRange(iTempAlarmHigh, iMaxGaugeTemp);
                  options.set("redColor", strColorYellow);
               }

               if ( iTempAlarmLow > 0 )
               {
                  options.set("yellowColor", strColorYellow);
                  options.setYellowRange(iMinGaugeTemp, iTempAlarmLow);
               }


            }
            else if ( stokerProbe.getAlarmEnabled().equals(StokerProbe.AlarmType.ALARM_FOOD))
            {
               int iTarget = stokerProbe.getTargetTemp();
               if ( iTarget > 0)
               {
                  options.setRedRange(iTarget, iMaxGaugeTemp);
                  options.set("redColor", strColorYellow);
               }

               if ( iTarget > 0 && stokerProbe.getCurrentTemp() > iTarget )
                   options.set("redColor",strColorRed );

            }

            options.setGreenRange(stokerProbe.getTargetTemp()-2, stokerProbe.getTargetTemp()+2);
    }

    public void checkAlarms( int i )
    {
       super.checkAlarms(i);
       
       if ( change == true )
       {
           int iTempAlarmLow = localProbe.getLowerTempAlarm();
           int iTempAlarmHigh = localProbe.getUpperTempAlarm();

           if ( tempAlert == TempAlert.HIGH)
           {
                  options.set("redColor",strColorRed );
                  options.set("yellowColor",strColorYellow );
           }
           else if ( tempAlert == TempAlert.LOW )
           {
               options.set("redColor",strColorYellow );
               options.set("yellowColor",strColorRed );
           }
           else if ( tempAlert == TempAlert.NONE )
           {
               options.set("redColor",strColorYellow );
               options.set("yellowColor",strColorYellow );
           }
           change = false;
       }
       
    }
    
    @Override
    public void draw()
    {
        g.draw(data, options);
        
    }

    @Override
    public void setTemp(float f)
    {
        data.setValue(0, 1, (int)f);
        checkAlarms((int) f );
    }

    @Override
    public void setTemp(int i)
    {
        data.setValue(0, 1, i);
        checkAlarms(i);
    }

   


    
}
