package sweb.client.widgets;

import com.allen_sauer.gwt.log.client.Log;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;
import com.smartgwt.client.widgets.events.DropMoveEvent;
import com.smartgwt.client.widgets.events.DropMoveHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;

public class ConfigurationListGrid extends ListGrid 
{
    public ConfigurationListGrid(String type) 
    {
        setWidth(310);
        setCellHeight(25);
       // setImageSize(16);
        setShowEdges(true);
        setEdgeSize(3);
        setBorder("0px");
        setBodyStyleName("normal");
        setShowHeader(true);
        setLeaveScrollbarGap(true);
        setEmptyMessage("<br><br>Drag &amp; drop probes here");
        setCanReorderRecords(true);  
        setCanAcceptDroppedRecords(true);  
        setCanDragRecordsOut(true);  
        
        setDragType(type);
        setDropTypes(type);
        
        setScrollbarSize(8);
      
       // setAutoFitData(Autofit.BOTH);   
        setAutoFitWidthApproach(AutoFitWidthApproach. BOTH);
        
      /*  addNodeClickHandler(new NodeClickHandler() {           
                 @Override
                 public void onNodeClick(NodeClickEvent event) {
                        String name = event.getNode().getName();
                        SC.say("Node Clicked: " + name);
                 }
             });

*/
                
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

        setFields( probeNameField, probeTypeField, probeIDField );

      //  setTrackerImage(new ImgProperties("pieces/24/cubes_all.png", 24, 24));
    }
    
}
