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

package sweb.server.controller.config;

import java.util.ArrayList;

import sweb.server.controller.HardwareDeviceConfiguration;
import sweb.server.controller.events.ConfigControllerEvent;
import sweb.server.controller.events.ConfigControllerEventListener;


public abstract class ConfigurationController
{
    ArrayList<ConfigControllerEventListener> arListener = new ArrayList<ConfigControllerEventListener>();

   public abstract void setConfiguration( HardwareDeviceConfiguration sc );

   public abstract void start();
   public abstract void stop();

   public abstract void loadNow();

   public void addEventListener( ConfigControllerEventListener listener )
   {
       synchronized ( this)
       {
          arListener.add( listener );
       }
   }

   public void removeEventListener( ConfigControllerEventListener listener )
   {
       synchronized ( this)
       {
          arListener.remove( listener );
       }
   }

   protected void fireActionPerformed( ConfigControllerEvent ce )
   {
       synchronized ( this)
       {
           for ( ConfigControllerEventListener listener : arListener )
           {
               listener.actionPerformed(ce);
           }
       }
   }

}
