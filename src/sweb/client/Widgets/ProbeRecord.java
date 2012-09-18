/**
 *  Stoker-web
 *
 *  Copyright (C) 2012  Gary Bak
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package sweb.client.widgets;

import com.smartgwt.client.widgets.grid.ListGridRecord;

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
