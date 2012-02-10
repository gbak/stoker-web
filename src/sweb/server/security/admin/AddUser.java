package sweb.server.security.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


import sweb.server.StokerWebProperties;
import sweb.server.logging.LoggingConfigurator;
import sweb.server.security.LoginProperties;

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
