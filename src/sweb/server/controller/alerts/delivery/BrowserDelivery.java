package sweb.server.controller.alerts.delivery;

import java.io.Serializable;

import sweb.server.controller.Controller;

public class BrowserDelivery
{

    public static void send( Serializable message )
    {
        Controller.getInstance().getClientMessenger().push( message );
    }
}
