package sweb.server.controller.alerts.conditions;

import sweb.shared.model.alerts.AlertModel;
import sweb.shared.model.alerts.ConnectionChangeAlertModel;

public class ConnectionOrConfigChangeAlert extends AlertCondition
{

   ConnectionChangeAlertModel cca = null;
   
   public ConnectionOrConfigChangeAlert() { super(); }
   public ConnectionOrConfigChangeAlert( boolean b ) { super(b); }
   @Override
   public void setAlertConfiguration(AlertModel ab)
   {
      if ( ab instanceof ConnectionChangeAlertModel )
         cca = (ConnectionChangeAlertModel) ab;
      // TODO: error on else condition
      
   }
   @Override
   public AlertModel getAlertConfiguration() 
   {
      
      return (AlertModel) cca;
   }
   
   
}
