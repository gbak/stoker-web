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

package sweb.server.log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import sweb.shared.model.data.SBlowerDataPoint;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.data.SProbeDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.devices.SDeviceBase;
import sweb.shared.model.logfile.LogNote;
import sweb.shared.model.stoker.StokerDeviceTypes;
import sweb.shared.model.stoker.StokerFan;
import sweb.shared.model.stoker.StokerPitSensor;
import sweb.shared.model.stoker.StokerProbe;
import sweb.shared.model.stoker.StokerDeviceTypes.DeviceType;
import sweb.shared.model.weather.WeatherData;

public class LogFileFormatter
{
    private static final String strDataPointSeperator = "|";
    private static final String strNewline = "\n";

    private static final String strDateFormat = "%1$tY%1$tm%1$td_%1$tH%1$tM%1$tS";

    private static final String strDataPrefix = "d:";
    private static final String strDataFormat = strDataPrefix + strDateFormat;

    private static final String strBlowerPrefix = "b:";
    private static final String strBlowerFormat = strBlowerPrefix + strDateFormat;
    
    private static final String strNotePrefix = "n:";
    private static final String strNoteFormat = strNotePrefix + strDateFormat;

    private static final String strWeatherPrefix = "w:";
    private static final String strWeatherDataFormat = "%5s:%5s:%5s:%s";;
    private static final String strWeatherFormat = strWeatherPrefix + strDateFormat;
    
    
    private static final String strDataPointFormat = "%2s:%03.1f";
    private static final String strDataBlowerFormat = "%2s:%1d";
  //  private static final String strDataPitFormat = "%2s:%03.1f:%1d";

    private static final String strSimpleDateFormat = "yyyyMMdd_HHmmss";
    private static final String strCookerFormat = "c:00:%02d:%s\n";
    private static final String strDeviceFormat = "c:%2s:%16s:%7s:%03d:%10s:%03d:%03d:%s:%16s\n";
     /*
      *  strDeviceFormat:
      *     number
      *     Device ID
      *     Type
      *     Alarm Enabled
      *     Alarm High
      *     Alarm Low
      *     Name
      *     FanID ( if any)
      */

    private static final Logger logger = Logger.getLogger(LogFileFormatter.class.getName());
    
    public static enum LineType  { CONFIG, BLOWER, DATA, WEATHER, NOTE, NONE };


    public static LineType getLineType( String prefix )
    {
       if ( prefix.length() > 2)
       {
           logger.error("Prefix is too long, something must be wrong");
       }
       
       if ( prefix.compareTo( "d:") == 0)
           return LineType.DATA;
       else if ( prefix.compareTo("b:") == 0)
           return LineType.BLOWER;
       else if ( prefix.compareTo("c:") == 0)
           return LineType.CONFIG;
       else if ( prefix.compareTo(":w") == 0 )
           return LineType.WEATHER;
       else if ( prefix.compareTo("n:") == 0 )
           return LineType.NOTE;
       else 
           return LineType.NONE;
       		
    }
    
    public static String generateLogHeader( String strCookerName, ArrayList<SDevice> arSD, HashMap<String,String> hmSDIndex )
    {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        // write c lines  ( cookers)
  //      ArrayList<Integer> cookers = getUniqueCookers(arSD);

        formatter.format(strCookerFormat,1,strCookerName);  //TODO: get Cooker Name


        for ( SDevice sd : arSD )
        {
            switch ( sd.getProbeType())
            {
                case BLOWER:
                    StokerFan sf = (StokerFan) sd;
                    formatter.format( strDeviceFormat, hmSDIndex.get(sf.getID()),
                            sf.getID(),
                            sf.getProbeType().toString(),0,
                            "",0,0,encode(sf.getName()),"");
                    break;
                case FOOD:
                    StokerProbe sp = (StokerProbe) sd;
                    formatter.format( strDeviceFormat, hmSDIndex.get( sp.getID()),
                            sp.getID(),
                            sp.getProbeType().toString(),sp.getTargetTemp(),
                            sp.getAlarmEnabled(),sp.getUpperTempAlarm(),sp.getLowerTempAlarm(),encode(sp.getName()),"");
                    break;
                case PIT:
                    StokerProbe sp2 = (StokerProbe) sd;
                    formatter.format( strDeviceFormat, hmSDIndex.get( sp2.getID() ),
                            sp2.getID(),
                            sp2.getProbeType().toString(),sp2.getTargetTemp(),
                            sp2.getAlarmEnabled(),sp2.getUpperTempAlarm(),sp2.getLowerTempAlarm(),encode(sp2.getName()),sp2.getFanDevice().getID());
                case UNKNOWN:
                    System.err.println("Unknown found while writing log header!");
                    System.err.println("Device: " + sd.toString());
                    break;
                default:


            }

        }

        sb.append("#start\n");
        return sb.toString();

    }


