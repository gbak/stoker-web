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

import java.util.ArrayList;
import java.util.HashMap;

import sweb.client.widgets.handlers.ConfigUpdateHandler;
import sweb.shared.model.ConfigurationSettings;
import sweb.shared.model.Cooker;
import sweb.shared.model.CookerList;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.devices.stoker.StokerPitProbe;
import sweb.shared.model.devices.stoker.StokerProbe;
import sweb.shared.model.stoker.StokerDeviceTypes.DeviceType;

import com.allen_sauer.gwt.log.client.Log;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.DragDataAction;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tab.events.CloseClickHandler;
import com.smartgwt.client.widgets.tab.events.TabCloseClickEvent;

public class Configuration extends Dialog
{

    private ArrayList<SDevice> stokerConfAvailable = null;
    private ArrayList<SDevice> stokerConf = null;
    private TabSet tabSet = null;
    CookerList cookerList = null;
    
    private ConfigUpdateHandler configHandler;
    
    public Configuration(ConfigurationSettings settings)
    {
        Log.debug("Configuration constructor");
        stokerConf = settings.getAvailableDevices();
        cookerList = settings.getCookerList();
        
        if ( cookerList == null )
            cookerList = new CookerList();
        
        removeAlreadyAssignedDevices();
        
        HLayout hStack = new HLayout(20);
       // hStack.setHeight(400); 
        hStack.setHeight100();
        hStack.setWidth100();
        hStack.setAlign(Alignment.CENTER);
        hStack.setMargin(10);
  
        VLayout sourceProbes = new VLayout(10);
        
        final ConfigurationListGrid tempProbeList = new ConfigurationListGrid("temp");  
        tempProbeList.setCanDragRecordsOut(true);  
        tempProbeList.setCanAcceptDroppedRecords(true);  
        tempProbeList.setCanReorderFields(true);  
        tempProbeList.setDragDataAction(DragDataAction.MOVE); 
        
      //  tempProbeList.setMargin(10);  // crushes the box and enables the scroll
       
       tempProbeList.setData(getData("temp"));  
  
        final ConfigurationListGrid blowerProbeList = new ConfigurationListGrid("blower");  
        blowerProbeList.setCanDragRecordsOut(true);  
        blowerProbeList.setCanAcceptDroppedRecords(true);  
        blowerProbeList.setCanReorderRecords(true);  
        blowerProbeList.setData(getData("blower"));
        
        
        Log.debug("Configuration: Created TreeGrids");
  
        sourceProbes.addMember( tempProbeList );
        sourceProbes.addMember( blowerProbeList );
     
        hStack.addMember(sourceProbes);
  
        tabSet = new TabSet();
        tabSet.setTabBarPosition(Side.TOP);  
        tabSet.setWidth(400);  
        tabSet.setHeight100();   
  
        
        VLayout buttonLayout = new VLayout(10);
     //   buttons.setMembersMargin(15);
        
        IButton addButton = new IButton("Add Cooker");
        addButton.addClickHandler(new ClickHandler() {  
            public void onClick(ClickEvent event) 
            {  
                
                
                if (tabSet.getTabs().length == 0) {  
                    tabSet.selectTab(0);  
                }  
                final Tab tTab = new Tab("Cooker");  
                
                tTab.setCanClose(true);  
                
                ChangedHandler tabTitleChangeHandler = new ChangedHandler() {  
                    public void onChanged(ChangedEvent event) {  
                        String newTitle = (event.getValue() == null ? "" : (String)event.getValue());  
                       tabSet.setTabTitle(tTab, newTitle);  
                        
                    }  
                };
                
                tTab.setPane( new ConfigurationTabPane(null, tabTitleChangeHandler));  
                tabSet.addTab(tTab);  
                
            }  
        });  

        buttonLayout.addMember(addButton);
        
        final Dialog dialog = this;
        IButton updateButton = new IButton("Update");
        updateButton.addClickHandler(new ClickHandler() {  
            public void onClick(ClickEvent event) 
            {  
                buildCooker();
                configHandler.onUpdate( cookerList );
                
            }  
        });  
        
        buttonLayout.addMember(updateButton);
 
        IButton cancelButton = new IButton("Cancel");
        cancelButton.addClickHandler( new ClickHandler() {  
            public void onClick(ClickEvent event) 
            {  
                dialog.cancelClick();
                
            }  
        });  
        
        buttonLayout.addMember(cancelButton);

        // Added existing cookers...
        if ( cookerList != null )
        {
            for ( Cooker cooker : cookerList.getCookerList())
            {
                final Tab tTab = new Tab(cooker.getCookerName());  
                
                tTab.setCanClose(true);  
              
                tTab.setPane(new ConfigurationTabPane( cooker, getTabTitleChangeHander(tTab)));  
                tabSet.addTab(tTab);  
            }
        }
        
        
        VLayout tabPane = new VLayout();
        tabPane.addMember( tabSet );
    // tabPane.addMember( buttonLayout );

        Log.debug("Configuration: adding members");
       
       // hStack.addMember(myList2);  
        tabSet.addCloseClickHandler(new CloseClickHandler() {

            @Override
            public void onCloseClick(TabCloseClickEvent event)
            {
                ConfigurationTabPane ctp = (ConfigurationTabPane) event.getTab().getPane();
                for ( ProbeRecord pr : ctp.onPaneClose())
                {
                    if( pr.getAttribute("probeType").compareToIgnoreCase("temp") == 0)
                    {
                        tempProbeList.addData( pr );
                    }
                    else
                    {
                        blowerProbeList.addData( pr );
                    }
                }
            }
            
        });
  
        hStack.addMember( tabSet );
        hStack.addMember( buttonLayout );
       // this.addMember( hStack );
        this.addItem( hStack );
        //this.addMember( buttonLayout );
        this.setAlign(Alignment.CENTER);
        hStack.draw();  
    
    }
    
