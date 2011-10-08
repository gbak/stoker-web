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

package sweb.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import sweb.server.controller.data.DataOrchestrator;

public class StokerWebProperties extends Properties
{

    private static final long serialVersionUID = 1402956556397035116L;
    private volatile static StokerWebProperties swp = null;

    private StokerWebProperties()
    {
        super();
        try
        {
            load( new FileInputStream("stokerWeb.properties"));
            //System.out.println("Stoker IP Address: " + stokerWebProps.getProperty("stoker_ip"));
        }
        catch (IOException ioe)
        {
            System.out.println("Error loading stokerWeb.properties");
        }
    }

    private StokerWebProperties( Properties p)
    {
        super(p);
    }

    public static StokerWebProperties getInstance()
    {
        if ( swp == null)
        {
            synchronized ( DataOrchestrator.class)
            {
                if ( swp == null )
                {
                    swp = new StokerWebProperties();
                }
            }
        }
        return swp;
    }


    public long getLongProperty( String s ) throws InvalidStokerWebPropertyException
    {
        String tmpString = "";
        long retval;
        try
        {
           tmpString = StokerWebProperties.getInstance().getProperty(StokerConstants.PROPS_LOG_FILE_PERIOD);
           if ( tmpString == null )
               throw new NumberFormatException();

           retval = new Long(tmpString).longValue();

        }
        catch(NumberFormatException nfe)
        {
            System.err.println("Unable to convert property [" + StokerConstants.PROPS_LOG_FILE_PERIOD + "] with value [" +
                    tmpString + "] to a long");
            throw new InvalidStokerWebPropertyException(StokerConstants.PROPS_LOG_FILE_PERIOD);
        }
        return retval;
    }
}
