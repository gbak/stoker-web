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

package sweb.server.security;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import sweb.server.StokerConstants;
import sweb.server.StokerWebProperties;
import sweb.server.controller.data.DataOrchestrator;

public class LoginProperties extends Properties
{

    
    private static final long serialVersionUID = -6912224771507005561L;
    private volatile static LoginProperties lp = null;

    private LoginProperties()
    {
        super();
        try
        {
           // Loading the StokerWebProperties is required since it will add the
           // stokerweb_dir to the classpath.
           StokerWebProperties.getInstance();
            
           InputStream inputStream = this.getClass().getClassLoader()
                 .getResourceAsStream(StokerConstants.FILE_LOGIN_PROPERTIES);
           load( inputStream );

        }
        catch (IOException ioe)
        {
            System.out.println("Error loading login.properties");
        }
    }

    private LoginProperties( Properties p)
    {
        super(p);
    }

    public static LoginProperties getInstance()
    {
        if ( lp == null)
        {
            synchronized ( LoginProperties.class)
            {
                if ( lp == null )
                {
                    lp = new LoginProperties();
                }
            }
        }
        return lp;
    }

    private String createHashPass( String strPass)
    {
        return BCrypt.hashpw( strPass, BCrypt.gensalt());
    }

    public void addLoginIDAndPass(String strLoginID, String strPass)
    {
        setProperty(strLoginID, createHashPass(strPass));

        try {
           URL url = this.getClass().getClassLoader().getResource(StokerConstants.FILE_LOGIN_PROPERTIES);
           String strFile = url.getFile();
           
           OutputStream output = new FileOutputStream( strFile );
            //store(new FileOutputStream(StokerConstants.FILE_LOGIN_PROPERTIES), null);
           store( output, null );
            
        }
        catch (IOException e) {

        }
    }

    public boolean validateLoginID( String strLoginID, String strPassword)
    {
       String pass = getProperty(strLoginID);
       if ( pass != null )
           if (BCrypt.checkpw(strPassword, pass))
               return true;

        return false;
    }

    public static void main( String[] args)
    {
        LoginProperties.getInstance().addLoginIDAndPass("user", "pass");
    }
}
