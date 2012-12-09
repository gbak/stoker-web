package com.gbak.sweb.server.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;


import com.gbak.sweb.common.json.Device;
import com.gbak.sweb.common.json.DeviceDataList;
import com.gbak.sweb.common.json.ItemCount;
import com.gbak.sweb.common.json.LogItem;
import com.gbak.sweb.common.json.LogItemCount;
import com.gbak.sweb.common.json.LogItemCountList;
import com.gbak.sweb.common.json.LogItemList;
import com.gbak.sweb.common.json.LogNote;
import com.gbak.sweb.common.json.PitProbe;
import com.gbak.sweb.common.json.Probe;
import com.gbak.sweb.common.json.ServerRequest;
import com.gbak.sweb.common.json.ServerResponse;
import com.gbak.sweb.server.StokerCoreServiceImpl;
import com.gbak.sweb.server.StokerSharedServices;
import com.gbak.sweb.server.config.StokerWebConfiguration;
import com.gbak.sweb.server.monitors.PitMonitor;
import com.gbak.sweb.server.security.LoginProperties;
import com.gbak.sweb.shared.model.CookerList;
import com.gbak.sweb.shared.model.data.SDataPoint;
import com.gbak.sweb.shared.model.devices.SDevice;
import com.gbak.sweb.shared.model.devices.stoker.StokerPitProbe;
import com.gbak.sweb.shared.model.devices.stoker.StokerProbe;
import com.gbak.sweb.utils.ConvertUtils;
import com.google.inject.Inject;
import com.sun.jersey.spi.resource.Singleton;

//@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Path("/v1")
public class RestServices {
    
    StokerWebConfiguration m_stokerwebConfiguration;
    PitMonitor m_pitMonitor;
    StokerSharedServices m_stokerSharedServices;
    
    private static final Logger logger = Logger.getLogger(RestServices.class.getName());
    
