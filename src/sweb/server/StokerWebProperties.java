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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

public class StokerWebProperties extends Properties
{

    private static final long serialVersionUID = 1402956556397035116L;
    private volatile static StokerWebProperties swp = null;
    private static String stokerWebDir = null;

    private static final Logger logger = Logger.getLogger(StokerWebProperties.class.getName());
    
    private StokerWebProperties()
    {
        super();
        
        Map<String,String> mapEnv = System.getenv();
        stokerWebDir = mapEnv.get(StokerConstants.ENV_STOKERWEB_DIR);
        if ( stokerWebDir == null )
        {
           logger.error("Unable to find STOKERWEB_DIR environment variable, using '.'");
           stokerWebDir = ".";
        }
        if ( stokerWebDir.endsWith("/") || stokerWebDir.endsWith("\\"))
            stokerWebDir = stokerWebDir.substring(0, stokerWebDir.length() - 1);
        
        logger.info("Using StokerWebDir = ["+ stokerWebDir + "]");
        try
        {
            // add path to classpath for the properties load
            addPath( stokerWebDir );
     
            //load( new FileInputStream(StokerConstants.FILE_STOKERWEB_PROPERTIES));
            InputStream inputStream = this.getClass().getClassLoader()
                  .getResourceAsStream(StokerConstants.FILE_STOKERWEB_PROPERTIES);
            load( inputStream );
            
        }
        catch (IOException ioe)
        {
            logger.fatal("Error loading stokerWeb.properties: " + ioe.getStackTrace());
            System.exit(1);
        }
      catch (Exception e)
      {
    	  String strError = "Error loading stokerWeb.properties, likely caused by error while adding stokerWebDir to classpath"; 
         logger.fatal(strError);
         logger.fatal(e.getStackTrace());
         System.out.println(strError);
         System.exit(1);
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
            synchronized ( StokerWebProperties.class )
            {
                if ( swp == null )
                {
                    swp = new StokerWebProperties();
                    swp.setProperty(StokerConstants.PROPS_STOKERWEB_DIR, stokerWebDir );
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
            logger.error("Unable to convert property [" + StokerConstants.PROPS_LOG_FILE_PERIOD + "] with value [" +
                    tmpString + "] to a long");
            throw new InvalidStokerWebPropertyException(StokerConstants.PROPS_LOG_FILE_PERIOD);
        }
        return retval;
    }
    
    public static void addPath(String s) throws Exception 
    {
       File f = new File(s);
       URL u = f.toURI().toURL();
       URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
       Class<URLClassLoader> urlClass = URLClassLoader.class;
       Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
       method.setAccessible(true);
       method.invoke(urlClassLoader, new Object[]{u});
     }

    public HashMap<String,String> getClientProperties()
    {
        HashMap<String,String> hm = new HashMap<String,String>();
        
        for ( Map.Entry<Object,Object> e : swp.entrySet() )
        {
            String key = (String)e.getKey();
            String value = (String)e.getValue();
            
            if ( key.contains("client") )
            {
                hm.put( key, value );
            }
        }
        
        return hm;
    }
}
