/**
 *  Stoker-web
 *
 *  Copyright (C) 2011  Gary Bak
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package sweb.server.controller.config.stoker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import org.codehaus.jackson.map.ObjectMapper;

import sweb.server.StokerWebConstants;
import sweb.server.StokerWebProperties;
import sweb.server.controller.HardwareDeviceConfiguration;
import sweb.server.controller.events.ConfigChangeEvent;
import sweb.server.monitors.stoker.config.json.Blower;
import sweb.server.monitors.stoker.config.json.Sensor;
import sweb.server.monitors.stoker.config.json.Stoker;
import sweb.server.monitors.stoker.config.json.StokerOuter;

import sweb.shared.model.devices.SDevice;
import sweb.shared.model.stoker.StokerFan;
import sweb.shared.model.stoker.StokerPitSensor;
import sweb.shared.model.stoker.StokerProbe;
import sweb.shared.model.stoker.StokerProbe.AlarmType;


import net.htmlparser.jericho.*;

// This name is confusing.
public class StokerHardwareDevice extends HardwareDeviceConfiguration
{
    private static final Logger logger = Logger.getLogger(StokerHardwareDevice.class.getName());
    sweb.server.monitors.stoker.StokerPitMonitor.Config config;

    public StokerHardwareDevice( sweb.server.monitors.stoker.StokerPitMonitor.Config c )
    {
       config = c;
    }
    
   private String convert( InputStream is)
   {
      StringBuilder sb = new StringBuilder();
      String line;
      BufferedReader reader;
      try
      {
         reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
         while ((line = reader.readLine()) != null)
         {
            sb.append(line).append("\n");
        }

      }
      catch (UnsupportedEncodingException e)
      {
         logger.error("Unsuppored Encoding exception caught: " + e.getStackTrace());
      }
      catch (IOException e)
      {
         logger.error("IOException running convert");
      }

        return ( sb.toString());
   }

   private StokerProbe.AlarmType getAlarmType( int i )
   {
       StokerProbe.AlarmType alt = StokerProbe.AlarmType.NONE;

       switch( i )
       {
           case 1:
               alt = StokerProbe.AlarmType.ALARM_FOOD;
               break;
           case 2:
               alt = StokerProbe.AlarmType.ALARM_FIRE;
               break;
       }

       return alt;
   }

   private synchronized void addConfig( String strInput, String strValue )
   {
       super.setUpdatedStaus(false);

       String prefix = strInput.substring(0,2).toLowerCase();
       String deviceID = strInput.substring( 2 ).toLowerCase();

     logger.debug("Prefix: " + prefix );
     logger.debug("Device ID: " + deviceID );

       if ( prefix.compareTo("n1") == 0)
       {
           super.addDevice(new StokerProbe( deviceID, strValue ));
           logger.debug("Found probe with ID: " + deviceID + " and value: " + strValue );
       }
       else if ( prefix.compareTo("n2") == 0)
       {
           super.addDevice(new StokerFan( deviceID, strValue ));
           logger.debug("Found fan with ID: " + deviceID + " and value: " + strValue);
       }
       else if ( prefix.compareTo("sw") == 0)
       {
           if ( strValue.length() > 0 )
           {
               // if there is no sw value, then this is a food probe

               StokerProbe sp = (StokerProbe) super.getDevice( deviceID );
               StokerFan dFan = (StokerFan) super.getDevice(strValue);

               StokerPitSensor dProbe = new StokerPitSensor(sp, dFan);

               super.replaceDevice( dProbe );
           }
       }
       else if ( prefix.compareTo("ta") == 0)
       {
           // target Temp
           StokerProbe sp = (StokerProbe) super.getDevice( deviceID );
           try { sp.setTargetTemp(new Integer( strValue ).intValue()); }
           catch ( NumberFormatException nfe )
           {
               logger.warn("Invalid number in ta: " + strValue );
               sp.setTargetTemp(0);
           }
       }
       else if ( prefix.compareTo("tl") == 0)
       {
           // Temp Alarm lower
           StokerProbe sp = (StokerProbe) super.getDevice( deviceID );
           try { sp.setLowerTempAlarm(new Integer( strValue ).intValue()); }
           catch ( NumberFormatException nfe )
           {
               logger.warn("Invalid number in ta: " + strValue );
               sp.setLowerTempAlarm(0);
           }
       }
       else if ( prefix.compareTo("th") == 0)
       {
           // Temp Alarm upper
           StokerProbe sp = (StokerProbe) super.getDevice( deviceID );
           try { sp.setUpperTempAlarm(new Integer( strValue ).intValue()); }
           catch ( NumberFormatException nfe )
           {
               logger.warn("Invalid number in ta: " + strValue );
               sp.setUpperTempAlarm(0);
           }
       }
       else if ( prefix.compareTo("al") == 0)
       {
          // Alarm option
           StokerProbe sp = (StokerProbe) super.getDevice( deviceID );
           try
           {
              sp.setAlarmEnabled(getAlarmType(new Integer( strValue ).intValue()));
           }
           catch ( NumberFormatException nfe)
           {
               logger.warn("Invalid number in al: " + strValue );
               sp.setAlarmEnabled( StokerProbe.AlarmType.NONE );
           }
       }
       super.setUpdatedStaus(true);
   }

    public synchronized boolean pullJSonConfig()
    {
        boolean bSuccess = false;
        int iTry = 0;

        super.clear();
        do
        {
            HttpClient httpclient = new DefaultHttpClient();
            try
            {
                String strStokerIP = StokerWebProperties.getInstance()
                        .getProperty(StokerWebConstants.PROPS_STOKER_IP_ADDRESS);
                String s = new String("http://" + strStokerIP + "/stoker.json");
                
                HttpGet httpget = new HttpGet(s);
                logger.debug("executing JSON request " + httpget.getURI());
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                
                String responseBody = httpclient.execute(httpget, responseHandler);
                logger.debug("after execute");
                logger.debug("----------------------------------------");
                logger.debug(responseBody);
                logger.debug("----------------------------------------");

                ObjectMapper mapper = new ObjectMapper();
                StokerOuter stokerOuter = mapper.readValue(responseBody, StokerOuter.class);
                
                Stoker stoke = stokerOuter.getStoker();
                ArrayList<Blower> stokerBlowers = stoke.getBlowers();
                for ( Blower b : stokerBlowers )
                {
                    logger.debug("Adding fan device: " + b.getId().toLowerCase() + " with name: " + b.getName());
                    super.addDevice( new StokerFan(b.getId().toLowerCase(), b.getName()));
                }
                
                for ( Sensor sensor : stoke.getSensors())
                {
                    if ( sensor.getBlower() == null )
                    {
                        logger.debug("Adding sensor device: " + sensor.getId().toLowerCase()+ " with name: " + sensor.getName());
                        StokerProbe sp = new StokerProbe( sensor.getId().toLowerCase(), sensor.getName());
                        sp.setCurrentTemp((float)sensor.getTc());
                        sp.setLowerTempAlarm(sensor.getTl());
                        sp.setTargetTemp(sensor.getTa());
                        sp.setUpperTempAlarm(sensor.getTh());
                        try
                        {
                           sp.setAlarmEnabled(getAlarmType(new Integer( sensor.getAl() ).intValue()));
                        }
                        catch ( NumberFormatException nfe)
                        {
                            logger.warn("Invalid number in al: " + sensor.getAl() );
                            sp.setAlarmEnabled( StokerProbe.AlarmType.NONE );
                        }
                        super.addDevice( sp );
                        
                    }
                    else
                    {
                        logger.debug("Adding pit sensor device: " + sensor.getId().toLowerCase()+ " with name: " + sensor.getName());
                        StokerPitSensor sp = new StokerPitSensor( sensor.getId().toLowerCase(), sensor.getName());
                        sp.setCurrentTemp((float)sensor.getTc());
                        sp.setLowerTempAlarm(sensor.getTl());
                        sp.setTargetTemp(sensor.getTa());
                        sp.setUpperTempAlarm(sensor.getTh());
                        try
                        {
                           sp.setAlarmEnabled(getAlarmType(new Integer( sensor.getAl() ).intValue()));
                        }
                        catch ( NumberFormatException nfe)
                        {
                            logger.warn("Invalid number in al: " + sensor.getAl() );
                            sp.setAlarmEnabled( StokerProbe.AlarmType.NONE );
                        }
                        sp.setFanDevice((StokerFan)super.getDevice(sensor.getBlower().toLowerCase()));
                        super.addDevice( sp );
                    }
                }
                bSuccess = true;
                
                
            }
            catch (java.net.ConnectException ce)
            {
                logger.error("Caught connection exception while scraping webpage");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (bSuccess == false)
            {
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                    logger.error(e.getStackTrace());
                }
            }

        } while (bSuccess == false && iTry++ < 5);

    //    assignCookerNames();

        if ( bSuccess == false )
        {
            return false;
        }
        super.setUpdatedStaus(true);
        logger.debug("Config: " + super.debugString());
        config.configChange(new ConfigChangeEvent(this,
                ConfigChangeEvent.EventType.CONFIG_UPDATE));
        
        return true;

    }
   public synchronized void scrapeWebPage()
   {
       boolean bSuccess = false;
       int iTry = 0;

       super.clear();
       do
       {
          try
          {
             String strStokerIP = StokerWebProperties.getInstance().getProperty(StokerWebConstants.PROPS_STOKER_IP_ADDRESS);
             Source s = new Source( new URL("http://" + strStokerIP + "/index.html"));
             Queue<String> qDefaults = new LinkedList<String>();

             // Find all the javascript and look for the 'sel' variable.
             // this contains the default values for the select boxes.

             List<Element> lscript = s.getAllElements(HTMLElementName.SCRIPT);
             Iterator<Element> scriptIter = lscript.iterator();
             while ( scriptIter.hasNext())
             {
                Element el = scriptIter.next();
                Attributes at = el.getAttributes();
                if ( at != null)
                {
                    String strScriptContent = el.getContent().toString();
                    int selIndex = strScriptContent.indexOf("var sel = ");
                    if ( selIndex >= 0)
                    {
                        int iBegin = strScriptContent.indexOf('[') + 1;
                        int iEnd = strScriptContent.indexOf(']');
                        String strDefaults = strScriptContent.substring(iBegin, iEnd);
                        logger.debug("Defaults: " + strDefaults );
                        StringTokenizer st = new StringTokenizer( strDefaults, ",");
                        while ( st.hasMoreTokens())
                        {
                            qDefaults.add(st.nextToken().replace("\"","").toLowerCase());
                        }
                    }
                }
             }

             FormFields ff = s.getFormFields();

             Iterator<FormField> it = ff.iterator();
             while ( it.hasNext() )
             {
                FormField f = it.next();
                logger.debug("Name: " + f.getName());

                // qq is the label for the ID checkbox.  We can ignore it.
                if ( f.getName().compareTo("qq") == 0)
                    continue;

                List<String> al = f.getValues();
                Iterator<String> listIter = al.iterator();
                if ( listIter.hasNext())
                    addConfig( f.getName(), listIter.next().toLowerCase());
                else
                {
                    // if there is no value, pull the default from the javascript variable sel =.
                    addConfig( f.getName(), qDefaults.remove() );
                }
             }
             bSuccess = true;

          }
          catch( java.net.ConnectException ce )
          {
              logger.error("Caught connection exception while scraping webpage");
          }
          catch( Exception e)
          {
             e.printStackTrace();
          }

          if ( bSuccess == false )
          {
              try
            {
                Thread.sleep(2000);
            }
            catch (InterruptedException e)
            {
                logger.error(e.getStackTrace());
            }
          }

       } while ( bSuccess == false && iTry++ < 5  );

   //   assignCookerNames();

       super.setUpdatedStaus(true);
      logger.debug("Config: " + super.debugString());
      config.configChange(new ConfigChangeEvent( this, ConfigChangeEvent.EventType.CONFIG_UPDATE ));

   }
/*
   private void assignCookerNames()
   {
       HashMap<Integer,String> hm = new HashMap<Integer,String>();

      for ( String s : sc.getAllBlowerIDs())
      {
         SDevice sd = sc.getDevice(s);
         String strName = StokerWebProperties.getInstance().getProperty(s);
         if ( strName == null )
         {
             logger.warn("Cooker missing name in properties file: " + sd.getID());
             strName = sd.getID();
         }
         hm.put( new Integer(sd.getCookerNum()), strName);
      }

      for ( SDevice sd : sc.getAllDevices())
      {
          sd.setCookerName( hm.get(sd.getCookerNum()));
      }

   }
*/

   private static String alPostData( SDevice sd ) throws UnsupportedEncodingException
   {
      if ( sd instanceof StokerProbe )
      {
            return "al" + URLEncoder.encode(sd.getID().toUpperCase(), "UTF-8") + "=" +
                            URLEncoder.encode( new Integer(((StokerProbe) sd).getAlarmEnabled().ordinal()).toString(), "UTF-8");

      }
      return "";
   }

   private static String n1PostData( SDevice sd ) throws UnsupportedEncodingException
   {
      if ( sd instanceof StokerProbe )
      {
            return "n1" + URLEncoder.encode(sd.getID().toUpperCase(), "UTF-8") + "=" +
                          URLEncoder.encode( sd.getName(), "UTF-8");

      }
      return "";
   }

   private static String n2PostData( SDevice sd ) throws UnsupportedEncodingException
   {
      if ( sd instanceof StokerFan )
            return "n2" + URLEncoder.encode(sd.getID().toUpperCase(), "UTF-8") + "=" +
                          URLEncoder.encode( sd.getName(), "UTF-8");
      return "";
   }

   private static String swPostData( SDevice sd ) throws UnsupportedEncodingException
   {
      if ( sd instanceof StokerProbe )
      {
          StokerFan sf = ((StokerProbe) sd).getFanDevice();
          String fanID = null;
          if ( sf == null )
              fanID = "None";
          else
              fanID = sf.getID().toUpperCase();

          return "sw" + URLEncoder.encode(sd.getID().toUpperCase(), "UTF-8") + "=" +
                  URLEncoder.encode( fanID , "UTF-8");

      }
       return "";
   }

   private static String taPostData( SDevice sd ) throws UnsupportedEncodingException
   {
      if ( sd instanceof StokerProbe )
            return "ta" + URLEncoder.encode(sd.getID().toUpperCase(), "UTF-8") + "=" +
                          URLEncoder.encode( new Integer(((StokerProbe) sd).getTargetTemp()).toString(), "UTF-8");
      return "";
   }

   private static String thPostData( SDevice sd ) throws UnsupportedEncodingException
   {
      if ( sd instanceof StokerProbe )
      {
          String alarmHigh = "n/a";

          if ( ((StokerProbe) sd).getAlarmEnabled() == AlarmType.ALARM_FIRE )
          {
             alarmHigh = new Integer(((StokerProbe) sd).getUpperTempAlarm()).toString();
          }

          return "th" + URLEncoder.encode(sd.getID().toUpperCase(), "UTF-8") + "=" +
                  URLEncoder.encode( alarmHigh , "UTF-8");

      }
       return "";
   }

   private static String tlPostData( SDevice sd ) throws UnsupportedEncodingException
   {
      if ( sd instanceof StokerProbe )
      {
          String alarmLow = "n/a";

          if ( ((StokerProbe) sd).getAlarmEnabled() == AlarmType.ALARM_FIRE )
          {
             alarmLow = new Integer(((StokerProbe) sd).getLowerTempAlarm()).toString();
          }

          return "tl" + URLEncoder.encode(sd.getID().toUpperCase(), "UTF-8") + "=" +
                  URLEncoder.encode( alarmLow , "UTF-8");
      }
       return "";
   }


   private static String getPostData( SDevice sd )
   {
      StringBuilder sb = new StringBuilder();
      ArrayList<String> alPostData = new ArrayList<String>();
      try
      {
          String s = null;
          s = alPostData(sd);
          if ( s.length() > 0 )
              alPostData.add( s );

          s = n1PostData(sd);
          if ( s.length() > 0 )
              alPostData.add( s );

          s = n2PostData(sd);
          if ( s.length() > 0 )
              alPostData.add( s );

          s = swPostData(sd);
          if ( s.length() > 0 )
              alPostData.add( s );

          s = taPostData(sd);
          if ( s.length() > 0 )
              alPostData.add( s );

          s = thPostData(sd);
          if ( s.length() > 0 )
              alPostData.add( s );

          s = tlPostData(sd);
          if ( s.length() > 0 )
              alPostData.add( s );

      }
      catch (UnsupportedEncodingException uee)
      {
          logger.warn( uee.getStackTrace() );
       //  System.out.println("Unsupported Character while encoding URL");
      }

      int size = alPostData.size();
      for ( int i = 0; i < size; i++ )
      {
          sb.append(  alPostData.get( i ));
          if ( i < size - 1 )
          {
              sb.append( "&");
          }
      }
      return sb.toString();
   }

   
   /** Update Stoker with updated configuration settings
    * @param stokerDeviceList List of SDevice's that are to be updated
    */
   public static void postUpdate( ArrayList<SDevice> stokerDeviceList)
   {
       try
       {
           StringBuilder postData = new StringBuilder();
           int size = stokerDeviceList.size();
           for ( int i = 0; i < stokerDeviceList.size(); i++ )
           {
               SDevice sd = stokerDeviceList.get( i );
               postData.append( getPostData( sd ));
               if ( i < size - 1)
                   postData.append("&");
           }

           logger.debug("Posting: " + postData.toString());

           // Send data
           String strStokerIP = StokerWebProperties.getInstance().getProperty(StokerWebConstants.PROPS_STOKER_IP_ADDRESS);
           URL url = new URL("http://" + strStokerIP + "/stoker.Post_Handler");
           URLConnection conn = url.openConnection();
           conn.setDoOutput(true);
           OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
           writer.write(postData.toString());
           writer.flush();

           // Get the response
           BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
           String line;
           while ((line = rd.readLine()) != null)
           {
               logger.debug("Response: " + line );
           }
           writer.close();
           rd.close();
       } catch (Exception e) {
       }
   }

   @Override
    public boolean loadNow()
    {
       super.setUpdatedStaus(false);
       //scrapeWebPage();
        return pullJSonConfig();

    }

}