    @Inject
    RestServices( StokerWebConfiguration swc, StokerSharedServices ssc, PitMonitor pitMon)
    {
        m_stokerwebConfiguration = swc;
        this.m_pitMonitor = pitMon;
        this.m_stokerSharedServices = ssc;
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
       com.gbak.sweb.common.json.CookerList cookerList = new com.gbak.sweb.common.json.CookerList();
       
       for ( com.gbak.sweb.shared.model.Cooker cooker : m_stokerwebConfiguration.getCookerList().getCookerList() )
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
    public Response handleDeviceGet( )
    {
        return handleDataGet(null);
    }
    
 
    @GET
    @Path("devices/{id}")
    public Response handleDataGet( @PathParam("id") String probe )
  
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
        
        CookerList cookerList =  m_stokerwebConfiguration.getCookerList();
        if ( cookerList != null)
        for ( com.gbak.sweb.shared.model.Cooker cooker : cookerList.getCookerList() )
        {
            String cookerName = cooker.getCookerName();
            Device d = new Device();
            d.cooker = cookerName;
            
//            if ( probe == null )
//            {
//               deviceDataList.devices.add( d );  // This is for the android title bar
//            }
//            
            StokerPitProbe spp = cooker.getPitSensor();
            if ( spp != null )
            {
                
                PitProbe pp = ConvertUtils.toPitProbe( spp , dataPointHash.get( spp.getID()));
                pp.cooker = cookerName;
                if ( probe == null || set.contains(pp.id))
                {
                   deviceDataList.devices.add( pp );
                }
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
        
        deviceDataList.logCount = ConvertUtils.toLogItemCountList(m_stokerSharedServices.getLogList());
            
       /* return("[{\"id\":\"123\",\"type\":\"probe\",\"alarmType\":\"NONE\",\"currentTemp\":\"200\",\"targetTemp\":\"200\",\"name\":\"Temp Probe 1\",\"alarmLow\":\"\",\"alarmHigh\":\"\"}," +
                "{\"id\":\"123\",\"type\":\"probe\",\"alarmType\":\"NONE\",\"currentTemp\":\"2\",\"targetTemp\":\"200\",\"name\":\"Temp Probe 1\",\"alarmLow\":\"\",\"alarmHigh\":\"\"}," +
                "{\"id\":\"123\",\"type\":\"probe\",\"alarmType\":\"NONE\",\"currentTemp\":\"225\",\"targetTemp\":\"200\",\"name\":\"Temp Probe 1\",\"alarmLow\":\"\",\"alarmHigh\":\"\"}]");
   */ 
       /* ObjectMapper mapper = new ObjectMapper();
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
        return jsonString;*/
        return Response.status(200).entity(deviceDataList).build();
    }
    
    @POST
    @Path("devices")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleDevicePost(ServerRequest<DeviceDataList> update)
    {
        logger.info("handleDevicePost()");
        ServerResponse<String> response = new ServerResponse<String>();
        if ( LoginProperties.getInstance().validateLoginID(update.login.username, update.login.password) )
        {
            logger.info("valid credentials detected for user: " + update.login.username);
            
            //m_stokerwebConfiguration.updateConfig(ConvertUtils.toSDeviceList( update.data));
            m_stokerSharedServices.updateTempAndAlarmSettings(ConvertUtils.toSDeviceList( update.data));
            
            // update code here;
            response.messages.add("values saved");
            response.success = true;

        }
        else
        {
            logger.info("Invalid credentials detected for user: " + update.login.username);
            response.messages.add("Invalid login ID or password");
            response.success = false;
            return Response.status(401).entity(response).build();
        }
        
        return Response.status(200).entity(response).build();
    }
    
    @GET
    @Path("logs/cooker")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLogList()
    {
       // LogItemList lil = new LogItemList();
       // lil.logList = ConvertUtils.toLogItemList(m_stokerSharedServices.getLogList());
        
       // return Response.status(200).entity(lil).build();
        return getLogListCooker( "" );
    }
    
    @PUT
    @Path("logs/note")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addNoteToLog( ServerRequest<LogNote> update )
    {
        LogNote ln = update.data;
        ServerResponse<String> response = new ServerResponse<String>();
        if ( LoginProperties.getInstance().validateLoginID(update.login.username, update.login.password) )
        {
            logger.info("valid credentials detected for user: " + update.login.username);
            
            m_stokerSharedServices.addNoteToLog(ln.note, ln.logList );
            
            // update code here;
            response.messages.add("log created");
            response.success = true;

        }
        else
        {
            logger.info("Invalid credentials detected for user: " + update.login.username);
            response.messages.add("Invalid login ID or password");
            response.success = false;
            return Response.status(401).entity(response).build();
        }
        
        return Response.status(201).entity(response).build();
    }
    
    @GET
    @Path("logs/cooker/{cooker}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLogListCooker(@PathParam("cooker") String cookerName)
    {
        LogItemList lil = new LogItemList();
        lil.logList = ConvertUtils.toLogItemList(m_stokerSharedServices.getLogList(), cookerName );
        return Response.status(200).entity(lil).build();
    }
    
    @GET
    @Path("logs/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLogListCount()
    {
        LogItemCount ic = new LogItemCount();
        ic = ConvertUtils.toLogItemCountList(m_stokerSharedServices.getLogList());
        
        return Response.status(200).entity(ic).build();
    }
    
    
    @PUT
    @Path("logs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLog(ServerRequest<LogItem> update)
    {
        
        LogItem log = update.data;
        ServerResponse<String> response = new ServerResponse<String>();
        if ( LoginProperties.getInstance().validateLoginID(update.login.username, update.login.password) )
        {
            logger.info("valid credentials detected for user: " + update.login.username);
            
            ArrayList<SDevice> deviceList = ConvertUtils.toSDeviceList(log.deviceList);
            
            m_stokerSharedServices.startLog(log.cookerName, log.logName, deviceList);
            
            // update code here;
            response.messages.add("values saved");
            response.success = true;

        }
        else
        {
            logger.info("Invalid credentials detected for user: " + update.login.username);
            response.messages.add("Invalid login ID or password");
            response.success = false;
            return Response.status(401).entity(response).build();
        }
        
        
        return Response.status(201).entity(response).build();
    }
    
    @POST
    @Path("logs")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopLog(ServerRequest<LogItem> update)
    {
        LogItem log = update.data;
        ServerResponse<String> response = new ServerResponse<String>();
        if ( LoginProperties.getInstance().validateLoginID(update.login.username, update.login.password) )
        {
            logger.info("valid credentials detected for user: " + update.login.username);
            
            m_stokerSharedServices.stopLog(log.cookerName, log.logName );
            
            // update code here;
            response.messages.add("log stopped");
            response.success = true;

        }
        else
        {
            logger.info("Invalid credentials detected for user: " + update.login.username);
            response.messages.add("Invalid login ID or password");
            response.success = false;
            return Response.status(401).entity(response).build();
        }
        
        
        return Response.status(200).entity(response).build();
    }
    
    
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String handleDefaultGet()
    {

        return "<html><body>Welcome to Stoker-web</body></html>";
        
    }
}