    public static String  logData(SDataPoint sdp, HashMap<String,String> hmSDIndex)
    {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter( sb, Locale.US);

        if (sdp instanceof SBlowerDataPoint )
        {
           //formatter.format( strDataPitFormat, hmSDIndex.get( sdp.getDeviceID()), sdp.getTempF(), sdp.bFanOn() ? 1 : 0);
            formatter.format( strDataBlowerFormat, hmSDIndex.get( sdp.getDeviceID()),((SBlowerDataPoint)sdp).isFanOn() ? 1 : 0);

        }
        else
        {
            formatter.format( strDataPointFormat, hmSDIndex.get( sdp.getDeviceID()), ((SProbeDataPoint)sdp).getTempF());
        }


        return sb.toString();
    }

    private static String encode( String enc )
    {
        String s = null;
        try
        {
            s = URLEncoder.encode(enc, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            logger.warn("Unable to encode string [" + enc + "]");
            s = "Unable to encode string";
        }    
        return s;
    }
    
    private static String decode( String dec )
    {
        String s = null;
        try
        {
            s = URLDecoder.decode(dec,"UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            logger.warn("Unable to decode note string");
            s = "Unable to decode string";
        }
        return s;
        
    }
    
    public static String logNoteDate( Date d)
    {
        Formatter format = new Formatter( Locale.US );
        return format.format( strNoteFormat, d).toString();  
    }
    
    public static String logNote( String note )
    {
        return encode( note );
    }
    
    public static String logPointSeperator()
    {
        return strDataPointSeperator;
    }
    public static String logEnd()
    {
        return strNewline;
    }

    public static String logDataDate( Date d )
    {
        Formatter format = new Formatter( Locale.US);
       return format.format(strDataFormat,d ).toString();
    }

    public static String logBlowerDate( Date d )
    {
        Formatter format = new Formatter( Locale.US);
       return format.format(strBlowerFormat,d ).toString();
    }

    public static String logWeatherDate( Date d )
    {
        Formatter format = new Formatter( Locale.US);
        return format.format(strWeatherFormat,d ).toString();
    }
    
    public static String logWeather(WeatherData wd )
    {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter( sb, Locale.US);

        formatter.format( strWeatherDataFormat, wd.getCurrentTemperature(), wd.getHumidity(), wd.getWindSpeed(), encode( wd.getText() ) );
        
        return sb.toString();
        
    }
    
    
 /*   private static ArrayList<Integer> getUniqueCookers(ArrayList<SDevice> arSD)
    {
        ArrayList<Integer> alcn = new ArrayList<Integer>();

        for ( SDevice sd : arSD )
        {
            if (! alcn.contains(new Integer(sd.getCookerNum()) ))
            {
                alcn.add( new Integer( sd.getCookerNum()));
            }
        }

        return alcn;
    }
*/

    public static StringBuilder parseLogDataSection(String s, ArrayList<ArrayList<SDataPoint>> arDP )
    {
        StringTokenizer st = new StringTokenizer(s, "\nd");

        while ( st.hasMoreTokens() )
        {
            String token = st.nextToken();

            logger.debug("Next Token: [" + token + "]");
        }
        return null;
    }

    public static void parseLogDataLine(String s, HashMap<String,String> hmSDIndex, ArrayList<ArrayList<SDataPoint>> arDP )
    {
        ArrayList<SDataPoint> ar = new ArrayList<SDataPoint>();
        if ( s.startsWith(strDataPrefix) || s.startsWith(strBlowerPrefix))
        {

            StringTokenizer st = new StringTokenizer(s, "|");
            String strDate = st.nextToken().substring(2);
            logger.debug("Date: " + strDate );
            SimpleDateFormat sdf = new SimpleDateFormat(strSimpleDateFormat);
            Date d = null;
            try
            {
                d = sdf.parse(strDate);
               logger.debug("Parsed Date: " + d.toString());

            }
            catch (ParseException e)
            {
                logger.error("Parse Exception while in parseLogDataLone ");
                logger.error(e.getStackTrace());
            }

            while ( st.hasMoreTokens() )
            {
                String token = st.nextToken();
                StringTokenizer st2 = new StringTokenizer( token, ":");
                String strDeviceID = hmSDIndex.get(st2.nextToken());
                String strValue = st2.nextToken();

                SDataPoint sdp = null;
                if ( strValue.contains("."))
                {
                    float F = new Double(strValue).floatValue();
                   sdp = new SProbeDataPoint( strDeviceID, d, F, calculateCelsius(F ));
                }
                else
                {
                    boolean blowerState = strValue.equalsIgnoreCase("1") ? true : false;

                    sdp = new SBlowerDataPoint( strDeviceID, d, blowerState );
                }

                ar.add( sdp );
            }
            arDP.add( ar );
        }

    }

    /** Parse out a single line of the configuration file.  This will only consider
     * and parse lines that begin with a c:.  All other lines will result in a
     * null return value
     * @param logLine Line to be parsed
     * @return SDevice for parsed line
     */

    public static LogNote parseNoteLine( String logLine )
    {
        if ( ! logLine.startsWith("n:" ))
        {
            return null;
        }
        
        StringTokenizer st = new StringTokenizer(logLine, "|");
        String strDate = st.nextToken().substring(2);
        SimpleDateFormat sdf = new SimpleDateFormat(strSimpleDateFormat);
        Date d = null;
        try
        {
            d = sdf.parse(strDate);
        }
        catch (ParseException e)
        {
           logger.error("Unable to parse date from line: [" + logLine + "]" );
        }

        
        String s = null;
        
        try
        {
            s = URLDecoder.decode(st.nextToken(),"UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            logger.warn("Unable to decode note string");
            s = "Unable to decode note string";
        }
        
        return new LogNote( d, s);
    }
    
    public static SDevice parseLogConfigLine( String logLine )
    {
        /*
         *  c:00:01:Large Egg
            c:01:db0000116f0bec30:   FOOD:190:ALARM_FOOD:250:075:food sensor:
            c:03:230000002a55c305: BLOWER:000:          :000:000:blower 1:
            c:02:e70000116f279030:    PIT:060:ALARM_FIRE:320:055:pit sensor:230000002a55c305
         * */

        if ( ! logLine.startsWith("c:") )
        {
           return null;

        }

        StringTokenizer st = new StringTokenizer( logLine, ":" );

        st.nextToken();  // c
        String strDeviceNum = st.nextToken();

        if ( strDeviceNum.compareTo("00") == 0 )
        {
            String strCookerNum = st.nextToken();
          return new SDeviceBase( strCookerNum, st.nextToken() );
        }

        String strDeviceID = st.nextToken();
        String strDeviceType = st.nextToken().trim();
        DeviceType deviceType = StokerDeviceTypes.getDeviceTypeForString( strDeviceType );
        String strTarget = st.nextToken();
        Integer target = new Integer( strTarget );
        String strAlarmType = st.nextToken();
        StokerProbe.AlarmType alarmType = StokerProbe.getAlarmTypeForString( strAlarmType );
        String strAlarmHigh = st.nextToken();
        Integer alarmHigh = new Integer( strAlarmHigh );
        String strAlarmLow = st.nextToken();
        Integer alarmLow = new Integer( strAlarmLow );
        String strName = decode(st.nextToken());
        String strBlowerID = "";
        if ( strDeviceType.compareTo("PIT") == 0 )
        {
            strBlowerID = st.nextToken();
        }

        SDevice sd;
        switch( deviceType )
        {
            case PIT:
                StokerProbe sp = new StokerProbe(strDeviceID, strName, target.intValue(), alarmHigh.intValue(), alarmLow.intValue(), alarmType );
                StokerFan sf = new StokerFan( strBlowerID, "" );
                sd = new StokerPitSensor(sp, sf);
                break;
            case FOOD:
                StokerProbe spFood = new StokerProbe(strDeviceID, strName, target.intValue(), alarmHigh.intValue(), alarmLow.intValue(), alarmType );
                sd = spFood;
                break;

            case BLOWER:
                StokerFan sfBlower = new StokerFan( strDeviceID, strName );
                sd = sfBlower;
                break;

            default:
                sd = null;

        }
     //   sd.setDeviceLogNum(strDeviceNum );  //TODO:  This was removed when deviceNum was removed from SDevice
                                              //       I don't think it is necessary

        return sd;

    }

    public static ArrayList<SDataPoint> parseLogDataLine(String s, HashMap<String,String> hmSDIndex )
    {
        ArrayList<SDataPoint> ar = new ArrayList<SDataPoint>();
        if ( s.startsWith(strDataPrefix) || s.startsWith(strBlowerPrefix))
        {

            StringTokenizer st = new StringTokenizer(s, "|");
            String strDate = st.nextToken().substring(2);
            logger.debug("Date: " + strDate );
            SimpleDateFormat sdf = new SimpleDateFormat(strSimpleDateFormat);
            Date d = null;
            try
            {
                d = sdf.parse(strDate);
               logger.debug("Parsed Date: " + d.toString());

            }
            catch (ParseException e)
            {
                logger.error("Error parsing date: " + e.getStackTrace());
            }

            while ( st.hasMoreTokens() )
            {
                String token = st.nextToken();
                StringTokenizer st2 = new StringTokenizer( token, ":");
                String strDeviceID = hmSDIndex.get(st2.nextToken());
                String strValue = st2.nextToken();

                SDataPoint sdp = null;
                if ( strValue.contains("."))
                {
                    float F = new Double(strValue).floatValue();
                   sdp = new SProbeDataPoint( strDeviceID, d, F, calculateCelsius(F ));
                }
                else
                {
                    boolean blowerState = strValue.equalsIgnoreCase("1") ? true : false;

                    sdp = new SBlowerDataPoint( strDeviceID, d, blowerState );
                }

                ar.add( sdp );
            }
        }

        return ar;
    }

    private static float calculateCelsius(float f) 
    {

        float celsius = (5/9) * (f -32);

        return celsius;
    }
    
    
}
