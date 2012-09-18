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

import com.allen_sauer.gwt.log.client.Log;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;
import com.smartgwt.client.widgets.events.DropMoveEvent;
import com.smartgwt.client.widgets.events.DropMoveHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;

public class ConfigurationListGrid extends ListGrid 
{
    public ConfigurationListGrid(String type) 
    {
        setWidth(310);
        setCellHeight(25);
        setShowEdges(true);
        setEdgeSize(3);
        setBorder("0px");
        setBodyStyleName("normal");
        setShowHeader(true);
        setLeaveScrollbarGap(true);
        setCanReorderRecords(true);  
        setCanAcceptDroppedRecords(true);  
        setCanDragRecordsOut(true);  
        
        setDragType(type);
        setDropTypes(type);
       
      
       // setAutoFitData(Autofit.BOTH);   
        setAutoFitWidthApproach(AutoFitWidthApproach. BOTH);
            
        addDropHandler(new DropHandler() {

            @Override
            public void onDrop(DropEvent event)
            {
                Log.debug(event.getSource().getClass().toString());
                
            }} );
        
        addDropMoveHandler(new DropMoveHandler() {

            @Override
            public void onDropMove(DropMoveEvent event)
            {
                // TODO Auto-generated method stub
                
            }
            
        });

        ListGridField probeIDField = new ListGridField("probeId",115);
        ListGridField probeTypeField = new ListGridField("probeType", 60 );
        ListGridField probeNameField = new ListGridField("probeName", 120);
        probeNameField.setCanEdit( true );

        ListGridField[] lgf = new ListGridField[] { probeNameField, probeTypeField, probeIDField };
        setDefaultFields( lgf );
        
    }
    
}
