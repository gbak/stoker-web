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



import sweb.client.dialog.handlers.LoginDialogHandler;
import sweb.client.StokerCoreServiceAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class LoginDialog extends DialogBox implements ClickHandler
{
    private static final String invalidLogin = "The username or password you entered is incorrect.";
   FlexTable loginPanel = new FlexTable();
   protected Long lReconnectCounter;
   protected ButtonBase time;
   StokerCoreServiceAsync gsa = null;
   TextBox userTextBox = new TextBox();
   PasswordTextBox passTextBox = new PasswordTextBox();
   LoginDialogHandler ldh = null;
   HTML loginMessage = new HTML("");


   public LoginDialog(StokerCoreServiceAsync g, sweb.client.dialog.handlers.LoginDialogHandler loginDialogHandler)
   {
      gsa = g;
       this.ldh = loginDialogHandler;
      setGlassEnabled(true);
      setAnimationEnabled(true);
      //setText("Please Sign In:");
       Button loginButton = new Button("Sign in", this);

       final DialogBox d = this;
       Button cancelButton = new Button("Cancel", new ClickHandler() {
         public void onClick(ClickEvent event){ d.hide();}
       });



       HTML userHTML = new HTML("<b>Username:</b>");
       HTML passHTML = new HTML("<b>Password:</b>");

       loginPanel.setWidget(0, 0, new Label("Please sign in:"));
       loginPanel.setWidget(1, 0, userHTML );
       loginPanel.setWidget(1, 1, userTextBox );
       loginPanel.setWidget(2, 0, passHTML );
       loginPanel.setWidget(2, 1, passTextBox );
       loginPanel.setWidget(3, 0, loginButton );
       loginPanel.setWidget(3, 1, loginMessage );
       loginPanel.setWidget(3, 2, cancelButton );
       loginPanel.getRowFormatter().addStyleName(3,"login-buttonRow");
       loginPanel.getFlexCellFormatter().setColSpan(0, 0, 3);
      // loginPanel.getFlexCellFormatter().setColSpan(3, 0, 3);

      // loginPanel.setStyleName("login-panel login-flexTable");

       setWidget( loginPanel );
   }


public void onClick(ClickEvent event)
   {
    final DialogBox d = this;
      gsa.login(
            userTextBox.getText(), passTextBox.getText(), new AsyncCallback<String>() {
                    public void onFailure(Throwable caught)
                    {
                        time.setText("FAILURE");
                    }

                    public void onSuccess(String result)
                    {
                       // Window.alert("Logged in result;: " + result );
                        if ( result == null )
                        {
                            loginMessage.setHTML(invalidLogin);
                            loginMessage.setStylePrimaryName("InvalidLoginText");
                        }
                        else
                        {
                           ldh.onLoginReturn( result );
                           d.hide();
                        }
                    }
            });

   }
}

