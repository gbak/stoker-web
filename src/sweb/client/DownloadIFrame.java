package sweb.client;

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
