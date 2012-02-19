package sweb.client.widgets;

import com.allen_sauer.gwt.log.client.Log;
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;

public class ConfigurationListGrid extends ListGrid 
{
    public ConfigurationListGrid() 
    {
        setWidth(300);
        setCellHeight(25);
        setImageSize(16);
        setShowEdges(true);
        setBorder("0px");
        setBodyStyleName("normal");
        setShowHeader(true);
        setLeaveScrollbarGap(false);
        setEmptyMessage("<br><br>Drag &amp; drop probes here");
        setCanReorderRecords(true);  
        setCanAcceptDroppedRecords(true);  
        setCanDragRecordsOut(true);  
        
      //  setDragType("");
        
      
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
        //ListGridField partSrcField = new ListGridField("partSrc", 24);
     //   partSrcField.setType(ListGridFieldType.IMAGE);
      //  partSrcField.setImgDir("pieces/16/");

        ListGridField probeIDField = new ListGridField("probeId",120);
        ListGridField probeTypeField = new ListGridField("probeType", 60 );
        ListGridField probeNameField = new ListGridField("probeName", 120);
        probeNameField.setCanEdit( true );

        setFields( probeNameField, probeTypeField, probeIDField );

      //  setTrackerImage(new ImgProperties("pieces/24/cubes_all.png", 24, 24));
    }
    
}
