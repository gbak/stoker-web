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


import java.util.HashMap;

import sweb.client.LoginStatus;
import sweb.client.dialog.GeneralMessageDialog;
import sweb.shared.FieldVerifier;
import sweb.shared.model.data.SBlowerDataPoint;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.data.SProbeDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.stoker.StokerFan;
import sweb.shared.model.stoker.StokerProbe;

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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.Gauge;


/**
 * @author gary.bak
 *
 */
public class ProbeComponent extends Composite
{
   FlexTable m_layout = null;
   DecoratorPanel m_decPan = null;
   
   InstantTempDisplay m_instantTempDisplay = null;
  // HorizontalPanel fanStatusHorizontalPanel = null;
   SimplePanel m_fanStatusPanel = null;
   SimplePanel m_tempPanel = null;
   DisclosurePanel m_dp = null;
   Button m_buttonAlertSettings = null;
   CheckBox m_alarmSelectedCheckBox = null;

   TextBox m_targtTempTextBox = null;
   TextBox m_alarmHighTextBox = null;
   TextBox m_alarmLowTextBox = null;
   TextBox m_probeNameTextBox = null;

   HTML m_htmlTargetTemperature = null;
   HTML m_htmlAlarmType = null;
   HTML m_htmlHighAlarm = null;
   HTML m_htmlLowAlarm = null;

   ListBox m_alarmTypeListBox = null;
  
   public static enum Alignment { SINGLE, MULTIPLE };

   FanStatusBinder m_fanStatusBinder = null;

   StokerProbe m_stokerProbe = null;
   boolean m_deviceConfigChanged = false;

   HashMap<String,String> m_properties = null;
   Alignment m_alignment;

   public ProbeComponent(StokerProbe sd1, Alignment align, HashMap<String,String> p )
   {
       //GaugeComponent gc = new GaugeComponent();

       m_properties = p;
       m_alignment = align;
       CellPanel cellp = null;

       m_stokerProbe = sd1;

       m_alarmSelectedCheckBox = new CheckBox();

       if ( sd1.getAlarmEnabled() == StokerProbe.AlarmType.NONE )
           m_alarmSelectedCheckBox.setValue( false );
       else
           m_alarmSelectedCheckBox.setValue( true );

       m_tempPanel = new SimplePanel();
    
       String tempDisplayType = m_properties.get("client.tempDisplayType");
       if ( tempDisplayType == null )
           tempDisplayType = "gauge";
       
       
       if ( tempDisplayType.equalsIgnoreCase("text") )
       {
           m_instantTempDisplay = new DigitDisplayBinder();
           m_instantTempDisplay.init(m_stokerProbe);
           
           m_tempPanel.add((Widget) m_instantTempDisplay );
           m_instantTempDisplay.setAlarmRange(m_stokerProbe);
       }
       else
       {
           Runnable onLoadCallBack = new Runnable() 
           {
               public void run()
               {
                   m_instantTempDisplay = new GaugeDisplay(m_properties);
                   m_instantTempDisplay.init( m_stokerProbe );
                   
                   m_tempPanel.add((Widget) m_instantTempDisplay );
                   m_instantTempDisplay.setAlarmRange(m_stokerProbe);
               }

           };
           VisualizationUtils.loadVisualizationApi(onLoadCallBack, Gauge.PACKAGE );

       }
       
       m_layout = new FlexTable();
       
       cellp = new VerticalPanel();
       
       ((VerticalPanel)cellp).setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
       ((VerticalPanel)cellp).setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);


       
       m_fanStatusPanel = new SimplePanel();
       m_decPan = new DecoratorPanel();
       m_dp = new DisclosurePanel("Settings");
       m_dp.setVisible(false);

       VerticalPanel vp1 = new VerticalPanel();
       
      // Label lName = new Label( sd1.getName());
       m_probeNameTextBox = new TextBox( );
       m_probeNameTextBox.setText(sd1.getName());
    //   lName.setWidth("100%");
       m_probeNameTextBox.setStylePrimaryName("label-GaugeName");
       m_probeNameTextBox.addChangeHandler(settingsChangeHandlerName());
      // lName.setStylePrimaryName("label-GaugeName");

