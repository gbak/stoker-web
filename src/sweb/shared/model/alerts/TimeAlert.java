package sweb.shared.model.alerts;

import java.io.Serializable;

public class TimeAlert extends AlertBase implements Serializable
{

   private static final long serialVersionUID = 1L;
   private static final String m_TimeAlertName = "Time Alert";

   public TimeAlert() { super( m_TimeAlertName ); }
   public TimeAlert( boolean b )
   {
      super( m_TimeAlertName, b );
   }
}
