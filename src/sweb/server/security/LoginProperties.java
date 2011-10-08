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
import java.util.Properties;

import sweb.server.controller.data.DataOrchestrator;

public class LoginProperties extends Properties
{

    private static final String LOGIN_PROPERTIES = "login.properties";
    private static final long serialVersionUID = -6912224771507005561L;
    private volatile static LoginProperties lp = null;

    private LoginProperties()
    {
        super();
        try
        {
            load( new FileInputStream(LOGIN_PROPERTIES));

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
            synchronized ( DataOrchestrator.class)
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

    public void setLoginIDAndPass(String strLoginID, String strPass)
    {
        setProperty(strLoginID, createHashPass(strPass));

        try {
            store(new FileOutputStream(LOGIN_PROPERTIES), null);
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
        LoginProperties.getInstance().setLoginIDAndPass("garybak@gmail.com", "stokerweb");
    }
}
