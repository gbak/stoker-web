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

import sweb.client.dialog.handlers.LogFileChooserHandler;
import sweb.shared.model.logfile.LogDir;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

public class LogFileChooser extends DialogBox implements ClickHandler
{
    Tree tree = new Tree();
    LogFileChooserHandler handler = null;
    LogDir logDirSaved = null;

    public LogFileChooser(LogDir ld, LogFileChooserHandler logFileChooserHandler )
    {

        logDirSaved = ld;

        VerticalPanel verticalPanel = new VerticalPanel();

        this.handler = logFileChooserHandler;

        tree.setHeight("343px");
        tree.setWidth( "450px");
        tree.setAnimationEnabled(true);
        ScrollPanel treeWrapper = new ScrollPanel(tree);

        tree.addItem( buildTree(ld));

        verticalPanel.add(treeWrapper);

        HorizontalPanel horizontalPanel = new HorizontalPanel();
        verticalPanel.add(horizontalPanel);
        horizontalPanel.setWidth("100%");

        final DialogBox d = this;
        Button buttonCancel = new Button("Cancel", new ClickHandler() {
            public void onClick(ClickEvent event){ d.hide();}
          });

        horizontalPanel.add(buttonCancel);
        horizontalPanel.setCellHorizontalAlignment(buttonCancel, HasHorizontalAlignment.ALIGN_CENTER);

        Button buttonSelect = new Button("Select");
        buttonSelect.addClickHandler(this);
        horizontalPanel.add(buttonSelect);
        horizontalPanel.setCellHorizontalAlignment(buttonSelect, HasHorizontalAlignment.ALIGN_CENTER);

        setWidget(verticalPanel);
    }

    private TreeItem buildTree( LogDir ld )
    {
        TreeItem tiMain = new TreeItem(ld.getName());

        ArrayList<String> listFiles = ld.getFileList();
        ArrayList<LogDir> listDirs = ld.getDirList();

        for ( String s : listFiles )
        {

            tiMain.addItem(s);
        }

        for ( LogDir d : listDirs )
        {
            TreeItem tiDir = buildTree( d );
            tiMain.addItem( tiDir );
        }
        return tiMain;
    }

    public void onClick(ClickEvent event)
    {
        final DialogBox d = this;
        TreeItem ti  = tree.getSelectedItem();
        // TODO: add error here if file not selected
        handler.onReturn(ti.getText());
        this.hide();
    }
}
