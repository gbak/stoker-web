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