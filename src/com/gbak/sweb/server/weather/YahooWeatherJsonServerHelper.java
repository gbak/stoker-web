package com.gbak.sweb.server.weather;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.gbak.sweb.common.json.YahooWeatherData;
import com.gbak.sweb.shared.model.weather.WeatherData;



public class YahooWeatherJsonServerHelper
{
   public static WeatherData parseYahooWeatherData(String JSONString)
   {
      WeatherData yahooWeatherData = null;
      ObjectMapper om = new ObjectMapper();
      
      TypeReference<YahooWeatherData> typeRef = new TypeReference<YahooWeatherData>()
      {
      };

      YahooWeatherData ywd = null;
      try
      {
         ywd = om.readValue(JSONString, typeRef);
         System.out.println("ywd.query.created: " + ywd.query.created);

         String description = ywd.query.results.channel.item.description;
         String backslash = "\\";
         String urlDesc = description.substring(description.indexOf("src=") +5, description.indexOf(">") - 2 );
         yahooWeatherData = new WeatherData(ywd.query.results.channel.location.city, 
                                                        ywd.query.results.channel.location.region, 
                                                        ywd.query.results.channel.item.condition.temp,
                                                        ywd.query.results.channel.atmosphere.humidity, 
                                                        ywd.query.results.channel.wind.speed, 
                                                        ywd.query.results.channel.item.condition.text, 
                                                        urlDesc, 
                                                        ywd.query.results.channel.item.condition.code, 
                                                        ywd.query.results.channel.item.forecast.get(0).high,
                                                        ywd.query.results.channel.item.forecast.get(0).low, 
                                                        ywd.query.results.channel.item.forecast.get(0).text);
         yahooWeatherData.setURL(ywd.query.results.channel.image.link);
         yahooWeatherData.setLogo(ywd.query.results.channel.image.url);
      }
      catch (JsonParseException e)
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
      

      return yahooWeatherData;
   }
   


   
}