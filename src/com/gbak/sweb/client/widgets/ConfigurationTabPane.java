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

package com.gbak.sweb.client.widgets;

import java.util.ArrayList;
import java.util.HashMap;


import com.gbak.sweb.shared.model.Cooker;
import com.gbak.sweb.shared.model.devices.SDevice;
import com.gbak.sweb.shared.model.devices.stoker.StokerFan;
import com.gbak.sweb.shared.model.devices.stoker.StokerPitProbe;
import com.gbak.sweb.shared.model.devices.stoker.StokerProbe;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.VLayout;

public class ConfigurationTabPane extends VLayout
{

    VLayout vp = new VLayout();
    private final ConfigurationListGrid pitProbeRecord;
    private final ConfigurationListGrid blowerProbe;
    private final ConfigurationListGrid tempProbes;
    private TextItem nameTextItem;
    
    ConfigurationTabPane(Cooker cooker, ChangedHandler tabChangedHandler )
    {
        vp.setAlign(VerticalAlignment.TOP);
        vp.setHeight100();
        vp.setWidth100();
        vp.setLayoutMargin(5);
        vp.setMembersMargin(5);
        
        DynamicForm tabNameForm = new DynamicForm();  
        nameTextItem = new TextItem();  
        
        if ( cooker != null )
        {
            nameTextItem.setValue(cooker.getCookerName());
        }
        
        nameTextItem.setTitle("Cooker Name");  
        nameTextItem.addChangedHandler(tabChangedHandler);  
        tabNameForm.setFields( nameTextItem );
        
        Label pit = new Label("Pit Probe");
        pit.setHeight(25);
        pit.setWidth100();
        pitProbeRecord = new ConfigurationListGrid("temp");

        pitProbeRecord.setShowHeader(false);
        pitProbeRecord.setHeight(40);
        
        pitProbeRecord.addDropHandler(new DropHandler() {
            @Override
            public void onDrop(DropEvent event)
            {
                // TODO Auto-generated method stub
                if ( pitProbeRecord.getTotalRows() > 0 )
                    pitProbeRecord.setCanAcceptDrop(false);
            }
            
        });
        
        pitProbeRecord.addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event)
            {
                if ( pitProbeRecord.getTotalRows() == 0 )
                    pitProbeRecord.setCanAcceptDrop(true);
                
            }
            
        });

        if ( cooker != null)
           pitProbeRecord.setData( getDataFromCooker( cooker, "pit"));
        
        vp.addMember( tabNameForm );
        vp.addMember(pit);
        vp.addMember(pitProbeRecord);

        Label blower = new Label("Blower: ");
        blower.setHeight(25);
        blower.setWidth100();
        blowerProbe = new ConfigurationListGrid("blower");

        blowerProbe.setShowHeader(false);
        blowerProbe.setHeight(40);
        
        blowerProbe.addDropHandler(new DropHandler() {
            @Override
            public void onDrop(DropEvent event)
            {
                // TODO Auto-generated method stub
                if ( blowerProbe.getTotalRows() > 0 )
                    blowerProbe.setCanAcceptDrop(false);
            }
            
        });
        
        blowerProbe.addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event)
            {
                if ( blowerProbe.getTotalRows() == 0 )
                    blowerProbe.setCanAcceptDrop(true);
                
            }
            
        });
        
        if ( cooker != null )
           blowerProbe.setData(getDataFromCooker( cooker, "blower"));
        
        Label food = new Label("Food Probes: ");
        food.setHeight(25);
        food.setWidth100();
        tempProbes = new ConfigurationListGrid("temp");
       
        if ( cooker != null )
           tempProbes.setData( getDataFromCooker( cooker, "food"));

        tempProbes.setShowHeader(false);
        tempProbes.setHeight(115);
        
        
        vp.addMember(blower);
        vp.addMember(blowerProbe);
        
        vp.addMember(food);
        vp.addMember( tempProbes );
        
        this.addMember( vp );
    }
    
    public ArrayList<ProbeRecord> onPaneClose()
    {
        ArrayList<ProbeRecord> discardList = new ArrayList<ProbeRecord>();
        
        if ( pitProbeRecord.getTotalRows() > 0)
        {
           ProbeRecord lg = (ProbeRecord)pitProbeRecord.getRecord(0);
           discardList.add(lg);
        }
        
        if ( blowerProbe.getTotalRows() > 0 )
        {
            //ListGridRecord lg = pitProbeRecord.getRecord(0);
           // discardList.add(lg.getAttribute("probeID"));
            ProbeRecord lg = (ProbeRecord)blowerProbe.getRecord(0);
             discardList.add(lg);
        }
        
        if ( tempProbes.getTotalRows() > 0 )
        {
            for ( int i = 0; i < tempProbes.getTotalRows(); i++ )
            {
                ProbeRecord lg = (ProbeRecord)tempProbes.getRecord(i);
                discardList.add(lg);
            }
        }
        
        
        return discardList;
    }
    
    private RecordList getDataFromCooker(Cooker cooker, String type)
    {
        RecordList rl = new RecordList();
        
        if ( type.compareToIgnoreCase( "pit" ) == 0)
        {
            SDevice sd = cooker.getPitSensor();
            if ( sd != null )
            {
               ProbeRecord r = new ProbeRecord(sd.getID(), sd.getName(), "Temp" );
               rl.add( r );
            }
        }
        else if ( type.compareToIgnoreCase("food") == 0)
        {
            if ( cooker.getProbeList() != null )
            {
                for ( SDevice sd : cooker.getProbeList() )
                {
                    ProbeRecord r = new ProbeRecord(sd.getID(), sd.getName(), "Temp" );
                    rl.add( r );
                }
            }
        }
        else if ( type.compareToIgnoreCase("blower") == 0)
        {
            if ( cooker.getPitSensor() != null && cooker.getPitSensor().getFanDevice() != null )
            {
                SDevice sd = cooker.getPitSensor().getFanDevice();
                ProbeRecord r = new ProbeRecord(sd.getID(), sd.getName(), "Blower" );
                rl.add( r );
            }
        }
        
        return rl;
    }
    
    public Cooker getCooker(ArrayList<SDevice> stokerConf )
    {
        HashMap<String,SDevice> deviceMap = new HashMap<String,SDevice>();
    
        Cooker cooker = new Cooker(nameTextItem.getEnteredValue());
        String pidID = null;
        String blowerID = null;
        StokerFan stokerFan = null;
        StokerPitProbe stokerPitSensor = null;
    
        for ( SDevice sd : stokerConf )
        {
            deviceMap.put(sd.getID(), sd);
        }
        
        RecordList blowerList = blowerProbe.getDataAsRecordList();
        if ( blowerList.getLength() > 0)
        {
           ProbeRecord blowerRecord = (ProbeRecord) blowerList.get(0);
           blowerID = blowerRecord.getID();
           SDevice sd = deviceMap.get( blowerID );
           stokerFan = new StokerFan( sd.getID(), sd.getName());
        }
        
        RecordList rl = pitProbeRecord.getDataAsRecordList();
        if( rl.getLength() > 0 )
        {
           ProbeRecord pitRecord = (ProbeRecord) rl.get(0);
           SDevice sd = deviceMap.get(pitRecord.getID());
           StokerProbe sp = new StokerProbe(sd.getID(), sd.getName() );
           
           stokerPitSensor = new StokerPitProbe(sp, stokerFan );
           
        }

        if ( stokerPitSensor != null )
        {
           //cooker.addStokerProbe(stokerPitSensor);
           cooker.setPitSensor(stokerPitSensor);
        }
        
        RecordList probeList = tempProbes.getDataAsRecordList();
        for ( int i = 0; i < probeList.getLength(); i++ )
        {
            ProbeRecord tempRecord = (ProbeRecord) probeList.get(i);
            String probeID = tempRecord.getID();
            SDevice sd = deviceMap.get( probeID );
            if ( sd instanceof StokerPitProbe )
                ((StokerPitProbe)sd).setFanDevice(null);
            cooker.addStokerProbe((StokerProbe) sd );
            
        }
        return cooker;
        
    }
    
}
