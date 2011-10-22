package sweb.shared.model.alerts;

import java.io.Serializable;

public abstract class AlertBase implements Serializable
{
   private static final long serialVersionUID = 3533993719511613483L;

   String  m_AlertName = "";
   boolean m_enabledInd = false;
   
   public AlertBase()
   {
   }
   
   public AlertBase(String name)
   {
      m_AlertName = name;
   }
   
   public AlertBase( String name, boolean b)
   {
      m_AlertName = name;
      m_enabledInd = b;
   }
   
   public boolean getEnabled()
   {
      return m_enabledInd;
   }
   
   public void setEnabled( boolean b)
   {
      m_enabledInd = b;
   }
   
   public String getName()
   {
      return m_AlertName;
   }
   
   public void setName( String s)
   {
      m_AlertName = s;
   }
   
}
