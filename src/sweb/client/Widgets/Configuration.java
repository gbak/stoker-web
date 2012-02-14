package sweb.client.Widgets;

import java.util.ArrayList;

import sweb.server.controller.StokerConfiguration;
import sweb.server.controller.StokerWebConfiguration;
import sweb.shared.model.devices.SDevice;

import com.google.gwt.user.client.ui.Composite;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.DragDataAction;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.layout.HStack;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;

public class Configuration extends VLayout
{

    ArrayList<SDevice> stokerConf = null;
    
    public Configuration(ArrayList<SDevice> arsd)
    {
        stokerConf = arsd;
        
        HStack hStack = new HStack(10);  
        hStack.setHeight(200);  
  
        final ConfigurationListGrid myList1 = new ConfigurationListGrid();  
        myList1.setCanDragRecordsOut(true);  
        myList1.setCanAcceptDroppedRecords(true);  
        myList1.setCanReorderFields(true);  
        myList1.setDragDataAction(DragDataAction.MOVE);  
        myList1.setData(getData());  
        hStack.addMember(myList1);  
  
        final ConfigurationListGrid myList2 = new ConfigurationListGrid();  
        myList2.setCanDragRecordsOut(true);  
        myList2.setCanAcceptDroppedRecords(true);  
        myList2.setCanReorderRecords(true);  
        
  
        VStack vStack = new VStack(10);  
        vStack.setWidth(32);  
        vStack.setHeight(74);  
        vStack.setLayoutAlign(Alignment.CENTER);  
  
       /* TransferImgButton rightImg = new TransferImgButton(TransferImgButton.RIGHT);  
        rightImg.addClickHandler(new ClickHandler() {  
            public void onClick(ClickEvent event) {  
                myList2.transferSelectedData(myList1);  
            }  
        });  
        vStack.addMember(rightImg);  
  
        TransferImgButton leftImg = new TransferImgButton(TransferImgButton.LEFT);  
        leftImg.addClickHandler(new ClickHandler() {  
            public void onClick(ClickEvent event) {  
                myList1.transferSelectedData(myList2);  
            }  
        });  
        vStack.addMember(leftImg);  
  */
        
        hStack.addMember(vStack);  
        hStack.addMember(myList2);  
  
        this.addMember( hStack );
        hStack.draw();  
    
    }
    
    private void getStokerConfiguration()
    {
        
    }
    
    private RecordList getData()
    {
        RecordList rl = new RecordList();
        for ( SDevice sd : stokerConf )
        {
           ProbeRecord r = new ProbeRecord(sd.getID(), sd.getName());
           rl.add( r );
        }
        return rl;
    }
    
    
    class ConfigurationListGrid extends ListGrid
    {
        ConfigurationListGrid() {
            setWidth(150);
            setCellHeight(24);
            setImageSize(16);
            setShowEdges(true);
            setBorder("0px");
            setBodyStyleName("normal");
            setShowHeader(false);
            setLeaveScrollbarGap(false);
            setEmptyMessage("<br><br>Drag &amp; drop probes here");

            //ListGridField partSrcField = new ListGridField("partSrc", 24);
         //   partSrcField.setType(ListGridFieldType.IMAGE);
          //  partSrcField.setImgDir("pieces/16/");

            ListGridField probeIDField = new ListGridField("probeId");
            
            ListGridField probeNameField = new ListGridField("probeName", 20);

            setFields( probeIDField, probeNameField);

          //  setTrackerImage(new ImgProperties("pieces/24/cubes_all.png", 24, 24));
        }
        
    }
    
}
