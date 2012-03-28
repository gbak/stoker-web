package sweb.client.widgets;

import java.util.ArrayList;
import java.util.HashMap;

import sweb.shared.model.Cooker;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.stoker.StokerFan;
import sweb.shared.model.stoker.StokerPitSensor;
import sweb.shared.model.stoker.StokerProbe;

import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;
import com.smartgwt.client.widgets.events.DropMoveEvent;
import com.smartgwt.client.widgets.events.DropMoveHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;

public class ConfigurationTabPane extends VLayout
{

    VLayout vp = new VLayout();
    private final ConfigurationListGrid pitProbeRecord;
    private final ConfigurationListGrid blowerProbe;
    private final ConfigurationListGrid tempProbes;
    private TextItem nameTextItem;
    
    ConfigurationTabPane(ChangedHandler tabChangedHandler )
    {
        vp.setAlign(VerticalAlignment.TOP);
        vp.setHeight100();
        vp.setWidth100();
        vp.setLayoutMargin(5);
        vp.setMembersMargin(5);
        
        DynamicForm tabNameForm = new DynamicForm();  
        nameTextItem = new TextItem();  
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
        
        Label food = new Label("Food Probes: ");
        food.setHeight(25);
        food.setWidth100();
        tempProbes = new ConfigurationListGrid("temp");

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
    
    public Cooker getCooker(ArrayList<SDevice> stokerConf )
    {
        HashMap<String,SDevice> deviceMap = new HashMap<String,SDevice>();
    
        Cooker cooker = new Cooker(nameTextItem.getTitle());
        String pidID = null;
        String blowerID = null;
        StokerFan stokerFan = null;
        StokerPitSensor stokerPitSensor = null;
    
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
           
           stokerPitSensor = new StokerPitSensor(sp, stokerFan );
           
        }

        if ( stokerPitSensor != null )
           cooker.addStokerProbe(stokerPitSensor);
        
        RecordList probeList = tempProbes.getDataAsRecordList();
        for ( int i = 0; i < probeList.getLength(); i++ )
        {
            ProbeRecord tempRecord = (ProbeRecord) probeList.get(i);
            String probeID = tempRecord.getID();
            SDevice sd = deviceMap.get( probeID );
            cooker.addStokerProbe((StokerProbe) sd );
            
        }
        return cooker;
        
    }
    
}
