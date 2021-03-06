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

package com.gbak.sweb.server.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.gbak.sweb.server.StokerWebProperties;
import com.gbak.sweb.server.events.WeatherChangeEvent;
import com.gbak.sweb.shared.model.weather.WeatherData;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;


public class WeatherController
{
    Timer updateTimer = null;
    WeatherData wd = null;
    EventBus eventBus = null;

    private static final Logger logger = Logger.getLogger(WeatherController.class.getName());
    
    @Inject
    public WeatherController(EventBus eventBus)
    {
        this.eventBus = eventBus;
    }
    
    public WeatherData getWeather()
    {
        if ( wd == null )
        {
            wd = fetchWeather();
        }
        return wd;
    }
    private static WeatherData fetchWeather() throws IllegalArgumentException
    {
       String strReply = null;
       WeatherData wd = null;


       try
       {
          String resultString;
          String strGetZipCode = StokerWebProperties.getInstance().getProperty("weather_zipcode");
          String strWeatherURL = StokerWebProperties.getInstance().getProperty("weather_url");

          //resultString = httpRequest("http://where.yahooapis.com/geocode?country=USA&flags=J&postal=30024");
          resultString = httpRequest(strWeatherURL + strGetZipCode);

  
    
           if ( resultString != null)
           {
               logger.debug("weather: " + resultString);
               wd = YahooWeatherJsonServerHelper.parseYahooWeatherData( resultString );   
              if ( wd == null )
              {
                  logger.warn("Error parsing weather string");
              }
           }
           
          
       }
       catch (IllegalStateException ise )
       {

       }
       catch ( net.sf.json.JSONException jse )
       {
          logger.warn("Error getting weather.");
          jse.printStackTrace();
       }


       return wd;
    }

    private static String httpRequest(String s)
    {
       String resultString = null;
       try
       {
          HttpClient httpclient = new DefaultHttpClient();
          HttpGet httpget = new HttpGet(s );
          HttpResponse response = httpclient.execute(httpget);
          HttpEntity entity = response.getEntity();

          if (entity != null) {
              InputStream instream = entity.getContent();
              Header contentEncoding = response.getFirstHeader("Content-Encoding");
              if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip"))
              {
                 instream = new GZIPInputStream(instream);
              }
              resultString= convertStreamToString(instream);
              instream.close();

              logger.debug("Response: " + resultString  );
          }
       }
       catch (IllegalStateException ise )
       {

       }
       catch (ClientProtocolException e)
       {
          logger.error("Client Protocol Exception in httpRequest: " + e.getStackTrace());
       }
       catch (IOException e)
       {
           logger.error("IO Exception in httpRequest: " + e.getStackTrace());
       }
       return resultString;

    }

    private static String convertStreamToString(InputStream is)
    {
       BufferedReader reader = new BufferedReader(new InputStreamReader(is));
       StringBuilder sb = new StringBuilder();

       String line = null;
       try
       {
          while ((line = reader.readLine()) != null)
          {
             sb.append(line + "\n");
          }
       }
       catch (IOException e)
       {
          e.printStackTrace();
       }
       finally
       {
          try
          {
             is.close();
          }
          catch (IOException e)
          {
             e.printStackTrace();
          }
       }
       return sb.toString();
    }

    private void updateWeatherAndFireEvent()
    {
        logger.info("Fetching Weather");
        wd = fetchWeather();
        if ( wd == null )
        {
            logger.error("Weather fetch failed");
            return;
        }
        logger.info("Weather fetch complete");
        WeatherChangeEvent wce = new WeatherChangeEvent(this,wd);
        eventBus.post(wce);
    }

    public void start()
    {
        logger.info("Starting weather controller");
        if ( updateTimer == null )
        {
            updateTimer = new Timer();
          //  updateWeatherAndFireEvent();

            RunTimer _runTimer = new RunTimer();
            updateTimer.schedule( _runTimer, 0 );  // 900000
        }
        else
        {
            logger.warn("Attempting to start Weather Controller and it's already running.");
        }

    }

    public void stop()
    {
        updateTimer.cancel();
        updateTimer = null;
    }

    private class RunTimer extends TimerTask
    {
       public void run()
       {
          Calendar c = Calendar.getInstance();

          updateWeatherAndFireEvent();

          c.add(Calendar.MINUTE,15);
          c.set(Calendar.SECOND, 0);
          updateTimer.schedule( new RunTimer(), c.getTime() );
       }
    }
}
