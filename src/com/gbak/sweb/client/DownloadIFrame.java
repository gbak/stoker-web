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

package com.gbak.sweb.client;

import com.google.gwt.event.dom.client.HasLoadHandlers;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;

public class DownloadIFrame extends Frame implements LoadHandler,
        HasLoadHandlers
{
    public static final String DOWNLOAD_FRAME = "__gwt_downloadFrame";

    public DownloadIFrame(String url)
    {
        super();
        setSize("0px", "0px");
        setVisible(false);
        RootPanel rp = RootPanel.get(DOWNLOAD_FRAME);
        if (rp != null)
        {
            addLoadHandler(this);
            rp.add(this);
            setUrl(url);
        }
        else
            openURLInNewWindow(url);
    }

    native void openURLInNewWindow(String url) /*-{
		$wnd.open(url);
    }-*/;

    public HandlerRegistration addLoadHandler(LoadHandler handler)
    {
        return addHandler(handler, LoadEvent.getType());
    }

    public void onLoad(LoadEvent event)
    {
    }
}
