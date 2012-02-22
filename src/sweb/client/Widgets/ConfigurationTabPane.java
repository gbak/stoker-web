package sweb.client.widgets;

import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;
import com.smartgwt.client.widgets.events.DropMoveEvent;
import com.smartgwt.client.widgets.events.DropMoveHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;

public class ConfigurationTabPane extends VLayout
{

    ConfigurationTabPane()
    {
        Label pit = new Label("Pit Probe");
        final ConfigurationListGrid pitProbeRecord = new ConfigurationListGrid("temp");

        pitProbeRecord.setShowHeader(false);
        
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

        this.addMember(pit);
        this.addMember(pitProbeRecord);

        Label blower = new Label("Blower: ");
        final ConfigurationListGrid blowerProbe = new ConfigurationListGrid("blower");

        blowerProbe.setShowHeader(false);
        
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
        
        this.addMember(blower);
        this.addMember(blowerProbe);
        
        
        /*
         * TextItem usernameItem = new TextItem();
         * usernameItem.setTitle("Username"); usernameItem.setRequired(true);
         * usernameItem.setDefaultValue("bob");
         * 
         * TextItem emailItem = new TextItem(); emailItem.setTitle("Email");
         * emailItem.setRequired(true);
         * emailItem.setDefaultValue("bob@isomorphic.com");
         * 
         * PasswordItem passwordItem = new PasswordItem();
         * passwordItem.setTitle("Password"); passwordItem.setRequired(true);
         * 
         * PasswordItem password2Item = new PasswordItem();
         * password2Item.setTitle("Password again");
         * password2Item.setRequired(true); password2Item.setType("password");
         */
    }
}
