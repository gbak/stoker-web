package sweb.server.controller.alerts.conditions;

import sweb.shared.model.alerts.Alert;
import sweb.shared.model.alerts.ConnectionChangeAlert;

public class ConnectionOrConfigChangeAlert extends AlertCondition
{

   ConnectionChangeAlert cca = null;
   
   public ConnectionOrConfigChangeAlert() { super(); }
   public ConnectionOrConfigChangeAlert( boolean b ) { super(b); }
   @Override
   public void setAlertConfiguration(Alert ab)
   {
      if ( ab instanceof ConnectionChangeAlert )
         cca = (ConnectionChangeAlert) ab;
      // TODO: error on else condition
      
   }
   @Override
   public Alert getAlertConfiguration() 
   {
      
      return (Alert) cca;
   }
   
   
}
