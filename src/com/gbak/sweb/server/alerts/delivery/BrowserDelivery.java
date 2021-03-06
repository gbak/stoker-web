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

package com.gbak.sweb.server.alerts.delivery;

import java.io.Serializable;

import com.gbak.sweb.server.ClientMessenger;
import com.google.inject.Inject;


public class BrowserDelivery
{

    private static ClientMessenger clientMessenger;
    
    @Inject
    private BrowserDelivery( ClientMessenger cm )
    {
        clientMessenger = cm;
    }
    
    public static void send( Serializable message )
    {
        
        clientMessenger.push( message );
    }
}
