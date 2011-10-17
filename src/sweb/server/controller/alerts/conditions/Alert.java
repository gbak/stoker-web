package sweb.server.controller.alerts.conditions;

public abstract class Alert
{

   boolean m_Enabled = false;
   
   Alert() { }
   public Alert( boolean b ) { m_Enabled = b; }
   
   public void setEnabled( boolean b ) { m_Enabled = b; }
   public boolean getEnabled()  { return m_Enabled; }
   
   
}
