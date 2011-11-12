package sweb.server.controller.alerts.delivery;

import java.io.Serializable;

import sweb.server.ClientMessagePusher;

public class BrowserDelivery
{

    public static void send( Serializable message )
    {
        ClientMessagePusher.getInstance().push( message );
    }
}
