package sweb.server.security.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import sweb.server.security.LoginProperties;

public class AddUser
{
   /**
    * @param args
    */
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
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      String password = PasswordField.readPassword("Enter password: ");
      String password2 = PasswordField.readPassword("Retype password: ");
      if ( password.compareTo(password2) == 0)
         LoginProperties.getInstance().addLoginIDAndPass(username, password);
      else
      {
         System.out.println("Password fields do no match.");
      }

   }

}
