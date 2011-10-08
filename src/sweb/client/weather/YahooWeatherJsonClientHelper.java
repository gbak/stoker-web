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

package sweb.client.weather;


import sweb.shared.model.weather.WeatherData;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;


public class YahooWeatherJsonClientHelper
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

      JSONValue jsonValue = JSONParser.parseStrict( JSONString );
      JSONObject jso = jsonValue.isObject();

      JSONObject jsoCondition = null;
      jsonValue = jso.get(PARAM_YAHOO_CONDITION);
      if (( jsoCondition = jsonValue.isObject()) != null )
      {
         strCode = getStringFromJSONObject( jsoCondition,ATT_YAHOO_C_CODE);
         strImage = getStringFromJSONObject( jsoCondition,ATT_YAHOO_C_IMAGE);
         strCurrentTemp = getStringFromJSONObject( jsoCondition,ATT_YAHOO_C_TEMPERATURE);
         strCurrentText = getStringFromJSONObject( jsoCondition,ATT_YAHOO_C_TEXT);
      }

      JSONObject jsoLocation = null;
      jsonValue = jso.get(PARAM_YAHOO_LOCATION);
      if (( jsoLocation = jsonValue.isObject()) != null )
      {
         strCity = getStringFromJSONObject( jsoLocation,ATT_YAHOO_L_CITY);
         strState = getStringFromJSONObject( jsoLocation,ATT_YAHOO_L_STATE);
      }

      // Units
      JSONObject jsoUnits = null;
      jsonValue = jso.get(PARAM_YAHOO_UNIT);
      if (( jsoUnits = jsonValue.isObject()) != null )
      {
         strUnitsDistance = getStringFromJSONObject( jsoUnits,ATT_YAHOO_U_DISTANCE);
         strUnitsPressure = getStringFromJSONObject( jsoUnits,ATT_YAHOO_U_PRESSURE);
         strUnitsSpeed = getStringFromJSONObject( jsoUnits,ATT_YAHOO_U_SPEED);
         strUnitsTemperature = getStringFromJSONObject( jsoUnits,ATT_YAHOO_U_TEMPERATURE);
      }

      // Atmosphere
      JSONObject jsoAtmosphere = null;
      jsonValue = jso.get(PARAM_YAHOO_ATMOSPHERE);
      if (( jsoAtmosphere = jsonValue.isObject()) != null )
      {
         strHumidity = getStringFromJSONObject( jsoAtmosphere,ATT_YAHOO_A_HUMIDITY);
         strPressure = getStringFromJSONObject( jsoAtmosphere,ATT_YAHOO_A_PRESSURE);
         strRising = getStringFromJSONObject( jsoAtmosphere,ATT_YAHOO_A_RISING);
         strVisibility = getStringFromJSONObject( jsoAtmosphere,ATT_YAHOO_A_VISIBILITY);
      }

      //  Wind
      JSONObject jsoWind = null;
      jsonValue = jso.get(PARAM_YAHOO_WIND);
      if (( jsoWind = jsonValue.isObject()) != null )
      {
         strWindDirection = getStringFromJSONObject( jsoWind,ATT_YAHOO_W_DIRECTION);
         strWindSpeed = getStringFromJSONObject( jsoWind,ATT_YAHOO_W_SPEED);
      }

      //  Forecast  ( today only)
      JSONArray jsaForecast = null;
      jsonValue =  jso.get(PARAM_YAHOO_FORECAST);
      if (( jsaForecast = jsonValue.isArray()) != null )
      {
         for ( int x = 0; x < jsaForecast.size(); x++ )
         {
            JSONObject jsoForecast = null;
            jsonValue = jsaForecast.get(x);
            if (( jsoForecast = jsonValue.isObject()) != null )
            {
               String strDay = getStringFromJSONObject( jsoForecast,ATT_YAHOO_F_DAY);
               if ( strDay.compareToIgnoreCase("Tomorrow") == 0)
                  continue;

               strForecastCondition = getStringFromJSONObject( jsoForecast,ATT_YAHOO_F_CONDITION);
               strForecastHigh = getStringFromJSONObject( jsoForecast,ATT_YAHOO_F_HIGH_TEMP);
               strForecastLow = getStringFromJSONObject( jsoForecast,ATT_YAHOO_F_LOW_TEMP);
            }
         }
      }

      strLogo = getStringFromJSONObject( jso,PARAM_YAHOO_LOGO);
      strURL = getStringFromJSONObject( jso,PARAM_YAHOO_URL);



      System.out.println("jso: " + jso.toString());
      System.out.println("jsoConditions: " + jsoCondition.toString());

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

   private static String getStringFromJSONObject(JSONObject jsv, String strAttribute)
   {
      String str = "";
      JSONValue jvalue = jsv.get(strAttribute);
      if ( jvalue != null ) {

         JSONNumber jsNum = jvalue.isNumber();
         if ( jsNum != null ) {
            str = new Double(jsNum.doubleValue()).toString();
            System.out.println("Str: " + str);
            return str;
         }

         JSONString jssStr = jvalue.isString();
         if ( jssStr != null ) {
            str = unQuote(jssStr.toString());
            System.out.println("Str: " + str);
         }
      }
      return str;
   }

   private static String unQuote( String s )
   {
      return s.replace("\"", "");

   }
   public static void main( String[] a )
   {
      YahooWeatherJsonClientHelper.parseYahooWeatherData("{\"units\":{\"temperature\":\"F\",\"speed\":\"mph\",\"distance\":\"mi\",\"pressure\":\"in\"},\"location\":{\"location_id\":\"USGA0548\",\"city\":\"Suwanee\",\"state_abbreviation\":\"GA\",\"country_abbreviation\":\"US\",\"elevation\":1001,\"latitude\":34.05000000000000,\"longitude\":-84.06999999999999},\"wind\":{\"speed\":9.00000000000000,\"direction\":\"WNW\"},\"atmosphere\":{\"humidity\":\"34\",\"visibility\":\"10\",\"pressure\":\"29.83\",\"rising\":\"falling\"},\"url\":\"http://weather.yahoo.com/forecast/USGA0548.html\",\"logo\":\"http://l.yimg.com/a/i/us/nt/ma/ma_nws-we_1.gif\",\"astronomy\":{\"sunrise\":\"06:54\",\"sunset\":\"20:29\"},\"condition\":{\"text\":\"Sunny\",\"code\":\"32\",\"image\":\"http://l.yimg.com/a/i/us/we/52/32.gif\",\"temperature\":93.00000000000000},\"forecast\":[{\"day\":\"Today\",\"condition\":\"Mostly Sunny\",\"high_temperature\":\"92\",\"low_temperature\":\"70\"},{\"day\":\"Tomorrow\",\"condition\":\"PM Thunderstorms\",\"high_temperature\":\"91\",\"low_temperature\":\"68\"}]}");
   }

}