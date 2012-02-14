package sweb.client.Widgets;

import com.smartgwt.client.widgets.grid.ListGridRecord;

public class ProbeRecord extends ListGridRecord
{

    public ProbeRecord()
    {
        
    }
    
    public ProbeRecord( String ID, String name )
    {
        setID(ID);
        setName(name);
    }
    
    public void setID( String id )
    {
        setAttribute("probeId", id );
    }
    
    public void setName( String name )
    {
        setAttribute("probeName", name );
    }
}
