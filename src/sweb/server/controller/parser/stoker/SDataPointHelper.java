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

package sweb.server.controller.parser.stoker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import sweb.server.controller.Controller;
import sweb.server.controller.HardwareDeviceConfiguration;
import sweb.shared.model.data.SBlowerDataPoint;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.data.SProbeDataPoint;
/*
 *  Helper class to SDataPoint since GWT did not like the complex set of classes
 *  in the shared section.
 */
public class SDataPointHelper
{
    public static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
    public static final String OUTPUT_FORMAT = "%3s|%15s|%16s|%5.1f|%5.1f|%1s|%1s\n";

    private static final Logger logger = Logger.getLogger(SDataPointHelper.class.getName());
    

    private static String getTime(Date d)
    {
       SimpleDateFormat sdf = new SimpleDateFormat( DATE_FORMAT );
       return sdf.format( d );
    }

    public static ArrayList<SDataPoint> createDataPoint(String strIN ) throws InvalidDataPointException
    {
        ArrayList<SDataPoint> arDP = new ArrayList<SDataPoint>();

        String strInString = strIN.toLowerCase();

        String deviceID = new String();
        float fTempC = 0;
        float fTempF = 0;
        boolean bHasFan = false;
        boolean bFanOn = false;

       int deviceColonPos = strInString.indexOf(':');
       if ( deviceColonPos == 16)
       {
          deviceID = strInString.substring(0, deviceColonPos);
          StringTokenizer st =new StringTokenizer( strInString );
          int iTokenIndex = 0;
          while ( st.hasMoreTokens() )
          {
             String strToken = st.nextToken();
             iTokenIndex++;
             switch ( iTokenIndex )
             {
                case 9:
                   fTempC = new Double(strToken).floatValue();
                   break;
                case 10:
                   fTempF = new Double( strToken).floatValue();
                   break;

             }
             if ( iTokenIndex > 10)
             {
                bHasFan = true;
                if ( strToken.contains("tgt:"))
                {
                   // Target Temp in C
                }
                if ( strToken.contains("blwr:"))
                {
                   bFanOn = strToken.substring(5).compareTo("on") == 0? true : false;
                }
             }
          }

          if ( bHasFan )
          {
              String strBlowerID = Controller.getInstance().getStokerConfiguration().getBlowerID(deviceID);
              SBlowerDataPoint sdp = new SBlowerDataPoint( strBlowerID, Calendar.getInstance().getTime(), bFanOn );
              arDP.add( sdp );
          }

          SProbeDataPoint pdp = new SProbeDataPoint(deviceID, Calendar.getInstance().getTime(),  fTempF , fTempC);
          arDP.add( pdp );


       }
       else
          throw (new InvalidDataPointException(strInString));

    return arDP;


    }

}
