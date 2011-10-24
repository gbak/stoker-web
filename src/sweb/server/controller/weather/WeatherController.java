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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import sweb.server.StokerWebProperties;
import sweb.server.controller.events.WeatherChangeEvent;
import sweb.server.controller.events.WeatherChangeEventListener;
import sweb.shared.model.weather.WeatherData;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class WeatherController
{
    ArrayList<WeatherChangeEventListener> arListener = new ArrayList<WeatherChangeEventListener>();
    Timer updateTimer = null;
    WeatherData wd = null;

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

          System.out.println("Result String: [" + resultString +"]");
           JSONObject json = (JSONObject) JSONSerializer.toJSON( resultString );
           JSONObject resultSet = json.getJSONObject("ResultSet");
           JSONArray jsa =  (JSONArray)resultSet.getJSONArray( "Results" );

           String woeid = null;
           for ( Object o : jsa )
           {
              JSONObject jo = (JSONObject) o;
              woeid = jo.getString("woeid");
              System.out.println("woeid: " + woeid);
           }

           //JSONObject results = json.getJSONObject("Results");

           if ( woeid != null)
           {
              resultString = httpRequest(strWeatherURL + woeid );
              json = (JSONObject) JSONSerializer.toJSON( resultString );

              System.out.println("weather: " + resultString);
           }
           wd = YahooWeatherJsonServerHelper.parseYahooWeatherData( resultString );
       }
       catch (IllegalStateException ise )
       {

       }
       catch ( net.sf.json.JSONException jse )
       {
          System.out.println("Error getting weather.");
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

              System.out.println("Response: " + resultString  );  // TODO: remove
          }
       }
       catch (IllegalStateException ise )
       {

       }
       catch (ClientProtocolException e)
       {
          // TODO Auto-generated catch block
          e.printStackTrace();
       }
       catch (IOException e)
       {
          // TODO Auto-generated catch block
          e.printStackTrace();
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
        wd = fetchWeather();
        WeatherChangeEvent wce = new WeatherChangeEvent(this,wd);
        fireActionPerformed( wce );
    }

    public void start()
    {
        if ( updateTimer == null )
        {
            updateTimer = new Timer();
          //  updateWeatherAndFireEvent();

            RunTimer _runTimer = new RunTimer();
            updateTimer.schedule( _runTimer, 0 );  // 900000
        }
        else
        {
            System.out.println("Attempting to start Weather Controller and it's already running.");
            // TODO: system log
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
