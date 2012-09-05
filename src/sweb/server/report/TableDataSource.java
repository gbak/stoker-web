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

package sweb.server.report;


import java.text.SimpleDateFormat;
import java.util.ArrayList;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class TableDataSource implements JRDataSource
{
   int index = 0;
   
   // The use of an ArrayList depends on if the data comes in date order, it should
   // since that is the way the file is parsed, but if somehow it does not then we'll
   // need a container that can order by date.
   
   ArrayList<TableEntry> arTE = new ArrayList<TableEntry>();
   
   public void addTableEntry( TableEntry te )
   {
       arTE.add( te );
   }
   public boolean next() throws JRException
   {
      if ( index < arTE.size() )
         return true;
      return false;
   }

   public Object getFieldValue(JRField jrField) throws JRException
   {
   
      String sFieldName = jrField.getName();
      Object value = null;
      
      if("value1".equals(sFieldName))
      {
         TableEntry te = arTE.get(index); 
         value = te.getActionType().toString();
      }
      else if("value2".equals(sFieldName))
      {
         TableEntry te = arTE.get(index);
         SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

         if ( te.getActionDate() == null )
             value = "";
         else
            value = format.format(te.getActionDate());
      }
      else if ("value3".equals(sFieldName))
      {
          TableEntry te = arTE.get(index);
          value = te.getMessageString();          
          index++;
      }
      

      return value;
   }
}