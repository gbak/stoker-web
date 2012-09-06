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

package sweb.server.weather;

import org.apache.log4j.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import sweb.shared.model.weather.WeatherData;


public class YahooWeatherJsonServerHelper
{
   private static final String PARAM_YAHOO_LOCATION = "location";  // L
   private static final String PARAM_YAHOO_UNIT = "units";         // U
   private static final String PARAM_YAHOO_ATMOSPHERE = "atmosphere";  // A
   private static final String PARAM_YAHOO_CONDITION = "condition";  // C
   private static final String PARAM_YAHOO_WIND = "wind";  // W
   private static final String PARAM_YAHOO_FORECAST = "forecast";  // F
   private static final String PARAM_YAHOO_URL = "url";
   private static final String PARAM_YAHOO_LOGO = "logo";
   // Location
   private static final String ATT_YAHOO_L_CITY = "city";
   private static final String ATT_YAHOO_L_STATE = "state_abbreviation";

   // Units
   private static final String ATT_YAHOO_U_DISTANCE = "distance";
   private static final String ATT_YAHOO_U_PRESSURE = "pressure";
   private static final String ATT_YAHOO_U_SPEED = "speed";
   private static final String ATT_YAHOO_U_TEMPERATURE = "temperature";


   // Atmosphere
   private static final String ATT_YAHOO_A_HUMIDITY = "humidity";
   private static final String ATT_YAHOO_A_PRESSURE = "pressure";
   private static final String ATT_YAHOO_A_RISING = "rising";
   private static final String ATT_YAHOO_A_VISIBILITY = "visibility";

   // Condition
   private static final String ATT_YAHOO_C_CODE = "code";
   private static final String ATT_YAHOO_C_IMAGE = "image";
   private static final String ATT_YAHOO_C_TEMPERATURE = "temperature";
   private static final String ATT_YAHOO_C_TEXT = "text";

   // Wind
   private static final String ATT_YAHOO_W_DIRECTION = "direction";
   private static final String ATT_YAHOO_W_SPEED = "speed";

   // Forecast
   private static final String ATT_YAHOO_F_CONDITION = "condition";
   private static final String ATT_YAHOO_F_DAY = "day";
   private static final String ATT_YAHOO_F_HIGH_TEMP = "high_temperature";
   private static final String ATT_YAHOO_F_LOW_TEMP = "low_temperature";

   private static final Logger logger = Logger.getLogger(YahooWeatherJsonServerHelper.class.getName());
   
