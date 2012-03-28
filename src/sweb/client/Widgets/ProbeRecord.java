package sweb.client.widgets;

import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.tree.TreeGridField;
import com.smartgwt.client.widgets.tree.TreeNode;

public class ProbeRecord extends ListGridRecord
{

    public ProbeRecord()
    {
        
    }
    
    public ProbeRecord( String ID, String name, String type, ProbeRecord...  children )
    {
        setID(ID);
        setName(name);
        setType( type );
    }
    
    public ProbeRecord( String ID, String name, String type )
    {
        this( ID, name, type, new ProbeRecord[]{} );
    }
    public ProbeRecord( String ID, ProbeRecord... probeRecords )
    {
        this( ID, null, null, probeRecords );
    }
    public ProbeRecord( String ID  )
    {
        this( ID, null, null );
    }
    
    public void setID( String id )
    {
        setAttribute("probeId", id );
    }
    
    public void setName( String name )
    {
        setAttribute("probeName", name );
    }
    public void setType( String type )
    {
        setAttribute("probeType", type );
    }
    
    public String getID()
    {
        return getAttribute("probeId");
    }
    
    public String getName()
    {
        return getAttribute("probeName");
    }
    
    public String getType()
    {
        return getAttribute("probeType");
    }
}
