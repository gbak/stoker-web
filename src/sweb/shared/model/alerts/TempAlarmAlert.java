package sweb.shared.model.alerts;

import java.io.Serializable;

public class TempAlarmAlert extends AlertBase implements Serializable
{

   private static final long serialVersionUID = 934933626861181813L;
   private static final String m_TempAlertName = "Temp Alarm"; 
   
   public TempAlarmAlert()  { super( m_TempAlertName );  }
   
   public TempAlarmAlert( boolean b )
   {
      super( m_TempAlertName, b );
   }

}
