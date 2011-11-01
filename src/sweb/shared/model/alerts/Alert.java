package sweb.shared.model.alerts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Alert implements Serializable
{
   private static final long serialVersionUID = 3533993719511613483L;

   protected String  m_AlertName = "";
   protected boolean m_enabledInd = false;
   protected Set<String> availableDeliveryMethods = new HashSet<String>();
   protected Set<String> configuredDeliveryMethods = new HashSet<String>();
   
   public Alert()
   {
   }
   
   public Alert(String name)
   {
      m_AlertName = name;
   }
   
   public Alert( String name, boolean b)
   {
      m_AlertName = name;
      m_enabledInd = b;
   }
   
   public boolean getEnabled()
   {
      return m_enabledInd;
   }
   
   public void setEnabled( boolean b)
   {
      m_enabledInd = b;
   }
   
   public String getName()
   {
      return m_AlertName;
   }
   
   public void setName( String s)
   {
      m_AlertName = s;
   }
   
   public void setAvailableDeliveryMethods( Set<String> deliveryList )
   {
       availableDeliveryMethods = deliveryList;
   }
   
   public void addAvailableDeliveryMethods( ArrayList<String> deliveryList )
   {
       availableDeliveryMethods.addAll(deliveryList);
   }
   
   public void setConfiguredDeliveryMethods( Set<String> deliveryList )
   {
       configuredDeliveryMethods = deliveryList;
   }
   
   public void addConfiguredDeliveryMethods( ArrayList<String> deliveryList )
   {
       configuredDeliveryMethods.addAll(deliveryList);
   }
   
   public Set<String> getAvailableDeliveryMethods()
   {
       return availableDeliveryMethods;
   }
   
   public Set<String> getConfiguredDeliveryMethods()
   {
       return configuredDeliveryMethods;
   }
}
