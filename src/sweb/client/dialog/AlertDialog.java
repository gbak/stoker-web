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

import sweb.client.StokerCoreServiceAsync;
import sweb.client.dialog.handlers.AlertDialogHandler;
import sweb.shared.model.alerts.BrowserAlarmModel;

import com.allen_sauer.gwt.voices.client.Sound;
import com.allen_sauer.gwt.voices.client.Sound.LoadState;
import com.allen_sauer.gwt.voices.client.SoundController;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

public class AlertDialog extends DialogBox
{
    FlexTable flexTable = null;
    StokerCoreServiceAsync gsa = null;
    AlertDialogHandler alertDialogHandler = null;
    BrowserAlarmModel browserAlarm = null;
    
    SoundController soundController = new SoundController();
    Sound sound = null;
   
    public AlertDialog(StokerCoreServiceAsync g, BrowserAlarmModel bam, AlertDialogHandler alertDialogHandler2 )
    {
        super();

        gsa = g;
        this.alertDialogHandler = alertDialogHandler2;
        browserAlarm = bam;
        
        //this.setWidth("100%");
        setText("Alert!");
        
        setGlassEnabled(true);
        setAnimationEnabled(true);
     
        FlexTable ft = new FlexTable();
        
        //HTML ht = new HTML(bam.getMessage());
        Label ht = new Label(bam.getMessage());
        ft.setWidget(0, 1, ht);
        Button silenceButton = new Button("Silence");
        silenceButton.addClickHandler(silenceButtonClickHandler());
        Button closeButton = new Button("Close");
        closeButton.addClickHandler(closeButtonClickHandler());
        
        ft.setWidget(1, 0, silenceButton);
        ft.setWidget(1, 2, closeButton);
        
        playSound();
        setWidget( ft);
    }
    
    private ClickHandler closeButtonClickHandler()
    {
        final DialogBox db = this;
        // Add a handler to close the DialogBox
        return new ClickHandler()
        {
           public void onClick(ClickEvent event)
           {
              stopSound();
              db.hide();
           }
        };
    }
    
    private ClickHandler silenceButtonClickHandler()
    {
        final DialogBox db = this;
        // Add a handler to close the DialogBox
        return new ClickHandler()
        {
           public void onClick(ClickEvent event)
           {
               stopSound();
           }
        };
    }
    
    private void playSound()
    {
        sound = soundController.createSound(Sound.MIME_TYPE_AUDIO_WAV_PCM, "Alarm1.wav");
        LoadState s = sound.getLoadState();
        
        sound.play();
    }
    
    private void stopSound()
    {
        sound.stop();
    }
}
