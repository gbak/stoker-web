
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

package sweb.client.gauge;


import java.util.HashMap;

import sweb.client.LoginStatus;
import sweb.client.dialog.GeneralMessageDialog;
import sweb.shared.FieldVerifier;
import sweb.shared.model.data.SBlowerDataPoint;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.data.SProbeDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.devices.stoker.StokerFan;
import sweb.shared.model.devices.stoker.StokerProbe;

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
import com.google.gwt.user.client.ui.Label;
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
public class DynamicProbeComponent extends Composite
{
   String strGaugeName = "";
   FlexTable layout = null;
   DecoratorPanel decPan = null;
   
   InstantTempDisplay ist = null;
  // HorizontalPanel fanStatusHorizontalPanel = null;
   SimplePanel fanStatusPanel = null;
   SimplePanel tempPanel = null;
   DisclosurePanel dp = null;
   Button buttonAlertSettings = null;
   CheckBox alarmSelectedCheckBox = null;

   TextBox targtTempTextBox = null;
   TextBox alarmHighTextBox = null;
   TextBox alarmLowTextBox = null;

   HTML htmlTargetTemperature = null;
   HTML htmlAlarmType = null;
   HTML htmlHighAlarm = null;
   HTML htmlLowAlarm = null;

   ListBox alarmTypeListBox = null;
  
   public static enum Alignment { SINGLE, MULTIPLE };

   FanStatusBinder fsb = null;

   StokerProbe m_stokerProbe = null;
   boolean deviceConfigChanged = false;

   HashMap<String,String> m_properties = null;
   Alignment alignment;

   public DynamicProbeComponent(StokerProbe sd1, HashMap<String,String> p )
   {
       m_properties = p;
       CellPanel cellp = null;

       m_stokerProbe = sd1;

       alarmSelectedCheckBox = new CheckBox();

       if ( sd1.getAlarmEnabled() == StokerProbe.AlarmType.NONE )
           alarmSelectedCheckBox.setValue( false );
       else
           alarmSelectedCheckBox.setValue( true );

       tempPanel = new SimplePanel();
    
       String tempDisplayType = m_properties.get("client.tempDisplayType");
       if ( tempDisplayType == null )
           tempDisplayType = "gauge";
       
       
       if ( tempDisplayType.equalsIgnoreCase("text") )
       {
           ist = new DigitDisplayBinder();
           ist.init(m_stokerProbe);
           
           tempPanel.add((Widget) ist );
           ist.setAlarmRange(m_stokerProbe);
       }
       else
       {
           Runnable onLoadCallBack = new Runnable() 
           {
               public void run()
               {
                   ist = new GaugeDisplay(m_properties);
                   ist.init( m_stokerProbe );
                   
                   tempPanel.add((Widget) ist );
                   ist.setAlarmRange(m_stokerProbe);
               }

           };
           VisualizationUtils.loadVisualizationApi(onLoadCallBack, Gauge.PACKAGE );

       }
       
       layout = new FlexTable();
       
       cellp = new VerticalPanel();
       
       ((VerticalPanel)cellp).setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
       ((VerticalPanel)cellp).setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);


       
       fanStatusPanel = new SimplePanel();
       decPan = new DecoratorPanel();
       dp = new DisclosurePanel("Settings");
       dp.setVisible(false);

       VerticalPanel vp1 = new VerticalPanel();
       
       Label lName = new Label( sd1.getName());
       lName.setStylePrimaryName("label-GaugeName");

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
       vp1.add(tempPanel); 
       vp1.setCellHorizontalAlignment(tempPanel, VerticalPanel.ALIGN_CENTER);
       vp1.setCellVerticalAlignment(tempPanel, VerticalPanel.ALIGN_MIDDLE);

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
       if ( ist != null)
          ist.draw();
   }

   private FlexTable getSettingsPanel(boolean bPitSensor)
   {
       targtTempTextBox = new TextBox();
       alarmHighTextBox = new TextBox();;
       alarmLowTextBox = new TextBox();
       alarmTypeListBox = new ListBox();

       targtTempTextBox.setText(new Integer(m_stokerProbe.getTargetTemp()).toString());
       alarmHighTextBox.setText(new Integer(m_stokerProbe.getUpperTempAlarm()).toString());
       alarmLowTextBox.setText(new Integer(m_stokerProbe.getLowerTempAlarm()).toString());

       alarmTypeListBox.addItem("None");
       alarmTypeListBox.addItem("Food");
       alarmTypeListBox.addItem("Fire");


       alarmTypeListBox.setSelectedIndex(m_stokerProbe.getAlarmEnabled().ordinal());

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
       SDevice s = deviceConfigChanged ? s = m_stokerProbe : null;
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
           if ( m_stokerProbe.getFanDevice() == null)
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
               m_stokerProbe.setAlarmEnabled(StokerProbe.getAlarmTypeForString(alarmTypeListBox.getItemText(alarmTypeListBox.getSelectedIndex())));
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
                   targtTempTextBox.setText(new Integer(m_stokerProbe.getTargetTemp()).toString());
                   return;
               }
               m_stokerProbe.setTargetTemp(i);
               System.out.println("Change detected in Target "  );
               ist.setAlarmRange(m_stokerProbe);
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
                   alarmHighTextBox.setText(new Integer(m_stokerProbe.getUpperTempAlarm()).toString());
                   return;
               }
               m_stokerProbe.setUpperTempAlarm(i);
               ist.setAlarmRange(m_stokerProbe);
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
                   alarmLowTextBox.setText(new Integer(m_stokerProbe.getLowerTempAlarm()).toString());
                   return;
               }
               m_stokerProbe.setLowerTempAlarm(i);  // TODO: need integer validation
               ist.setAlarmRange(m_stokerProbe);
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
       m_stokerProbe.setCurrentTemp(f);
       updateFanStatus();
       if ( ist != null)
       {
           ist.setTemp(f);
           //ist.setAlarmRange(stokerProbe);
       }
   }
   public void updateData( StokerProbe sp )
   {
      //sp.getCurrentTemp();
      m_stokerProbe = sp;
      updateFanStatus();
      if ( ist != null )
      {
         ist.setTemp( sp.getCurrentTemp());
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
          if ( ist != null )
          {
            // ist.setAlarmRange(stokerProbe);
              ist.checkAlarms((int)m_stokerProbe.getCurrentTemp());
             ist.setTemp(m_stokerProbe.getCurrentTemp());
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
               fsb.fanOn(m_stokerProbe.getFanDevice().getTotalRuntime());
            
           }
           else
           {
               fsb.fanOff(m_stokerProbe.getFanDevice().getTotalRuntime());
           }
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