   public static WeatherData parseYahooWeatherData( String JSONString )
   {
      String strCity = null;
      String strState = null;
      String strDistance = null;
      String strPressure = null;
      String strRising = null;
      String strVisibility = null;
      String strCode = null;
      String strHumidity = null;
      String strImage = null;
      String strCurrentTemp = null;
      String strCurrentText = null;
      String strWindDirection = null;
      String strWindSpeed = null;
      String strForecastCondition = null;
      String strForecastHigh = null;
      String strForecastLow = null;
      String strUnitsDistance = null;
      String strUnitsPressure = null;
      String strUnitsSpeed = null;
      String strUnitsTemperature = null;
      String strURL = null;
      String strLogo = null;

      //JSONValue jsonValue = JSONParser.parseStrict( JSONString );


      JSONObject JSONFullObject = (JSONObject) JSONSerializer.toJSON( JSONString );

      JSONObject jsoCondition =  JSONFullObject.getJSONObject( PARAM_YAHOO_CONDITION );

      strCode =  jsoCondition.getString(ATT_YAHOO_C_CODE);
      strImage = jsoCondition.getString(ATT_YAHOO_C_IMAGE);
      strCurrentTemp = jsoCondition.getString(ATT_YAHOO_C_TEMPERATURE);
      strCurrentText = jsoCondition.getString(ATT_YAHOO_C_TEXT);

      JSONObject jsoLocation = JSONFullObject.getJSONObject( PARAM_YAHOO_LOCATION );

      strCity = jsoLocation.getString(ATT_YAHOO_L_CITY);
      strState = jsoLocation.getString(ATT_YAHOO_L_STATE);

      // Units
      JSONObject jsoUnits = JSONFullObject.getJSONObject( PARAM_YAHOO_UNIT );

      strUnitsDistance = jsoUnits.getString(ATT_YAHOO_U_DISTANCE);
      strUnitsPressure = jsoUnits.getString(ATT_YAHOO_U_PRESSURE);
      strUnitsSpeed = jsoUnits.getString(ATT_YAHOO_U_SPEED);
      strUnitsTemperature = jsoUnits.getString(ATT_YAHOO_U_TEMPERATURE);

      // Atmosphere
      JSONObject jsoAtmosphere = JSONFullObject.getJSONObject( PARAM_YAHOO_ATMOSPHERE );

      strHumidity = jsoAtmosphere.getString(ATT_YAHOO_A_HUMIDITY);
      strPressure = jsoAtmosphere.getString(ATT_YAHOO_A_PRESSURE);
      strRising = jsoAtmosphere.getString(ATT_YAHOO_A_RISING);
      strVisibility = jsoAtmosphere.getString(ATT_YAHOO_A_VISIBILITY);

      //  Wind
      JSONObject jsoWind = JSONFullObject.getJSONObject( PARAM_YAHOO_WIND );

      strWindDirection = jsoWind.getString(ATT_YAHOO_W_DIRECTION);
      strWindSpeed = jsoWind.getString(ATT_YAHOO_W_SPEED);

      //  Forecast  ( today only)
      JSONArray jsaForecast = JSONFullObject.getJSONArray( PARAM_YAHOO_FORECAST );

      for ( Object oForecast :  jsaForecast )
      {
         JSONObject jsoForecast = (JSONObject) oForecast;

         String strDay = jsoForecast.getString(ATT_YAHOO_F_DAY);
         if ( strDay.compareToIgnoreCase("Tomorrow") == 0)
            continue;

         strForecastCondition = jsoForecast.getString(ATT_YAHOO_F_CONDITION);
         strForecastHigh = jsoForecast.getString(ATT_YAHOO_F_HIGH_TEMP);
         strForecastLow = jsoForecast.getString(ATT_YAHOO_F_LOW_TEMP);

      }


      strLogo = JSONFullObject.getString(PARAM_YAHOO_LOGO);
      strURL = JSONFullObject.getString(PARAM_YAHOO_URL);



      logger.debug("jso: " + JSONFullObject.toString());
      logger.debug("jsoConditions: " + jsoCondition.toString());

      /*
       *   public WeatherData( String strCity, String strCountry, String strCurrentTemp,
                       String strHumidity, String strWindSpeed, String strText,
                       String strCurrentImage, String strCode, String strForecastHigh,
                       String strForecastLow, String strForecastCondition )

       */
      WeatherData yahooWeatherData = new WeatherData( strCity, strState, strCurrentTemp,
                                                      strHumidity, strWindSpeed, strCurrentText,
                                                      strImage, strCode, strForecastHigh,
                                                      strForecastLow, strForecastCondition );
      yahooWeatherData.setURL( strURL );
      yahooWeatherData.setLogo( strLogo );

      return yahooWeatherData;

   }

   private static String unQuote( String s )
   {
      return s.replace("\"", "");

   }
   public static void main( String[] a )
   {
      YahooWeatherJsonServerHelper.parseYahooWeatherData("{\"units\":{\"temperature\":\"F\",\"speed\":\"mph\",\"distance\":\"mi\",\"pressure\":\"in\"},\"location\":{\"location_id\":\"USGA0548\",\"city\":\"Suwanee\",\"state_abbreviation\":\"GA\",\"country_abbreviation\":\"US\",\"elevation\":1001,\"latitude\":34.05000000000000,\"longitude\":-84.06999999999999},\"wind\":{\"speed\":9.00000000000000,\"direction\":\"WNW\"},\"atmosphere\":{\"humidity\":\"34\",\"visibility\":\"10\",\"pressure\":\"29.83\",\"rising\":\"falling\"},\"url\":\"http://weather.yahoo.com/forecast/USGA0548.html\",\"logo\":\"http://l.yimg.com/a/i/us/nt/ma/ma_nws-we_1.gif\",\"astronomy\":{\"sunrise\":\"06:54\",\"sunset\":\"20:29\"},\"condition\":{\"text\":\"Sunny\",\"code\":\"32\",\"image\":\"http://l.yimg.com/a/i/us/we/52/32.gif\",\"temperature\":93.00000000000000},\"forecast\":[{\"day\":\"Today\",\"condition\":\"Mostly Sunny\",\"high_temperature\":\"92\",\"low_temperature\":\"70\"},{\"day\":\"Tomorrow\",\"condition\":\"PM Thunderstorms\",\"high_temperature\":\"91\",\"low_temperature\":\"68\"}]}");
   }

}