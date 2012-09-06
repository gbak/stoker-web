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

package sweb.server.alerts;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;

public class AlertsClassLoader extends ClassLoader
{
   private Hashtable<String, Class<AlertsBase>> classes = new Hashtable<String, Class<AlertsBase>>();

   public AlertsClassLoader()
   {
      super(AlertsClassLoader.class.getClassLoader());
   }

   public Class<AlertsBase> loadClass(String className) throws ClassNotFoundException
   {
      return findClass(className);
   }

   @SuppressWarnings("unchecked")
   public Class<AlertsBase> findClass(String className)
   {
      byte classByte[];
      Class<AlertsBase> result = null;
      result = classes.get(className);
      if (result != null)
      {
         return result;
      }

      try
      {
         return (Class<AlertsBase>) findSystemClass(className);
      }
      catch (Exception e)
      {

      }

      try
      {
         String classPath = ((String) ClassLoader.getSystemResource(
               className.replace('.', File.separatorChar) + ".class").getFile()).substring(1);
         classByte = loadClassData(classPath);

         result = (Class<AlertsBase>) defineClass(className, classByte, 0, classByte.length,
               null);
         classes.put(className, result);
         return result;
      }
      catch (Exception e)
      {
         return null;
      }
   }

   private byte[] loadClassData(String className) throws IOException
   {

      File f;
      f = new File(className);
      int size = (int) f.length();
      byte buff[] = new byte[size];
      FileInputStream fis = new FileInputStream(f);
      DataInputStream dis = new DataInputStream(fis);
      dis.readFully(buff);
      dis.close();
      return buff;
   }

}

// Here is how to use the CustomClassLoader.
/*
 * public class CustomClassLoaderTest {
 *
 * public static void main(String [] args) throws Exception{ CustomClassLoader
 * test = new CustomClassLoader(); test.loadClass(com.test.HelloWorld); } }
 */