    /**
     * This removes all devices from the stokerConf list that have already been assigned to a cooker.
     * 
     */
    private void removeAlreadyAssignedDevices()
    {
        HashMap<String,SDevice> deviceHash = new HashMap<String,SDevice>();
       
        for ( SDevice sd : stokerConf)
        {
            deviceHash.put( sd.getID(),sd);
        }
        
        if ( cookerList != null )
        {
            for ( Cooker cooker : cookerList.getCookerList() )
            {
                StokerPitProbe pit = cooker.getPitSensor();
                if ( pit != null )
                {
                    if ( deviceHash.containsKey(pit.getID()))
                    {
                       deviceHash.remove(pit.getID());    
                    }
                    if ( pit != null && pit.getFanDevice() != null )
                    {
                        String fanID =pit.getFanDevice().getID();;
                        if ( deviceHash.containsKey( fanID ))
                        {
                            deviceHash.remove(fanID);
                        }
                    }
                    
                }
                for ( StokerProbe sp : cooker.getProbeList() )
                {
                    if ( deviceHash.containsKey(sp.getID()))
                    {
                        deviceHash.remove( sp.getID());
                    }
                }
            }
        }
        stokerConfAvailable = new ArrayList<SDevice>(deviceHash.values());
    }
    
    private ChangedHandler getTabTitleChangeHander(Tab tab)
    {
        final Tab tTab = tab;
        ChangedHandler tabTitleChangeHandler = new ChangedHandler() {  
    
            public void onChanged(ChangedEvent event) {  
                String newTitle = (event.getValue() == null ? "" : (String)event.getValue());  
               tabSet.setTabTitle(tTab, newTitle);  
                
            }  
        };
        return tabTitleChangeHandler;
    }

    private void buildCooker()
    {
        ArrayList<Cooker> clist = new ArrayList<Cooker>();
        
        Tab[] ta = tabSet.getTabs();
        for ( int t = 0; t < ta.length; t++)
        {
            Tab tab = ta[t];
            
            ConfigurationTabPane ct = (ConfigurationTabPane) tab.getPane();
            Cooker c = ct.getCooker( stokerConf );
            clist.add( c );
        }
        cookerList.setCookerList(clist);

    }

    private void getStokerConfiguration()
    {
        
    }
    
    private RecordList getData(String type)
    {
        RecordList rl = new RecordList();
        for ( SDevice sd : stokerConfAvailable )
        {
            DeviceType dt = sd.getProbeType();
            String deviceTypeString;
            if (( dt.toString().compareToIgnoreCase("PIT") == 0 ) || (dt.toString().compareToIgnoreCase("FOOD") == 0 ))
            {
                deviceTypeString = "Temp";
                if ( dt.toString().compareToIgnoreCase("PIT") == 0)
                {
                    StokerPitProbe sp = (StokerPitProbe) sd;
                    sp.setFanDevice(null);
                }
            }
            else
                deviceTypeString = "Blower";
           
            if ( type.compareToIgnoreCase(deviceTypeString) == 0)
            {
               ProbeRecord r = new ProbeRecord(sd.getID(), sd.getName(), deviceTypeString );
           
               rl.add( r );
            }
           
        }
        return rl;
    }
    
     public void addUpdateHandler( ConfigUpdateHandler configUpdateHandler )
     {
         configHandler = configUpdateHandler;
     }
    /*
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
    */
    
}
