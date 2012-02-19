package sweb.client.widgets;

import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;
import com.smartgwt.client.widgets.events.DropMoveEvent;
import com.smartgwt.client.widgets.events.DropMoveHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.layout.VLayout;

public class ConfigurationTabPane extends VLayout
{

    ConfigurationTabPane()
    {
        Label pit = new Label("Pit Probe");
        final ConfigurationListGrid pitProbeRecord = new ConfigurationListGrid();

        pitProbeRecord.setShowHeader(false);
        
        pitProbeRecord.setDropTypes();
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
        
        pitProbeRecord.addDropMoveHandler(new DropMoveHandler() {

            @Override
            public void onDropMove(DropMoveEvent event)
            {
                // TODO Auto-generated method stub
                
            }
            
        });
        
        this.addMember(pit);
        this.addMember(pitProbeRecord);

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
