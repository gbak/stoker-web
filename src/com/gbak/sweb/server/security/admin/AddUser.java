/**
 *  Stoker-web
 *
 *  Copyright (C) 2012  Gary Bak
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

package com.gbak.sweb.server.security.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.gbak.sweb.server.log.LoggingConfigurator;
import com.gbak.sweb.server.security.LoginProperties;


public class AddUser
{    
    private static final Logger logger = Logger.getLogger(LoginProperties.class.getName());
    
    public void addLoginAndPass( String username, String password)
    {

        new LoggingConfigurator().configure();
        LoginProperties.getInstance().addLoginIDAndPass(username, password);
        logger.info("User: [" + username + "] added");
    }
    
   public static void main(String[] args)
   {
      String username = "";
      System.out.print("Enter username: ");
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      try
      {
         username = in.readLine();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      
      System.out.print("Enter password: ");
      String password = new String(System.console().readPassword());
      System.out.print("Retype password: ");
      String password2 = new String(System.console().readPassword());
      
      
      
      if ( password.compareTo(password2) == 0)
          new AddUser().addLoginAndPass( username, password );
      else
      {
         System.out.println("Password fields do no match.");
      }

   }

}
