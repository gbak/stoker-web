package sweb.server.rest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import sweb.server.config.StokerWebConfiguration;

import com.google.inject.Inject;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Path("/test")
public class JerseyTest {
    
    StokerWebConfiguration m_stokerwebConfiguration;
    
    @Inject
    JerseyTest( StokerWebConfiguration swc)
    {
        m_stokerwebConfiguration = swc;
    }
    
    @POST
    public String handleFooPost(@QueryParam("bar") String bar, @QueryParam("quux") int quux) {
       return "{\"yay\":\"hooray\"}";
    }
    
    @GET
    @Path("Stoker.json")
    public String handleLegacyGet()
    {
        return("{\"stoker\":{\"sensors\":[{\"id\":\"E70000116F279030\",\"name\":\"pit sensor\",\"al\":0,\"ta\":60,\"th\":76,\"tl\":20,\"tc\":79.6,\"blower\":\"230000002A55C305\"}," +
" {\"id\":\"4C0000116EF72C30\",\"name\":\"Plug 4\",\"al\":0,\"ta\":32,\"th\":32,\"tl\":32,\"tc\":163.5,\"blower\":null}, "+
" {\"id\":\"DB0000116F0BEC30\",\"name\":\"food sensor3\",\"al\":0,\"ta\":190,\"th\":250,\"tl\":75,\"tc\":81.7,\"blower\":null}, "+
" {\"id\":\"0E0000116F0B2130\",\"name\":\"Plug 5\",\"al\":0,\"ta\":50,\"th\":32,\"tl\":32,\"tc\":155.8,\"blower\":null}], "+
"  \"blowers\":[{\"id\":\"230000002A55C305\",\"name\":\"Blower 1\",\"on\":0}]}}");
    }
    
    @POST
    @Path("data/all")
    public String handleDataGet()
    {
        return("[{\"id\":\"123\",\"alarmType\":\"NONE\",\"currentTemp\":\"200\",\"targetTemp\":\"200\",\"name\":\"Temp Probe 1\",\"alarmLow\":\"\",\"alarmHigh\":\"\"},{\"id\":\"123\",\"alarmType\":\"NONE\",\"currentTemp\":\"2\",\"targetTemp\":\"200\",\"name\":\"Temp Probe 1\",\"alarmLow\":\"\",\"alarmHigh\":\"\"},{\"id\":\"123\",\"alarmType\":\"NONE\",\"currentTemp\":\"225\",\"targetTemp\":\"200\",\"name\":\"Temp Probe 1\",\"alarmLow\":\"\",\"alarmHigh\":\"\"}]");
    }
    
    
    
    @GET
    public String handleFooGet()
    {
        ObjectMapper mapper = new ObjectMapper();

        String cookerListJson = null;
        try
        {
            cookerListJson = mapper.writeValueAsString( m_stokerwebConfiguration.getCookerList());
        }
        catch (JsonGenerationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (JsonMappingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return cookerListJson;
        
    }
}