package sweb.server.controller.alerts.delivery;

import java.io.Serializable;

import com.google.inject.Inject;

import sweb.server.ClientMessenger;

public class BrowserDelivery
{

    private static ClientMessenger clientMessenger;
    
    @Inject
    private BrowserDelivery( ClientMessenger cm )
    {
        clientMessenger = cm;
    }
    
    public static void send( Serializable message )
    {
        
        clientMessenger.push( message );
    }
}
