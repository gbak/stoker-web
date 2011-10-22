package sweb.server.controller.alerts.delivery;

import java.util.ArrayList;

public interface AlertDelivery
{

   public void init();
   public void sendAlert( ArrayList<String> message );
   
}
