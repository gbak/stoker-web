package sweb.client.gauge;

import sweb.shared.model.StokerProbe;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.Gauge;
import com.google.gwt.visualization.client.visualizations.Gauge.Options;

public class GaugeDisplay extends Composite implements InstantTempDisplay
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

    public GaugeDisplay()
    {
       g = new Gauge();
    
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
       int isize = ((iMaxGaugeTemp - iMinGaugeTemp) / iGaugeStepping) + 1;
       String[] sa = new String[isize];
       int iTemp = iMinGaugeTemp;


       options.setWidth(190);
       options.setHeight(200);

       
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
    public void init(String name, Object o)
    {
        initDataTable(name);
        initOptions();
        
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

               // use both the red and yellow ranges modifying the color
               // to be the same when the temp is in the safe range

               if ( iTempAlarmLow > 0 && stokerProbe.getCurrentTemp() < iTempAlarmLow )
                  options.set("yellowColor",strColorRed );
               else if ( iTempAlarmHigh > 0 && stokerProbe.getCurrentTemp() > iTempAlarmHigh )
                  options.set("redColor",strColorRed );
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

    @Override
    public void draw()
    {
        g.draw(data, options);
        
    }

    @Override
    public void setTemp(float f)
    {
        data.setValue(0, 1, (int)f);
        
    }

    @Override
    public void setTemp(int i)
    {
        data.setValue(0, 1, i);
        
    }

   


    
}
