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

import sweb.client.dialog.handlers.NewLogDialogHandler;
import sweb.shared.model.SDevice;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

public class NewLogDialog extends DialogBox implements ClickHandler
{
    NewLogDialogHandler dialogHandler = null;
    String strLogName  = null;
    TextBox textBox = new TextBox();
    ArrayList<SDevice> arDeviceList = new ArrayList<SDevice>();
    ArrayList<CheckBox> arCheckBoxList = new ArrayList<CheckBox>();

    public NewLogDialog(ArrayList<SDevice> arSDevice, NewLogDialogHandler dh)
    {
        arDeviceList = arSDevice;
        dialogHandler = dh;
        setGlassEnabled(true);
        setAnimationEnabled(true);
        setTitle("Add New Log");

        final DialogBox d = this;
        Button cancelButton = new Button("Cancel", new ClickHandler() {
          public void onClick(ClickEvent event){ strLogName = null; d.hide();}
        });

        FlexTable flexTable = new FlexTable();
        setWidget(flexTable);
        flexTable.setSize("323px", "198px");

        Label lblLogName = new Label("Log Name:");
        flexTable.setWidget(0, 0, lblLogName);

        flexTable.setWidget(0, 1, textBox);

        int iRowNum = 1;
        for ( int x = 0; x < arSDevice.size(); x++ )
        {
            CheckBox cb1 = new CheckBox(arSDevice.get(x).getName());
            flexTable.setWidget(iRowNum, 1, cb1);
            arCheckBoxList.add( cb1 );
            iRowNum++;
        }
        //Button btnCancel = new Button("Cancel");
        flexTable.setWidget(iRowNum, 1, cancelButton);

        Button btnCreateAndStart = new Button("Create and Start", this );
        flexTable.setWidget( iRowNum, 2, btnCreateAndStart);

        flexTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        flexTable.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT);
        flexTable.getFlexCellFormatter().setColSpan(0, 1, 2);
        flexTable.getCellFormatter().setHorizontalAlignment(iRowNum, 1, HasHorizontalAlignment.ALIGN_LEFT);


    }

    public void onClick(ClickEvent event)
    {
        ArrayList<SDevice> arSD = new ArrayList<SDevice>();

        for ( CheckBox cb : arCheckBoxList )
        {
           if ( cb.getValue() )
           {
               for ( SDevice sd : arDeviceList )
               {
                   if ( sd.getName().compareTo(cb.getText()) == 0 )
                   {
                       arSD.add( sd );

                   }
               }
           }
        }
        dialogHandler.onReturn(textBox.getText().replace(" ", "_"), arSD );
        this.hide();

    }

}

