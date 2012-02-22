package sweb.client.widgets;

import java.util.ArrayList;

import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;
import com.smartgwt.client.widgets.events.DropMoveEvent;
import com.smartgwt.client.widgets.events.DropMoveHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;

public class ConfigurationTabPane extends VLayout
{

    VLayout vp = new VLayout();
    private final ConfigurationListGrid pitProbeRecord;
    private final ConfigurationListGrid blowerProbe;
    private final ConfigurationListGrid tempProbes;
    
    ConfigurationTabPane()
    {
        vp.setAlign(VerticalAlignment.TOP);
        vp.setHeight100();
        vp.setWidth100();
        vp.setLayoutMargin(5);
        vp.setMembersMargin(5);
        
        Label pit = new Label("Pit Probe");
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

        vp.addMember(pit);
        vp.addMember(pitProbeRecord);

        Label blower = new Label("Blower: ");
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
        tempProbes = new ConfigurationListGrid("temp");

        tempProbes.setShowHeader(false);
        
        
        vp.addMember(blower);
        vp.addMember(blowerProbe);
        
        vp.addMember(food);
        vp.addMember( tempProbes );
        
        this.addMember( vp );
    }
    
    public ArrayList<String> onPaneClose()
    {
        ArrayList<String> discardList = new ArrayList<String>();
        
        if ( pitProbeRecord.getTotalRows() > 0)
        {
           ListGridRecord lg = pitProbeRecord.getRecord(0);
           discardList.add(lg.getAttribute("probeID"));
        }
        
        if ( blowerProbe.getTotalRows() > 0 )
        {
            ListGridRecord lg = pitProbeRecord.getRecord(0);
            discardList.add(lg.getAttribute("probeID"));
        }
        
        
        return discardList;
    }
    
    
}
