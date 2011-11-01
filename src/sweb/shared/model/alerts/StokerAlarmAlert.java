package sweb.shared.model.alerts;

import java.io.Serializable;

public class StokerAlarmAlert extends Alert implements Serializable
{
   private static final long serialVersionUID = -3312639618804325814L;

   private static final String m_StokerAlertName = "Stoker Alarm"; 

   public StokerAlarmAlert()
   {
      super( m_StokerAlertName );
   }

   public StokerAlarmAlert(boolean b)
   {
      super( m_StokerAlertName, b );
   }
}
