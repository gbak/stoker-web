package sweb.shared.model.alerts;

import java.io.Serializable;
import java.util.Date;

public class SoundAlarm  implements Serializable
{
    private static final long serialVersionUID = 6295441263225889854L;

    String m_AlarmName = "";
    String m_SupressAlarmDuration = "";
    Date   m_AlarmSupressTime = null;
    
    public SoundAlarm() { }
    public SoundAlarm( String AlarmName ) { m_AlarmName = AlarmName; }
    
}
