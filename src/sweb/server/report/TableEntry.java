package sweb.server.report;

import java.util.Date;

public class TableEntry
{

    public enum ActionType 
    {
        CONFIG { public String toString() { return "Config"; } },
        NOTE { public String toString() { return "Note"; } },
        LID_OPEN { public String toString() {   return "Lid Opened"; } }
   }
    
    ActionType act = null;
    Date       actionDate = null;
    String     messageString = null;
    
    public TableEntry( ActionType a, Date d, String m)
    {
        act = a;
        actionDate = d;
        messageString = m;
    }
    
    public ActionType getActionType()
    {
        return act;
    }
    
    public Date getActionDate()
    {
        return actionDate;
    }
    
    public String getMessageString()
    {
        return messageString;
    }
}
