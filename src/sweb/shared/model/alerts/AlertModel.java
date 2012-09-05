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

package sweb.shared.model.alerts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AlertModel implements Serializable
{
   private static final long serialVersionUID = 3533993719511613483L;

   protected String  m_AlertName = "";
   protected boolean m_enabledInd = false;
   protected Set<String> availableDeliveryMethods = new HashSet<String>();
   protected Set<String> configuredDeliveryMethods = new HashSet<String>();
   
   public AlertModel()
   {
   }
   
   public AlertModel(String name)
   {
      m_AlertName = name;
   }
   
   public AlertModel( String name, boolean b)
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