       if (sd1.getFanDevice() != null )
       {
           m_fanStatusBinder = new FanStatusBinder();
           m_fanStatusPanel.add( m_fanStatusBinder );
       }
       else
       {
           m_fanStatusPanel.setHeight("25px");
       }
 
       FlexTable ft = getSettingsPanel(sd1.getFanDevice() != null ? true : false);

       ft.addStyleName("sweb-flexGauge");

       m_dp.setContent( ft );
       m_dp.setAnimationEnabled(true);

       vp1.add(m_probeNameTextBox);
       vp1.add(m_tempPanel); 
       vp1.setCellHorizontalAlignment(m_tempPanel, VerticalPanel.ALIGN_CENTER);
       vp1.setCellVerticalAlignment(m_tempPanel, VerticalPanel.ALIGN_MIDDLE);

       vp1.add( m_fanStatusPanel );
   //    vp2.setCellHorizontalAlignment( fanStatusPanel, HasHorizontalAlignment.ALIGN_CENTER);

       if ( m_alignment == Alignment.SINGLE)
       {
          vp1.add( ft );
          cellp.addStyleName("sweb-panelGaugeTall");
       }
       else
       {
           vp1.add( m_dp );
           vp1.setCellHorizontalAlignment(m_dp, VerticalPanel.ALIGN_LEFT); 
           cellp.addStyleName("sweb-panelGaugeShort");
       }
       
       cellp.add( vp1 );
       
       m_decPan.setWidget( cellp );

