package sweb.shared.model.alerts;

public class ConnectionChangeAlert extends AlertBase
{
   private static final long serialVersionUID = 7846663410227329176L;

   private static final String m_ConnectionChangeAlertName = "Connection Change Alarm";
   
   public ConnectionChangeAlert()
   {
      super( m_ConnectionChangeAlertName );
   }
   
   public ConnectionChangeAlert( boolean b )
   {
      super( m_ConnectionChangeAlertName, b );
   }
}
