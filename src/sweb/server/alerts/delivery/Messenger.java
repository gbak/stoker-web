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

package sweb.server.alerts.delivery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import java.util.Set;

public class Messenger
{
    public Messenger()  { }
    
    public static void deliver(Collection<String> deliverTo, ArrayList<String> message )
    {
        for ( String delivery : deliverTo )
        {
            if ( delivery.compareTo("Browser Alert") == 0)
            {
                Notify ad = new NotifyByBrowser();
                ad.sendAlert(message);
            }
            else if ( delivery.compareTo("Email") == 0 )
            {
                Notify ad = new NotifyByEmail();
                ad.sendAlert(message);
            }
        }
    }
    
    public static Set<String> getDeliveryChannels()
    {
        Set<String> availableDeliveryList = new HashSet<String>();
        availableDeliveryList.add( "Email" );
        availableDeliveryList.add( "Browser Alert" );

        return availableDeliveryList;
    }
}
