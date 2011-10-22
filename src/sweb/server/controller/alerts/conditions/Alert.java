package sweb.server.controller.alerts.conditions;

import sweb.shared.model.alerts.AlertBase;

public abstract class Alert
{

   boolean m_Enabled = false;
   String  m_Identifier = "";
   
   Alert() { }
   public Alert( boolean b ) { m_Enabled = b; }
   
   public void setEnabled( boolean b ) { m_Enabled = b; }
   public boolean getEnabled()  { return m_Enabled; }
   
   public void setIdentifier( String id ) { m_Identifier = id; }
   public String getIdentifier( ) { return(m_Identifier); }
   
   public abstract void setAlertConfiguration( AlertBase ab );
   public abstract AlertBase getAlertConfiguration();
   
   
}
