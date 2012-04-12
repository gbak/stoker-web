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

package sweb.server.controller.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

import sweb.server.StokerWebProperties;
import sweb.server.controller.events.WeatherChangeEvent;
import sweb.server.controller.events.WeatherChangeEventListener;
import sweb.shared.model.weather.WeatherData;

public class WeatherController
{
    ArrayList<WeatherChangeEventListener> arListener = new ArrayList<WeatherChangeEventListener>();
    Timer updateTimer = null;
    WeatherData wd = null;

    private static final Logger logger = Logger.getLogger(WeatherController.class.getName());
    
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
          String strGetWoeidUrl = StokerWebProperties.getInstance().getProperty("weather_woeid_url");
          String strGetZipCode = StokerWebProperties.getInstance().getProperty("weather_zipcode");
          String strWeatherURL = StokerWebProperties.getInstance().getProperty("weather_get_by_woeid_url");

          //resultString = httpRequest("http://where.yahooapis.com/geocode?country=USA&flags=J&postal=30024");
          resultString = httpRequest(strGetWoeidUrl + strGetZipCode);

          if ( resultString != null)
          {
              logger.debug("Result String: [" + resultString +"]");
               JSONObject json = (JSONObject) JSONSerializer.toJSON( resultString );
               JSONObject resultSet = json.getJSONObject("ResultSet");
               JSONArray jsa =  (JSONArray)resultSet.getJSONArray( "Results" );
    
               String woeid = null;
               for ( Object o : jsa )
               {
                  JSONObject jo = (JSONObject) o;
                  woeid = jo.getString("woeid");
                  logger.debug("woeid: " + woeid);
               }
    
               //JSONObject results = json.getJSONObject("Results");
    
               if ( woeid != null)
               {
                  resultString = httpRequest(strWeatherURL + woeid );
                  json = (JSONObject) JSONSerializer.toJSON( resultString );
    
                  logger.debug("weather: " + resultString);
               }
               wd = YahooWeatherJsonServerHelper.parseYahooWeatherData( resultString );
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

    public void addEventListener( WeatherChangeEventListener listener )
    {
        synchronized ( this)
        {
           arListener.add( listener );

           WeatherChangeEvent wce = new WeatherChangeEvent( this, getWeather() );
           listener.weatherUpdated(wce);
        }

    }

    public void removeEventListener( WeatherChangeEventListener listener )
    {
        synchronized ( this)
        {
           arListener.remove( listener );
        }

    }

    protected void fireActionPerformed( WeatherChangeEvent ce )
    {
        synchronized ( this)
        {
            for ( WeatherChangeEventListener listener : arListener )
            {
                listener.weatherUpdated(ce);
            }
        }
    }

    private void updateWeatherAndFireEvent()
    {
        logger.info("Fetching Weather");
        wd = fetchWeather();
        logger.info("Weather fetch complete");
        WeatherChangeEvent wce = new WeatherChangeEvent(this,wd);
        fireActionPerformed( wce );
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
            logger.error("Attempting to start Weather Controller and it's already running.");
        }

    }

    public void stop()
    {
        updateTimer.cancel();
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
