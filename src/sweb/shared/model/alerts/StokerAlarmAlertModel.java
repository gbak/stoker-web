package sweb.shared.model.alerts;

import java.io.Serializable;

public class StokerAlarmAlertModel extends AlertModel implements Serializable
{
   private static final long serialVersionUID = -3312639618804325814L;

   private static final String m_StokerAlertName = "Stoker Alarm"; 

   public StokerAlarmAlertModel()
   {
      super( m_StokerAlertName );
   }

   public StokerAlarmAlertModel(boolean b)
   {
      super( m_StokerAlertName, b );
   }
}
