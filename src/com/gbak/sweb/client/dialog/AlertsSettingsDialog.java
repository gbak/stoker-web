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

package com.gbak.sweb.client.dialog;

import java.util.ArrayList;


import com.gbak.sweb.client.StokerCoreServiceAsync;
import com.gbak.sweb.client.dialog.handlers.AlertsSettingsDialogHandler;
import com.gbak.sweb.shared.model.alerts.AlertModel;
import com.gbak.sweb.shared.model.alerts.ConnectionChangeAlertModel;
import com.gbak.sweb.shared.model.alerts.StokerAlarmAlertModel;
import com.gbak.sweb.shared.model.alerts.TempAlarmAlertModel;
import com.gbak.sweb.shared.model.alerts.TimeAlertModel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AlertsSettingsDialog extends DialogBox
{

    FlexTable flexTable = null;
    StokerCoreServiceAsync gsa = null;
    AlertsSettingsDialogHandler alertsSettingsDialogHandler = null;
    ArrayList<AlertModel> alertBaseList = new ArrayList<AlertModel>();
   
    public AlertsSettingsDialog(StokerCoreServiceAsync g, ArrayList<AlertModel> alertConfig, AlertsSettingsDialogHandler alertsDialogHandler )
    {
        super();

        gsa = g;
        this.alertsSettingsDialogHandler = alertsDialogHandler;
        
        //this.setWidth("100%");
        setText("Alert Settings");
        
        setGlassEnabled(true);
        setAnimationEnabled(true);
        final Button closeButton = new Button("Close");
        final Button addTempButton = new Button("Add Temp Alert");
        final Button addTimeButton = new Button("Add Time Alert");
        
        
        closeButton.getElement().setId("closeButton");

        
        VerticalPanel dialogVPanel = new VerticalPanel();
        //dialogVPanel.addStyleName("alerts-dialog-panel");
       
        flexTable = new FlexTable();
        FlexCellFormatter cellFormatter = flexTable.getFlexCellFormatter();
       // fT.addStyleName("cw-FlexTable");
       // flexTable.setWidth("32em");
        
        flexTable.setCellSpacing(5);
        flexTable.setCellPadding(3);
       
        addTempButton.addClickHandler(addTempButtonClickHandler());
        addTimeButton.addClickHandler( addTimeButtonClickHandler());
        
        // TODO: Enable these when the alert is added exists
        addTempButton.setEnabled(false);
        addTimeButton.setEnabled(false);
        
        
        for ( AlertModel a : alertConfig )
        {
           //Alert stokerAlarm = new StokerAlarmAlert();
            if ( a instanceof StokerAlarmAlertModel )
            {
               addButtons(addTempButton, addTimeButton );
               addRow( a );
            }
        }
        
        // TODO: make a for loop for this one, and the ones below.
        AlertModel connectionAlert = new ConnectionChangeAlertModel();
        addRow( connectionAlert );
        
        
        dialogVPanel.add(flexTable);
        
        dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
        dialogVPanel.add(closeButton);
       // dialogVPanel.add(verticalPanel);
        setWidget(dialogVPanel);

        final DialogBox db = this;
        // Add a handler to close the DialogBox
        closeButton.addClickHandler(new ClickHandler()
        {
           public void onClick(ClickEvent event)
           {
              for ( int i = 0; i < flexTable.getRowCount() - 1; i++ )
              {
                 CheckBox cb = (CheckBox) flexTable.getWidget(i, 0);
                 
                 if ( cb.getValue() == true )
                    alertBaseList.get(i).setEnabled(true);
                 else
                    alertBaseList.get(i).setEnabled(false);
                 
                 DisclosurePanel dp = (DisclosurePanel) flexTable.getWidget(i, 1);
                 Grid g = (Grid)dp.getContent();
                 for ( int j = 0; j < g.getRowCount(); j++ )
                 {
                     CheckBox cb2 = (CheckBox) g.getWidget( j, 0);
                     if ( cb2.getValue() == true )
                     {
                         alertBaseList.get(i).getConfiguredDeliveryMethods().add( cb2.getText() );
                     }
                 }
                 
              }
              
              gsa.setAlertConfiguration(alertBaseList, new AsyncCallback<Void>() {
                  public void onFailure(Throwable caught)
                  {
                      System.out.println("Failure saving Alert Configuration");  // TODO: log
                  }
                  public void onSuccess(Void result)
                  {
                     // TODO Auto-generated method stub
                     
                  }
            });
              db.hide();
           }
        });

    }


   private ClickHandler addTempButtonClickHandler()
    {
        return new ClickHandler() {

            public void onClick(ClickEvent event)
            {
               AlertModel tempAlarmAlert = new TempAlarmAlertModel();
               addRow(tempAlarmAlert);
               // TODO: implement this
            }
        };
    } 

    private ClickHandler addTimeButtonClickHandler()
    {
        return new ClickHandler() {

            public void onClick(ClickEvent event)
            {
               AlertModel timeAlert = new TimeAlertModel();
               addRow( timeAlert );
            }
        };
    }
    
    private void addButtons(Widget label1, Widget label2)
    {
       int numRows = flexTable.getRowCount();
       
       flexTable.setWidget( numRows, 0, label1 );
       flexTable.setWidget( numRows, 1, label2 );
    }
    private void addRow( AlertModel alertBase )
    {
       int numRows = flexTable.getRowCount();
       
       
       alertBaseList.add( alertBase );
       CheckBox cb = new CheckBox();
       cb.setValue(alertBase.getEnabled());

       /*cb.addClickHandler( new ClickHandler()  {

         public void onClick(ClickEvent event)
         {
            
            
         }
          
       });*/
       
       Grid g =  deliveryDetails(alertBase);
       
       DisclosurePanel alertSettings = new DisclosurePanel(alertBase.getName());
      // alertSettings.setTitle(alertBase.getName());\
       alertSettings.setContent(g);
       
       if ( numRows > 0 )
       {
          int prev = numRows -1;
          Widget wl = flexTable.getWidget(prev, 0);
          Widget wr = flexTable.getWidget(prev, 1);
          
          // The align top is needed to prevent the checkbox from moving when the
          // disclosure panel is opened.
          FlexCellFormatter cellFormatter = flexTable.getFlexCellFormatter();
          cellFormatter.setVerticalAlignment(prev, 0,HasVerticalAlignment.ALIGN_TOP);
          
          flexTable.setWidget( prev, 0, cb );
          //flexTable.setWidget( prev, 1, new HTML(alertBase.getName()) );
          flexTable.setWidget( prev, 1, alertSettings );
          
          flexTable.setWidget( numRows, 0, wl );
          flexTable.setWidget( numRows, 1, wr );
          
       }
       else
       {
          flexTable.setWidget( numRows, 0, cb );
          //flexTable.setWidget( numRows, 1,  new HTML(alertBase.getName()) );
          flexTable.setWidget( numRows, 1,  alertSettings );
       }
       if ( g.getRowCount() == 0 )
       {
           cb.setEnabled(false);
           
       }
       //flexTable.getFlexCellFormatter().setRowSpan(0, 1, numRows + 1);
    }
    
    private Grid deliveryDetails(AlertModel alert)
    {
        int listSize = alert.getAvailableDeliveryMethods().size();
        Grid g = new Grid(listSize, 1);
        
        int x = 0;
        for ( String delivery : alert.getAvailableDeliveryMethods())
        {
            CheckBox cb = new CheckBox(delivery);
            if ( alert.getConfiguredDeliveryMethods().contains(delivery))
            {
                cb.setValue(true);
            }
            g.setWidget(x++, 0, cb);
        }
        
        return g;
    }
    
    public void show(Button b)
    {
        final Button b2 = b;
        setPopupPositionAndShow(new PositionCallback() {

            public void setPosition(int offsetWidth, int offsetHeight)
            {
//                int left = (Window.getClientWidth() - offsetWidth) / 3;
 //               int top = (Window.getClientHeight() - offsetHeight) / 3;
              //setPopupPosition(left, top);

                 setPopupPosition( b2.getAbsoluteLeft(), b2.getAbsoluteTop());


            }

        });


    }
    
}
