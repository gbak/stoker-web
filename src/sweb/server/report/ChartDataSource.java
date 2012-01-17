package sweb.server.report;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class ChartDataSource implements JRDataSource
{
   int x = 0;
   
   public boolean next() throws JRException
   {
      System.out.println("Next" + new Integer(x).toString());
      if ( x++ < 10 )
         return true;
      return false;
   }

   public Object getFieldValue(JRField jrField) throws JRException
   {
   
      String sFieldName = jrField.getName();
      Object value = null;

      
      
      return value;
   }
}