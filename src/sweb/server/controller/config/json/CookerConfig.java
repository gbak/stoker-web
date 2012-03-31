package sweb.server.controller.config.json;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;


import sweb.shared.model.CookerList;

public class CookerConfig
{
    private static final Logger logger = Logger.getLogger(CookerConfig.class.getName());
    public CookerConfig()
    {
        
    }
    
    public static void saveConfig(CookerList cookerList)
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
    
    public static CookerList loadConfig()
    {
        CookerList cookerList = new CookerList();
        
        return cookerList;
    }
}
