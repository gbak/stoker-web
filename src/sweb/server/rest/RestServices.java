package sweb.server.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
@Path("/v1")
public class RestServices {
    
    StokerWebConfiguration m_stokerwebConfiguration;
    PitMonitor m_pitMonitor;
    
    @Inject
    RestServices( StokerWebConfiguration swc, PitMonitor pitMon)
    {
        m_stokerwebConfiguration = swc;
        this.m_pitMonitor = pitMon;
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
    
    @GET
    @Path("cookers")
    public String handleConfigGet()
    {
       sweb.common.json.CookerList cookerList = new sweb.common.json.CookerList();
       
       for ( sweb.shared.model.Cooker cooker : m_stokerwebConfiguration.getCookerList().getCookerList() )
       {
          cookerList.add( ConvertUtils.toCooker( cooker )); 
       }
       
       ObjectMapper mapper = new ObjectMapper();
       String jsonString = "";
       try
       {
           jsonString = mapper.writeValueAsString(cookerList);
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
    @Path("devices")
    public String handleDeviceGet( )
    {
        return handleDataGet(null);
    }
    
    @GET
    @Path("devices/{id}")
    public String handleDataGet( @PathParam("id") String probe )
  
    {
        DeviceDataList deviceDataList = new DeviceDataList();
        
        Date receivedDate = null;
        
        Set<String> set = null;
        if ( probe != null )
        {
            set = new HashSet<String>(Arrays.asList(probe.split(","))); 
        }
 
        HashMap<String,SDataPoint> dataPointHash = new HashMap<String,SDataPoint>();
        
        // Load the data points into a hash also figure out the latest collected date.
        for ( SDataPoint sdp : m_pitMonitor.getCurrentTemps())
        {
            dataPointHash.put( sdp.getDeviceID(), sdp);
            if ( receivedDate == null || receivedDate.getTime() < sdp.getCollectedDate().getTime())
               receivedDate = sdp.getCollectedDate();
        }
        
        for ( sweb.shared.model.Cooker cooker : m_stokerwebConfiguration.getCookerList().getCookerList() )
        {
            String cookerName = cooker.getCookerName();
            Device d = new Device();
            d.cooker = cookerName;
            
            if ( probe == null )
            {
               deviceDataList.devices.add( d );  // This is for the android title bar
            }
            
            StokerPitProbe spp = cooker.getPitSensor();
            PitProbe pp = ConvertUtils.toPitProbe( spp , dataPointHash.get( spp.getID()));
            pp.cooker = cookerName;
            if ( probe == null || set.contains(pp.id))
            {
               deviceDataList.devices.add( pp );
            }
            
            for ( StokerProbe stokerProbe : cooker.getProbeList())
            {
                Probe p = ConvertUtils.toProbe( stokerProbe, dataPointHash.get( stokerProbe.getID()) );
                p.cooker = cookerName;
                if ( probe == null || set.contains(p.id))
                {
                   deviceDataList.devices.add( p );
                }
            }
        }
        
        if ( receivedDate != null )
            deviceDataList.receivedDate = receivedDate;
        
            
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
    public String handleDefaultGet()
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