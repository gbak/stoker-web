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

package sweb.client.gauge;


import sweb.client.LoginStatus;
import sweb.client.dialog.GeneralMessageDialog;
import sweb.shared.FieldVerifier;
import sweb.shared.model.SBlowerDataPoint;
import sweb.shared.model.SDataPoint;
import sweb.shared.model.SDevice;
import sweb.shared.model.SProbeDataPoint;
import sweb.shared.model.StokerFan;
import sweb.shared.model.StokerProbe;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.Gauge;
import com.google.gwt.visualization.client.visualizations.Gauge.Options;

/**
 * @author gary.bak
 *
 */
public class GaugeComponent extends Composite
{
   String strGaugeName = "";
   FlexTable layout = null;
   DecoratorPanel decPan = null;
   
  // HorizontalPanel fanStatusHorizontalPanel = null;
   SimplePanel fanStatusPanel = null;
   DisclosurePanel dp = null;
   Button buttonAlertSettings = null;
   DataTable data;
   Gauge g = null;
   Options options = null;
   CheckBox alarmSelectedCheckBox = null;

   TextBox targtTempTextBox = null;
   TextBox alarmHighTextBox = null;
   TextBox alarmLowTextBox = null;

   HTML htmlTargetTemperature = null;
   HTML htmlAlarmType = null;
   HTML htmlHighAlarm = null;
   HTML htmlLowAlarm = null;

   ListBox alarmTypeListBox = null;

   private static final String strFanOnURL = new String("fanOn_s.png");
   private static final String strFanOffURL = new String("fanOff_s.png");
   
   public static enum Alignment { SINGLE, MULTIPLE };

   Image fanImage = new Image(strFanOffURL);
   FanStatusBinder fsb = null;

   StokerProbe stokerProbe = null;
   boolean deviceConfigChanged = false;

   // TODO: move these to stokerweb.properties
   // These are client properties, they'll need to be pushed somehow.
   int iMinGaugeTemp = 0;
   int iMaxGaugeTemp =800;
   int iGaugeStepping = 100;
   String strColorGreen = "#109618";
   String strColorYellow = "#FF9900";
   String strColorRed = "#DC3912";
   
   Alignment alignment;

   public GaugeComponent(StokerProbe sd1, Alignment align )
   {
       //GaugeComponent gc = new GaugeComponent();

       alignment = align;
       CellPanel cellp = null;

       stokerProbe = sd1;

       alarmSelectedCheckBox = new CheckBox();

       if ( sd1.getAlarmEnabled() == StokerProbe.AlarmType.NONE )
           alarmSelectedCheckBox.setValue( false );
       else
           alarmSelectedCheckBox.setValue( true );

       g = new Gauge();
       layout = new FlexTable();
       
       cellp = new VerticalPanel();
       
       ((VerticalPanel)cellp).setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
       ((VerticalPanel)cellp).setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);


       
       fanStatusPanel = new SimplePanel();
       decPan = new DecoratorPanel();
       dp = new DisclosurePanel("Settings");
       dp.setVisible(false);
       options = Options.create();
       data = DataTable.create();

       
       VerticalPanel vp1 = new VerticalPanel();
       
       Label lName = new Label( sd1.getName());
       lName.setStylePrimaryName("label-GaugeName");
       
       initDataTable();
       initOptions();
  

       if (sd1.getFanDevice() != null )
       {
           fsb = new FanStatusBinder();
           fanStatusPanel.add( fsb );
       }
       else
       {
           fanStatusPanel.setHeight("25px");
       }
 
       FlexTable ft = getSettingsPanel(sd1.getFanDevice() != null ? true : false);

       ft.addStyleName("sweb-flexGauge");

       dp.setContent( ft );
       dp.setAnimationEnabled(true);
       
       vp1.add(lName);
       vp1.add(g); 

       vp1.add( fanStatusPanel );
   //    vp2.setCellHorizontalAlignment( fanStatusPanel, HasHorizontalAlignment.ALIGN_CENTER);