       changeVisibility(LoginStatus.getInstance().getLoginStatus());
       initWidget( m_decPan );
   }
   


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
       m_dp.setVisible(b);
       m_targtTempTextBox.setEnabled(b);
       System.out.println("Style: " + m_targtTempTextBox.getStyleName());
       m_alarmHighTextBox.setEnabled(b);
       m_alarmLowTextBox.setEnabled(b);
       m_alarmTypeListBox.setEnabled(b);
       m_probeNameTextBox.setEnabled(b);
       

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
       if ( m_instantTempDisplay != null)
          m_instantTempDisplay.draw();
   }

   private FlexTable getSettingsPanel(boolean bPitSensor)
   {
       m_targtTempTextBox = new TextBox();
       m_alarmHighTextBox = new TextBox();;
       m_alarmLowTextBox = new TextBox();
       m_alarmTypeListBox = new ListBox();

       m_targtTempTextBox.setText(new Integer(m_stokerProbe.getTargetTemp()).toString());
       m_alarmHighTextBox.setText(new Integer(m_stokerProbe.getUpperTempAlarm()).toString());
       m_alarmLowTextBox.setText(new Integer(m_stokerProbe.getLowerTempAlarm()).toString());

       m_alarmTypeListBox.addItem("None");
       m_alarmTypeListBox.addItem("Food");
       m_alarmTypeListBox.addItem("Fire");


       m_alarmTypeListBox.setSelectedIndex(m_stokerProbe.getAlarmEnabled().ordinal());

       FlexTable ft = new FlexTable();

       ft.setCellSpacing(6);
      ft.setStyleName("sweb-flexAlarmSettings");
       FlexCellFormatter cellFormatter = ft.getFlexCellFormatter();
       ft.getFlexCellFormatter().setColSpan(0, 0, 2);
       ft.getFlexCellFormatter().setColSpan(3, 1, 1);
       ft.getFlexCellFormatter().setColSpan(2, 1, 1);
       ft.getFlexCellFormatter().setColSpan(0, 1, 1);
       ft.getFlexCellFormatter().setColSpan(1, 1, 2);

       m_htmlTargetTemperature = new HTML("Target Temperature", true);
       m_htmlTargetTemperature.setStyleName("html-SettingsText");
       ft.setWidget(0, 0, m_htmlTargetTemperature);
       ft.setWidget(0,1, m_targtTempTextBox);
       m_targtTempTextBox.setStyleName("temp-TextBox");
     //  targtTempTextBox.addMouseOverHandler(settingsTextBoxMouseOverHandler());
     //  targtTempTextBox.addMouseOutHandler(settingsTextBoxMouseOutHandler());



       m_htmlAlarmType = new HTML("Alarm Type");
       m_htmlAlarmType.setStyleName("html-SettingsText");
       ft.setWidget(1,0, m_htmlAlarmType );
       m_alarmTypeListBox.setStyleName("alarmType-TextBox");
       ft.setWidget(1,1, m_alarmTypeListBox);

       m_htmlLowAlarm = new HTML("Low Alarm Temp:");
       m_htmlLowAlarm.setStyleName("html-SettingsText");
       ft.setWidget(2,0, m_htmlLowAlarm );
       cellFormatter.setColSpan(2, 0, 2);
       ft.setWidget(2,1, m_alarmLowTextBox);
       m_alarmLowTextBox.setStyleName("temp-TextBox");

       m_htmlHighAlarm = new HTML("High Alarm Temp");
       m_htmlHighAlarm.setStyleName("html-SettingsText");
       ft.setWidget( 3,0, m_htmlHighAlarm );
       cellFormatter.setColSpan(3, 0, 2);
       ft.setWidget(3,1, m_alarmHighTextBox);
       m_alarmHighTextBox.setStyleName("temp-TextBox");



       m_alarmTypeListBox.addChangeHandler(alarmTypeChangeHandler());

       m_alarmTypeListBox.addChangeHandler( settingsChangeHandlerAlarm() );
       m_targtTempTextBox.addChangeHandler( settingsChangeHandlerTarget() );
       m_alarmHighTextBox.addChangeHandler( settingsChangeHandlerHigh() );
       m_alarmLowTextBox.addChangeHandler( settingsChangeHandlerLow() );


       setSettingsVisibility();

       ft.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_LEFT);
       ft.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
       ft.getCellFormatter().setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_RIGHT);
       ft.getCellFormatter().setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_RIGHT);

       return ft;
   }

   public SDevice getConfigUpdates()
   {
       SDevice s = m_deviceConfigChanged ? s = m_stokerProbe : null;
       m_deviceConfigChanged = false;
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

       if ( m_alarmTypeListBox.getSelectedIndex() == 0 )
       {
           boolean b = true;
           if ( m_stokerProbe.getFanDevice() == null)
              b = false;

           m_htmlTargetTemperature.setVisible(b);
           m_targtTempTextBox.setVisible(b);

           m_htmlHighAlarm.setVisible(false);
           m_alarmHighTextBox.setVisible(false);

           m_htmlLowAlarm.setVisible(false);
           m_alarmLowTextBox.setVisible(false);

       }
       else if (m_alarmTypeListBox.getSelectedIndex() == 1 )
       {
           m_htmlTargetTemperature.setVisible(true);
           m_targtTempTextBox.setVisible(true);

           m_htmlHighAlarm.setVisible(false);
           m_alarmHighTextBox.setVisible(false);

           m_htmlLowAlarm.setVisible(false);
           m_alarmLowTextBox.setVisible(false);
       }
       else if (m_alarmTypeListBox.getSelectedIndex() == 2 )
       {
           m_htmlTargetTemperature.setVisible(true);
           m_targtTempTextBox.setVisible(true);

           m_htmlHighAlarm.setVisible(true);
           m_alarmHighTextBox.setVisible(true);

           m_htmlLowAlarm.setVisible(true);
           m_alarmLowTextBox.setVisible(true);
       }

   }


   // This stinks but the easiest route
   private ChangeHandler settingsChangeHandlerAlarm()
   {
       ChangeHandler cl = new ChangeHandler(){

           public void onChange(ChangeEvent event)
           {
               m_stokerProbe.setAlarmEnabled(StokerProbe.getAlarmTypeForString(m_alarmTypeListBox.getItemText(m_alarmTypeListBox.getSelectedIndex())));
               m_deviceConfigChanged = true;
               // TODO: fire flag update event;
           }
       };
    return cl;
   }

   private ChangeHandler settingsChangeHandlerName()
   {
       ChangeHandler cl = new ChangeHandler(){

           public void onChange(ChangeEvent event)
           {
               m_stokerProbe.setName(m_probeNameTextBox.getText());
               System.out.println("Name change detected in Target "  );
               m_deviceConfigChanged = true;
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
               Integer i = FieldVerifier.getValidTemp(m_targtTempTextBox.getText());
               if ( i == null)
               {
                   new GeneralMessageDialog("Error", "Invalid Temperature Entered").center();
                   m_targtTempTextBox.setText(new Integer(m_stokerProbe.getTargetTemp()).toString());
                   return;
               }
               m_stokerProbe.setTargetTemp(i);
               System.out.println("Change detected in Target "  );
               m_instantTempDisplay.setAlarmRange(m_stokerProbe);
               m_deviceConfigChanged = true;
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
               Integer i = FieldVerifier.getValidTemp(m_alarmHighTextBox.getText());
               if ( i == null)
               {
                   new GeneralMessageDialog("Error", "Invalid Temperature Entered").center();
                   m_alarmHighTextBox.setText(new Integer(m_stokerProbe.getUpperTempAlarm()).toString());
                   return;
               }
               m_stokerProbe.setUpperTempAlarm(i);
               m_instantTempDisplay.setAlarmRange(m_stokerProbe);
               m_deviceConfigChanged = true;
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
               Integer i = FieldVerifier.getValidTemp(m_alarmLowTextBox.getText());
               if ( i == null)
               {
                   new GeneralMessageDialog("Error", "Invalid Temperature Entered").center();
                   m_alarmLowTextBox.setText(new Integer(m_stokerProbe.getLowerTempAlarm()).toString());
                   return;
               }
               m_stokerProbe.setLowerTempAlarm(i);  // TODO: need integer validation
               m_instantTempDisplay.setAlarmRange(m_stokerProbe);
               m_deviceConfigChanged = true;
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
       m_stokerProbe.setCurrentTemp(f);
       updateFanStatus();
       if ( m_instantTempDisplay != null)
       {
           m_instantTempDisplay.setTemp(f);
           //ist.setAlarmRange(stokerProbe);
       }
   }
   public void updateData( StokerProbe sp )
   {
      //sp.getCurrentTemp();
      m_stokerProbe = sp;
      updateFanStatus();
      if ( m_instantTempDisplay != null )
      {
         m_instantTempDisplay.setTemp( sp.getCurrentTemp());
        // ist.setAlarmRange(sp);
      }
   }

   public void updateData( SDataPoint dp )
   {
       if ( dp instanceof SBlowerDataPoint)
       {
          // logger.debug("GaugeComponent, " + dp.getDeviceID() + " Runtime: " + ((SBlowerDataPoint)dp).getTotalRuntime());
          // stokerProbe.getFanDevice().setFanOn(((SBlowerDataPoint)dp).isFanOn());
           
           if ( ((SBlowerDataPoint)dp).getTotalRuntime() < 0 )  // dummy point, ignore
               return;
           
           m_stokerProbe.getFanDevice().update(dp);
           updateFanStatus();
       }
       else
       {
          m_stokerProbe.setCurrentTemp(((SProbeDataPoint)dp).getTempF());
          if ( m_instantTempDisplay != null )
          {
            // ist.setAlarmRange(stokerProbe);
              m_instantTempDisplay.checkAlarms((int)m_stokerProbe.getCurrentTemp());
             m_instantTempDisplay.setTemp(m_stokerProbe.getCurrentTemp());
          }

          
       }
   }

   private void updateFanStatus()
   {
       StokerFan sf = m_stokerProbe.getFanDevice();

       if ( sf != null )
       {
           if ( sf.isFanOn() )
           {
               m_fanStatusBinder.fanOn(m_stokerProbe.getFanDevice().getTotalRuntime());
               //fanImage.setUrl(strFanOnURL);
               
           }
           else
           {
               m_fanStatusBinder.fanOff(m_stokerProbe.getFanDevice().getTotalRuntime());
               //fanImage.setUrl(strFanOffURL);
           }
           // Fan icon not switching correctly.
       }

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
