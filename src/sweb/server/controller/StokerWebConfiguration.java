package sweb.server.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;


import sweb.server.ClientMessagePusher;
import sweb.server.controller.events.CookerConfigChangeListener;
import sweb.server.controller.events.DataControllerEvent;
import sweb.server.controller.events.DataControllerEventListener;
import sweb.shared.model.CookerList;

public class StokerWebConfiguration
{
    private static final Logger logger = Logger.getLogger(StokerWebConfiguration.class.getName());
    
    private volatile static StokerWebConfiguration stokerWebConfiguration = null;
    
    private static CookerList cookerList;
    
    private static ArrayList<CookerConfigChangeListener> arListener = new ArrayList<CookerConfigChangeListener>();
    
    public static StokerWebConfiguration getInstance()
    {
        if ( stokerWebConfiguration == null)
        {
            synchronized ( Controller.class)
            {
                if ( stokerWebConfiguration == null )
                {
                    stokerWebConfiguration = new StokerWebConfiguration();
                }
            }
        }
        return stokerWebConfiguration;
    }
    
    public StokerWebConfiguration() { loadConfig(); }
    
    public void saveConfig(CookerList cookerList)
    {
        try 
        {
            ObjectMapper mapper = new ObjectMapper();
            BufferedWriter out = new BufferedWriter(new FileWriter("CookerConfig.json"));
            mapper.writeValue(out, cookerList);
            
            out.close();
        } 
        catch (IOException e) 
        {
            logger.error("Error writing CookerConig.json:\n" + e.getStackTrace() );
        }
    }
    
    public void loadConfig()
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            BufferedReader in = new BufferedReader(new FileReader("CookerConfig.json"));
            cookerList = mapper.readValue(in, CookerList.class);
            in.close();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (JsonParseException e)
        {
            logger.error("Error parsing CookerConfig.json in loadConfig");
            
        }
        catch (JsonMappingException e)
        {
            logger.error("Error mapping CookerConfig.json in loadConfig");
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public CookerList getCookerList()
    {
        return cookerList;
    }
    
    
    public void addChangeListener( CookerConfigChangeListener listener )
    {
        synchronized( this )
        {
           arListener.add( listener );
        }
    }

    public void removeChangeListener( CookerConfigChangeListener listener )
    {
        synchronized( this )
        {
            arListener.remove(listener);

        }
    }

    protected void fireActionPerformed()
    {
        synchronized( this )
        {
            for ( CookerConfigChangeListener listener : arListener )
            {
                listener.actionPerformed();
            }
        }
    }
}
