package sweb.shared.model.alerts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class AlertDeliveryMethod
{
   public final String DELIVERY_SOUND = "SOUND";
   public final String DELIVERY_EMIAL = "EMAIL";

   private Set<String> deliveryList = new HashSet<String>();

   public AlertDeliveryMethod() { }
   public AlertDeliveryMethod( ArrayList<String> deliveryList ) { this.deliveryList.addAll(deliveryList); }
   
   public void add( String s ) { deliveryList.add( s ); } 
   
   
   
}
