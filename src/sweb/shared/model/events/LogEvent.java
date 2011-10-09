package sweb.shared.model.events;

import java.io.Serializable;


public class LogEvent implements Serializable
{
   public enum LogEventType { NONE, NEW, UPDATED, DELETED }

   private LogEventType m_LogType = LogEventType.NONE;
   private String m_CookerName;
   private String m_LogName;

   private static final long serialVersionUID = 1L;

   private LogEvent()
   {

   }

   public LogEvent( LogEventType et, String cookerName, String logName )
   {
      m_LogType = et;
      m_CookerName = cookerName;
      m_LogName = logName;
   }

   public LogEventType getEventType()
   {
       return m_LogType;
   }
   
   public void setCookerName( String cookerName )
   {
      m_CookerName = cookerName;
   }
   
   public void setLogName( String logName )
   {
      m_LogName = logName;
   }
   
   public String getCookerName()
   {
      return m_CookerName;
   }
   
   public String getLogName()
   {
      return m_LogName;
   }
}
