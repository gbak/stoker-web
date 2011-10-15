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

package sweb.client.dialog;

import java.util.ArrayList;

import sweb.client.StokerCoreServiceAsync;
import sweb.client.dialog.handlers.AlertsDialogHandler;
import sweb.shared.model.alerts.AlertBase;
import sweb.shared.model.alerts.ConnectionChangeAlert;
import sweb.shared.model.alerts.StokerAlarmAlert;
import sweb.shared.model.alerts.TempAlarmAlert;
import sweb.shared.model.alerts.TimeAlert;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AlertsSettingsDialog extends DialogBox
{

    FlexTable flexTable = null;
    StokerCoreServiceAsync gsa = null;
    AlertsDialogHandler alertsDialogHandler = null;
    ArrayList<AlertBase> alertBaseList = new ArrayList<AlertBase>();
   
    public AlertsSettingsDialog(StokerCoreServiceAsync g, AlertsDialogHandler alertsDialogHandler )
    {
        super();

        gsa = g;
        this.alertsDialogHandler = alertsDialogHandler;
        
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
        
        AlertBase stokerAlarm = new StokerAlarmAlert();
        addButtons(addTempButton, addTimeButton );
        addRow( stokerAlarm );
        
        AlertBase connectionAlert = new ConnectionChangeAlert();
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
              db.hide();
           }
        });

    }


   private ClickHandler addTempButtonClickHandler()
    {
        return new ClickHandler() {

            public void onClick(ClickEvent event)
            {
               AlertBase tempAlarmAlert = new TempAlarmAlert();
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
               AlertBase timeAlert = new TimeAlert();
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
    private void addRow( AlertBase alertBase )
    {
       int numRows = flexTable.getRowCount();
       alertBaseList.add( alertBase );
       CheckBox cb = new CheckBox();

       if ( numRows > 0 )
       {
          int prev = numRows -1;
          Widget wl = flexTable.getWidget(prev, 0);
          Widget wr = flexTable.getWidget(prev, 1);
          
          flexTable.setWidget( prev, 0, cb );
          flexTable.setWidget( prev, 1, new HTML(alertBase.getName()) );
          
          flexTable.setWidget( numRows, 0, wl );
          flexTable.setWidget( numRows, 1, wr );
          
       }
       else
       {
          flexTable.setWidget( numRows, 0, cb );
          flexTable.setWidget( numRows, 1,  new HTML(alertBase.getName()) );
       }
       //flexTable.getFlexCellFormatter().setRowSpan(0, 1, numRows + 1);
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
