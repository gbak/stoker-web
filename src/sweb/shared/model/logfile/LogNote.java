package sweb.shared.model.logfile;

import java.io.Serializable;
import java.util.Date;

public class LogNote implements Serializable
{

    private static final long serialVersionUID = 1461156812231083124L;

    Date logNoteDate;
    String logNote;
    
    private LogNote() { }
    public LogNote( Date d, String logNote) { logNoteDate = d; this.logNote = logNote; }
    
    public String getNote() { return logNote; }
    public Date   getNoteDate() { return logNoteDate; }
    public void setNote( String s ) { logNote = s; }
    public void setNoteDate( Date d ) { logNoteDate = d; }
  
    
    
}
