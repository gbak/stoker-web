package sweb.common.json;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

public class YahooWeatherData
{

   public  Query query;
    
    public static class Query 
    {
       public int count;
       public String created;
       public String lang;
       @JsonProperty(value="results")
       public Results results;
       public Query() {}
    }
    
    public static class Results 
    {
       @JsonProperty(value="channel")
       public Channel channel;
       public Results() {}
    }
    
    public static class Channel
    {
       public String title;
       public String link;
       public String description;
       public String language;
       public String lastBuildDate;
       public String ttl;
       public Location location;
       public Units units;
       public Wind wind;
       public Atmosphere atmosphere;
       public Astronomy astronomy;
       public Image image;
       public Item item;
    }
    
    public static class Item
    {
       public String title;
       public String lat;
       @JsonProperty(value="long")
       public String longitude; 
       public String link;
       public String pubDate;
       public Condition condition;
       public String description;
       public List<Forecast> forecast;
       public Guid guid;
       
       public Item()
       {
          forecast = new ArrayList<Forecast>();
       }
    }
    
    public static class Guid
    {
       public String isPermaLink;
       public String content;
    }
    
    public static class Forecast
    {
       public String code;
       public  String date;
       public String  day;
       public String high;
       public String low;
       public String text;
    }
    
    public static class Condition
    {
       public String code;
       public String date; 
       public String temp;
       public String text;
    }
    
    public static class Image
    {
       public String title;
       public String width;
       public String height;
       public String link;
       public String url;
    }
    
    public static class Astronomy
    {
       public String sunrise;
       public String sunset;
    }
    public static class Atmosphere
    {
       public String humidity;
       public String pressure;
       public String rising;
       public String visibility;
    }
    
    public static class Wind
    {
       public String chill;
       public String direction;
       public String speed;
    }
    public static class Units
    {
       public String distance;
       public String pressure;
       public String speed;
       public String temperature;
    }
    
    public static class Location
    {
       public String city;
       public String country;
       public String region;
    }
}