       if ( alignment == Alignment.SINGLE)
       {
          vp1.add( ft );
          cellp.addStyleName("sweb-panelGaugeTall");
       }
       else
       {
           vp1.add( dp );
           vp1.setCellHorizontalAlignment(dp, VerticalPanel.ALIGN_LEFT); 
           cellp.addStyleName("sweb-panelGaugeShort");
       }
       
       cellp.add( vp1 );
       
       decPan.setWidget( cellp );

       changeVisibility(LoginStatus.getInstance().getLoginStatus());
       initWidget( decPan );
   }
   
/*   public GaugeComponent(StokerProbe sd1, Alignment align )
   {
       //GaugeComponent gc = new GaugeComponent();
       VerticalPanel vp = null;
       HorizontalPanel hp = null;
       
       stokerProbe = sd1;

       alarmSelectedCheckBox = new CheckBox();

       if ( sd1.getAlarmEnabled() == StokerProbe.AlarmType.NONE )
           alarmSelectedCheckBox.setValue( false );
       else
           alarmSelectedCheckBox.setValue( true );

       g = new Gauge();
       layout = new FlexTable();
       
       if ( align == Alignment.VERTICAL )
          vp = new VerticalPanel();
       else
          hp = new HorizontalPanel();
       
      // fanStatusHorizontalPanel = new HorizontalPanel();
       fanStatusPanel = new SimplePanel();
       decPan = new DecoratorPanel();
       dp = new DisclosurePanel("Settings");
       options = Options.create();
       data = DataTable.create();

       dp.setVisible(false);

       Label lName = new Label( sd1.getName());
       lName.setStylePrimaryName("label-GaugeName");
       
       if ( align == Alignment.VERTICAL )
       {
          vp.add(lName);
          vp.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
          vp.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

          vp.addStyleName("sweb-panelGaugeV");
       }
       else
       {
           hp.add(lName);
           hp.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
           hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

           hp.addStyleName("sweb-panelGaugeH");
            
       }
       initDataTable();
       initOptions();

     //  vp.setCellHorizontalAlignment(g, VerticalPanel.ALIGN_CENTER);
       
       if ( align == Alignment.VERTICAL )
       vp.add(g);
       else
           hp.add( g );
      

       if (sd1.getFanDevice() != null )
       {
           fsb = new FanStatusBinder();
           //fanStatusHorizontalPanel.add( fsb );
           fanStatusPanel.add( fsb );
           
          Label lFan = new Label( "Fan: ");
          lFan.setStyleName("fan-Label");
          fanStatusHorizontalPanel.add(lFan);
          fanStatusHorizontalPanel.add(fanImage);
          fanStatusHorizontalPanel.setStyleName("fanStatus-panel");
          fanStatusHorizontalPanel.setHeight("25px");
          

       }
       else
       {
           // fanStatusHorizontalPanel.setHeight("25px");
           fanStatusPanel.setHeight("25px");
       }

       // vp.add( fanStatusHorizontalPanel );
       // vp.setCellHorizontalAlignment( fanStatusHorizontalPanel, HasHorizontalAlignment.ALIGN_RIGHT);

       if ( align == Alignment.VERTICAL )
       {
          vp.add( fanStatusPanel );
          vp.setCellHorizontalAlignment( fanStatusPanel, HasHorizontalAlignment.ALIGN_CENTER);
       }
       else
       {
           hp.add( fanStatusPanel );
           hp.setCellHorizontalAlignment( fanStatusPanel, HasHorizontalAlignment.ALIGN_CENTER);
           
       }
       FlexTable ft = getSettingsPanel(sd1.getFanDevice() != null ? true : false);
       dp.setContent( ft );
       dp.setAnimationEnabled(true);

       ft.addStyleName("sweb-flexGauge");
       
       
       CellPanel cp = null;
       // vp.add(dp);
       if ( align == Alignment.VERTICAL )
       {
           vp.add( ft );
           vp.setCellHorizontalAlignment(dp, VerticalPanel.ALIGN_LEFT);
           cp = vp;
       }
       else
       {
           hp.add( ft );
           hp.setCellHorizontalAlignment(dp, VerticalPanel.ALIGN_LEFT);
           cp = hp;
           
       }
       
      
       
       
       decPan.setWidget( cp );


       changeVisibility(LoginStatus.getInstance().getLoginStatus());
       initWidget( decPan );
   }*/

   /**
 * Convenience method to Allow the components in the settings panel to be edited.
 *
 * Same thing as calling changeVisibility( true );
 *
 */
   public void enableSettings()
   {
          //setVisible(true);
          changeVisibility( true );
   }

   /**
    * Convenience method to set the components in the settings panel to be non edit able
     */
   public void disableSettings()
   {
       changeVisibility( false );
   }


   public void changeVisibility( boolean b )
   {
       dp.setVisible(b);
       targtTempTextBox.setEnabled(b);
       System.out.println("Style: " + targtTempTextBox.getStyleName());
       alarmHighTextBox.setEnabled(b);
       alarmLowTextBox.setEnabled(b);
       alarmTypeListBox.setEnabled(b);

   }

   public void loginEvent()
   {
       changeVisibility(LoginStatus.getInstance().getLoginStatus());
   }


   /**
    * @wbp.parser.entryPoint
    */
   public void draw()
   {
      g.draw(data, options);
   }

   private FlexTable getSettingsPanel(boolean bPitSensor)
   {
       targtTempTextBox = new TextBox();
       alarmHighTextBox = new TextBox();;
       alarmLowTextBox = new TextBox();
       alarmTypeListBox = new ListBox();

       targtTempTextBox.setText(new Integer(stokerProbe.getTargetTemp()).toString());
       alarmHighTextBox.setText(new Integer(stokerProbe.getUpperTempAlarm()).toString());
       alarmLowTextBox.setText(new Integer(stokerProbe.getLowerTempAlarm()).toString());

       alarmTypeListBox.addItem("None");
       alarmTypeListBox.addItem("Food");
       alarmTypeListBox.addItem("Fire");


       alarmTypeListBox.setSelectedIndex(stokerProbe.getAlarmEnabled().ordinal());

       FlexTable ft = new FlexTable();

       ft.setCellSpacing(6);
      ft.setStyleName("sweb-flexAlarmSettings");
       FlexCellFormatter cellFormatter = ft.getFlexCellFormatter();
       ft.getFlexCellFormatter().setColSpan(0, 0, 2);
       ft.getFlexCellFormatter().setColSpan(3, 1, 1);
       ft.getFlexCellFormatter().setColSpan(2, 1, 1);
       ft.getFlexCellFormatter().setColSpan(0, 1, 1);
       ft.getFlexCellFormatter().setColSpan(1, 1, 2);

       htmlTargetTemperature = new HTML("Target Temperature", true);
       htmlTargetTemperature.setStyleName("html-SettingsText");
       ft.setWidget(0, 0, htmlTargetTemperature);
       ft.setWidget(0,1, targtTempTextBox);
       targtTempTextBox.setStyleName("temp-TextBox");
     //  targtTempTextBox.addMouseOverHandler(settingsTextBoxMouseOverHandler());
     //  targtTempTextBox.addMouseOutHandler(settingsTextBoxMouseOutHandler());



       htmlAlarmType = new HTML("Alarm Type");
       htmlAlarmType.setStyleName("html-SettingsText");
       ft.setWidget(1,0, htmlAlarmType );
       alarmTypeListBox.setStyleName("alarmType-TextBox");
       ft.setWidget(1,1, alarmTypeListBox);

       htmlLowAlarm = new HTML("Low Alarm Temp:");
       htmlLowAlarm.setStyleName("html-SettingsText");
       ft.setWidget(2,0, htmlLowAlarm );
       cellFormatter.setColSpan(2, 0, 2);
       ft.setWidget(2,1, alarmLowTextBox);
       alarmLowTextBox.setStyleName("temp-TextBox");

       htmlHighAlarm = new HTML("High Alarm Temp");
       htmlHighAlarm.setStyleName("html-SettingsText");
       ft.setWidget( 3,0, htmlHighAlarm );
       cellFormatter.setColSpan(3, 0, 2);
       ft.setWidget(3,1, alarmHighTextBox);
       alarmHighTextBox.setStyleName("temp-TextBox");



       alarmTypeListBox.addChangeHandler(alarmTypeChangeHandler());

       alarmTypeListBox.addChangeHandler( settingsChangeHandlerAlarm() );
       targtTempTextBox.addChangeHandler( settingsChangeHandlerTarget() );
       alarmHighTextBox.addChangeHandler( settingsChangeHandlerHigh() );
       alarmLowTextBox.addChangeHandler( settingsChangeHandlerLow() );


       setSettingsVisibility();

       ft.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_LEFT);
       ft.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
       ft.getCellFormatter().setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_RIGHT);
       ft.getCellFormatter().setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_RIGHT);

       return ft;
   }

   public SDevice getConfigUpdates()
   {
       SDevice s = deviceConfigChanged ? s = stokerProbe : null;
       deviceConfigChanged = false;
       return s;
   }

   private MouseOverHandler settingsTextBoxMouseOverHandler()
   {
       MouseOverHandler mol = new MouseOverHandler()
       {

        public void onMouseOver(MouseOverEvent event)
        {
            Widget widget = (Widget) event.getSource();
            widget.addStyleDependentName("hover");
          //  System.out.println("Style Name: + " + widget.getStyleName() );
        }

       };
       return mol;
   }

   private MouseOutHandler settingsTextBoxMouseOutHandler()
   {
       MouseOutHandler mol = new MouseOutHandler()
       {
            public void onMouseOut(MouseOutEvent event)
            {
                Widget widget = (Widget) event.getSource();
                widget.removeStyleDependentName("hover");
               // System.out.println("Style Name: + " + widget.getStyleName() );
            }

       };
       return mol;
   }

   private void setSettingsVisibility()
   {

       if ( alarmTypeListBox.getSelectedIndex() == 0 )
       {
           boolean b = true;
           if ( stokerProbe.getFanDevice() == null)
              b = false;

           htmlTargetTemperature.setVisible(b);
           targtTempTextBox.setVisible(b);

           htmlHighAlarm.setVisible(false);
           alarmHighTextBox.setVisible(false);

           htmlLowAlarm.setVisible(false);
           alarmLowTextBox.setVisible(false);

       }
       else if (alarmTypeListBox.getSelectedIndex() == 1 )
       {
           htmlTargetTemperature.setVisible(true);
           targtTempTextBox.setVisible(true);

           htmlHighAlarm.setVisible(false);
           alarmHighTextBox.setVisible(false);

           htmlLowAlarm.setVisible(false);
           alarmLowTextBox.setVisible(false);
       }
       else if (alarmTypeListBox.getSelectedIndex() == 2 )
       {
           htmlTargetTemperature.setVisible(true);
           targtTempTextBox.setVisible(true);

           htmlHighAlarm.setVisible(true);
           alarmHighTextBox.setVisible(true);

           htmlLowAlarm.setVisible(true);
           alarmLowTextBox.setVisible(true);
       }

   }


   // This stinks but the easiest route
   private ChangeHandler settingsChangeHandlerAlarm()
   {
       ChangeHandler cl = new ChangeHandler(){

           public void onChange(ChangeEvent event)
           {
               stokerProbe.setAlarmEnabled(StokerProbe.getAlarmTypeForString(alarmTypeListBox.getItemText(alarmTypeListBox.getSelectedIndex())));
               deviceConfigChanged = true;
               // TODO: fire flag update event;
           }
       };
    return cl;
   }

   private ChangeHandler settingsChangeHandlerTarget()
   {
       ChangeHandler cl = new ChangeHandler(){

           public void onChange(ChangeEvent event)
           {
               Integer i = FieldVerifier.getValidTemp(targtTempTextBox.getText());
               if ( i == null)
               {
                   new GeneralMessageDialog("Error", "Invalid Temperature Entered").center();
                   targtTempTextBox.setText(new Integer(stokerProbe.getTargetTemp()).toString());
                   return;
               }
               stokerProbe.setTargetTemp(i);
               System.out.println("Change detected in Target "  );
               deviceConfigChanged = true;
            // TODO: fire flag update event;
           }
       };
    return cl;
   }


   private ChangeHandler settingsChangeHandlerHigh()
   {
       ChangeHandler cl = new ChangeHandler(){

           public void onChange(ChangeEvent event)
           {
               Integer i = FieldVerifier.getValidTemp(alarmHighTextBox.getText());
               if ( i == null)
               {
                   new GeneralMessageDialog("Error", "Invalid Temperature Entered").center();
                   alarmHighTextBox.setText(new Integer(stokerProbe.getUpperTempAlarm()).toString());
                   return;
               }
               stokerProbe.setUpperTempAlarm(i);
               deviceConfigChanged = true;
            // TODO: fire flag update event;
           }
       };
    return cl;
   }
   private ChangeHandler settingsChangeHandlerLow()
   {
       ChangeHandler cl = new ChangeHandler(){

           public void onChange(ChangeEvent event)
           {
               Integer i = FieldVerifier.getValidTemp(alarmLowTextBox.getText());
               if ( i == null)
               {
                   new GeneralMessageDialog("Error", "Invalid Temperature Entered").center();
                   alarmLowTextBox.setText(new Integer(stokerProbe.getLowerTempAlarm()).toString());
                   return;
               }
               stokerProbe.setLowerTempAlarm(i);  // TODO: need integer validation
               deviceConfigChanged = true;
               // TODO: fire flag update event;
           }
       };
    return cl;
   }
   private ChangeHandler alarmTypeChangeHandler()
   {
       ChangeHandler cl = new ChangeHandler()
       {
           public void onChange(ChangeEvent event) {
              setSettingsVisibility();
           }
      };
      return cl;
   }

   public void updateCurrentTemp( float f )
   {
       stokerProbe.setCurrentTemp(f);
       setAlarmRange();
       updateFanStatus();
       data.setValue(0, 1, (int)f);

   }
   public void updateData( StokerProbe sp )
   {
      //sp.getCurrentTemp();
      stokerProbe = sp;
      setAlarmRange();
      updateFanStatus();
      data.setValue(0, 1, (int)sp.getCurrentTemp());

   }

   public void updateData( SDataPoint dp )
   {
       if ( dp instanceof SBlowerDataPoint)
       {
           System.out.println("GaugeComponent, " + dp.getDeviceID() + " Runtime: " + ((SBlowerDataPoint)dp).getTotalRuntime());
          // stokerProbe.getFanDevice().setFanOn(((SBlowerDataPoint)dp).isFanOn());
           
           if ( ((SBlowerDataPoint)dp).getTotalRuntime() < 0 )  // dummy point, ignore
               return;
           
           stokerProbe.getFanDevice().update(dp);
           updateFanStatus();
       }
       else
       {
          stokerProbe.setCurrentTemp(((SProbeDataPoint)dp).getTempF());

          data.setValue(0, 1, (int)stokerProbe.getCurrentTemp());

          setAlarmRange();
       }
   }

   private void updateFanStatus()
   {
       StokerFan sf = stokerProbe.getFanDevice();

       if ( sf != null )
       {
           if ( sf.isFanOn() )
           {
               fsb.fanOn(stokerProbe.getFanDevice().getTotalRuntime());
               //fanImage.setUrl(strFanOnURL);
               
           }
           else
           {
               fsb.fanOff(stokerProbe.getFanDevice().getTotalRuntime());
               //fanImage.setUrl(strFanOffURL);
           }
           // Fan icon not switching correctly.
       }

   }

   private void setAlarmRange()
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



   private void initDataTable()
   {
      data.addColumn(ColumnType.STRING, "Label");
      data.addColumn(ColumnType.NUMBER, "Value");
      data.addRows(1);
      data.setValue(0, 0, strGaugeName);
      data.setValue(0, 1, 0);


 }

 private Options initOptions()
 {
    int isize = ((iMaxGaugeTemp - iMinGaugeTemp) / iGaugeStepping) + 1;
    String[] sa = new String[isize];
    int iTemp = iMinGaugeTemp;


    options.setWidth(200);
    options.setHeight(210);

    
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

    setAlarmRange();

    return options;
 }
/*
 class AlarmButtonHandler implements ClickHandler, KeyUpHandler
 {
    public void onClick(ClickEvent event)
    {

    }

    public void onKeyUp(KeyUpEvent event)
    {
       if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
       {

       }
    }

 }
 */

}
