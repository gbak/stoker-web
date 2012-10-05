package sweb.server.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import sweb.common.json.Blower;
import sweb.common.json.Device;
import sweb.common.json.DeviceDataList;
import sweb.common.json.PitProbe;
import sweb.common.json.Probe;
import sweb.server.config.StokerWebConfiguration;
import sweb.server.monitors.PitMonitor;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.devices.stoker.StokerFan;
import sweb.shared.model.devices.stoker.StokerPitProbe;
import sweb.shared.model.devices.stoker.StokerProbe;
import sweb.utils.ConvertUtils;

import com.google.inject.Inject;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Path("/test")
public class RestServices {
    
    StokerWebConfiguration m_stokerwebConfiguration;
    PitMonitor m_pitMonitor;
    
    @Inject
    RestServices( StokerWebConfiguration swc, PitMonitor pitMon)
    {
        m_stokerwebConfiguration = swc;
        this.m_pitMonitor = pitMon;
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
    @Path("data")
    public String handleDataGet( @DefaultValue("all") @QueryParam("probes") String probe )
    {
        DeviceDataList deviceDataList = new DeviceDataList();
        
        Set<String> set = null;
        if ( probe.compareToIgnoreCase("all") != 0 )
        {
            set = new HashSet<String>(Arrays.asList(probe.split(",")));
            
        }
 
        for ( SDataPoint sdp : m_pitMonitor.getCurrentTemps())
        {
            SDevice sd = m_stokerwebConfiguration.getDeviceByID(sdp.getDeviceID());
            if ( set == null || (set != null && set.contains(sdp.getDeviceID())) )
            {
            
                Device d = null;
                if ( sd instanceof StokerProbe )
                {
                    Probe p = ConvertUtils.toProbe( (StokerProbe) sd );
                    deviceDataList.add( p );
                }
                else if ( sd instanceof StokerPitProbe )
                {
                    PitProbe p = ConvertUtils.toPitProbe( (StokerPitProbe) sd );
                    deviceDataList.add( p );
                }
                else if ( sd instanceof StokerFan )
                {
                    Blower b = ConvertUtils.toBlower( (StokerFan) sd );
                    deviceDataList.add( b );
                }
            }
        }
        
            
       /* return("[{\"id\":\"123\",\"type\":\"probe\",\"alarmType\":\"NONE\",\"currentTemp\":\"200\",\"targetTemp\":\"200\",\"name\":\"Temp Probe 1\",\"alarmLow\":\"\",\"alarmHigh\":\"\"}," +
                "{\"id\":\"123\",\"type\":\"probe\",\"alarmType\":\"NONE\",\"currentTemp\":\"2\",\"targetTemp\":\"200\",\"name\":\"Temp Probe 1\",\"alarmLow\":\"\",\"alarmHigh\":\"\"}," +
                "{\"id\":\"123\",\"type\":\"probe\",\"alarmType\":\"NONE\",\"currentTemp\":\"225\",\"targetTemp\":\"200\",\"name\":\"Temp Probe 1\",\"alarmLow\":\"\",\"alarmHigh\":\"\"}]");
   */ 
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "";
        try
        {
            jsonString = mapper.writeValueAsString(deviceDataList);
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
        return jsonString;
    
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