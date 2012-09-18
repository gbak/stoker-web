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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class GeneralMessageDialog extends DialogBox
{
    public GeneralMessageDialog(String title, String htmlMessage)
    {
        super();

        setText(title);

        setAnimationEnabled(true);
        final Button closeButton = new Button("Close");
        // We can set the id of a widget by accessing its Element
        closeButton.getElement().setId("closeButton");
        final Label textToServerLabel = new Label();
        final HTML serverResponseLabel = new HTML();
        VerticalPanel dialogVPanel = new VerticalPanel();
        dialogVPanel.addStyleName("alerts-dialog-panel");
        dialogVPanel.add(new HTML(htmlMessage));
        dialogVPanel.add(textToServerLabel);

        dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
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

    public void show(Button b)
    {
        final Button b2 = b;
        setPopupPositionAndShow(new PositionCallback() {

            public void setPosition(int offsetWidth, int offsetHeight)
            {
                int left = (Window.getClientWidth() - offsetWidth) / 3;
                int top = (Window.getClientHeight() - offsetHeight) / 3;
              setPopupPosition(left, top);

             //    setPopupPosition( b2.getAbsoluteLeft(), b2.getAbsoluteTop());


            }

        });


    }
}
