package sweb.client.widgets;

import java.util.ArrayList;
import java.util.HashMap;

import sweb.server.controller.StokerConfiguration;
import sweb.server.controller.StokerWebConfiguration;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.stoker.StokerDeviceTypes.DeviceType;
import sweb.shared.model.stoker.StokerFan;
import sweb.shared.model.stoker.StokerProbe;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.ui.Composite;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.DragDataAction;
import com.smartgwt.client.types.GroupStartOpen;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.HStack;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeGridField;
import com.smartgwt.client.widgets.tree.TreeNode;

import com.smartgwt.client.widgets.events.ClickEvent;  
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;

public class Configuration extends VLayout
{

    private ArrayList<SDevice> stokerConf = null;
    private TabSet tabSet = null;
    
    public Configuration(ArrayList<SDevice> arsd)
    {
        Log.debug("Configuration constructor");
        stokerConf = arsd;
        
        HStack hStack = new HStack();
        hStack.setHeight(200); 
        hStack.setWidth100();
  
        final ConfigurationTreeGrid myList1 = new ConfigurationTreeGrid();  
        myList1.setCanDragRecordsOut(true);  
        myList1.setCanAcceptDroppedRecords(true);  
        myList1.setCanReorderFields(true);  
        myList1.setDragDataAction(DragDataAction.MOVE); 
   //     myList1.setGroupStartOpen(GroupStartOpen.ALL);  
   //     myList1.setGroupByField("probeType"); 
        
       myList1.setData(getData());  
        hStack.addMember(myList1);  
  
        final ConfigurationTreeGrid myList2 = new ConfigurationTreeGrid();  
        myList2.setCanDragRecordsOut(true);  
        myList2.setCanAcceptDroppedRecords(true);  
        myList2.setCanReorderRecords(true);  
        
        Log.debug("Configuration: Created TreeGrids");
  
        VStack vStack = new VStack(0);  
        vStack.setWidth(170);  
        vStack.setHeight(74);  
        vStack.setLayoutAlign(Alignment.CENTER);  
  
        tabSet = new TabSet();
        tabSet.setTabBarPosition(Side.TOP);  
        tabSet.setWidth(400);  
        tabSet.setHeight(200);   
        
        HLayout buttons = new HLayout();
        buttons.setMembersMargin(15);
        
        IButton addButton = new IButton("Add Cooker");
        addButton.addClickHandler(new ClickHandler() {  
            public void onClick(ClickEvent event) 
            {  
                
                if (tabSet.getTabs().length == 0) {  
                    tabSet.selectTab(0);  
                }  
            }  
        });  
        
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
        Log.debug("Configuration: adding members");
        hStack.addMember(vStack);  
        hStack.addMember(myList2);  
  
        this.addMember( hStack );
        hStack.draw();  
    
    }
    
    private void getStokerConfiguration()
    {
        
    }
    
    /*private RecordList getData()
    {
        RecordList rl = new RecordList();
        for ( SDevice sd : stokerConf )
        {
            DeviceType dt = sd.getProbeType();
            
           ProbeRecord r = new ProbeRecord(sd.getID(), sd.getName(), dt.toString());
           rl.add( r );
        }
        return rl;
    }*/
    private Tree getData()
    {
        Log.debug("Configuration getData()");
        Tree rl = new Tree();
        Log.debug("Adding root node");
        TreeNode rootNode = new ProbeRecord("Root");
        rl.setRoot( rootNode );
        rl.setModelType(TreeModelType.CHILDREN);
        
        HashMap<String,String> pitBlowerMap = new HashMap<String,String>();
        for (SDevice sd : stokerConf )
        {
            if ( sd.getProbeType() == DeviceType.BLOWER)
            {
                pitBlowerMap.put( sd.getID(), null);
            }
        }
        
        for ( SDevice sd : stokerConf )
        {
            TreeNode tn = null;
            //Skip blower devices that are attached to probes.
            if ( pitBlowerMap.containsKey(sd.getID()))
            {
                continue;
            }
            DeviceType dt = sd.getProbeType();
            
            
              Log.debug("Creating probeRecord"); 
              tn = new ProbeRecord(sd.getID(), sd.getName(), dt.toString());
             
              Log.debug("ProbeRecord created");
           
              rl.add(tn, rootNode );
              if ( dt == DeviceType.PIT )
              {
                  tn.setCanAcceptDrop(true);
                  
                  StokerProbe sp = (StokerProbe) sd;
                  StokerFan sf = sp.getFanDevice();
                  TreeNode blowerNode = new ProbeRecord( sf.getID(), sf.getName(), sf.getProbeType().toString() );
                  ProbeRecord p = new ProbeRecord("", "", "");
              
                  blowerNode.setCanAcceptDrop(false);
                  rl.add( blowerNode, tn);
              }
            
           
           Log.debug("Added probe to Root node");
           
           
           
        }
        return rl;
    }
    
    class ConfigurationTreeGrid extends TreeGrid
    {
        ConfigurationTreeGrid() {
            setWidth(400);
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
            
            addDropHandler(new DropHandler() {

                @Override
                public void onDrop(DropEvent event)
                {
                    Log.debug(event.getSource().getClass().toString());
                    
                }} );
            //ListGridField partSrcField = new ListGridField("partSrc", 24);
         //   partSrcField.setType(ListGridFieldType.IMAGE);
          //  partSrcField.setImgDir("pieces/16/");

            TreeGridField probeIDField = new TreeGridField("probeId",150);
            TreeGridField probeTypeField = new TreeGridField("probeType", 80 );
            TreeGridField probeNameField = new TreeGridField("probeName", 150);
            probeNameField.setCanEdit( true );

            setFields( probeNameField, probeTypeField, probeIDField );

          //  setTrackerImage(new ImgProperties("pieces/24/cubes_all.png", 24, 24));
        }
        
    }
    
}
