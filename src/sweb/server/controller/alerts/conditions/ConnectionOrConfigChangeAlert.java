package sweb.server.controller.alerts.conditions;

import sweb.shared.model.alerts.AlertBase;
import sweb.shared.model.alerts.ConnectionChangeAlert;

public class ConnectionOrConfigChangeAlert extends Alert
{

   ConnectionChangeAlert cca = null;
   
   public ConnectionOrConfigChangeAlert() { super(); }
   public ConnectionOrConfigChangeAlert( boolean b ) { super(b); }
   @Override
   public void setAlertConfiguration(AlertBase ab)
   {
      if ( ab instanceof ConnectionChangeAlert )
         cca = (ConnectionChangeAlert) ab;
      // TODO: error on else condition
      
   }
   @Override
   public AlertBase getAlertConfiguration()
   {
      
      return (AlertBase) cca;
   }
   
   
}
