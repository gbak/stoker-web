package sweb.shared.model.alerts;

import java.io.Serializable;
import java.util.Date;

public class BrowserAlarmModel  implements Serializable
{
    private static final long serialVersionUID = 6295441263225889854L;

    String m_AlarmName = "";
    String m_Message = "";
    String m_SupressAlarmDuration = "";
    Date   m_AlarmSupressTime = null;
    
    public BrowserAlarmModel() { }
    public BrowserAlarmModel( String AlarmName ) { m_AlarmName = AlarmName; }
    
    public String getMessage() { return m_Message; }
    public void setMessage( String message) { m_Message = message; }
    
}
