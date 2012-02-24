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

package sweb.client;


import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

public class StokerWeb implements EntryPoint
{


    private long startTimeMillis;
    
    public void onModuleLoad()
    {
     
        /*
         * Install an UncaughtExceptionHandler which will produce <code>FATAL</code> log messages
         */
        Log.setUncaughtExceptionHandler();

        // use deferred command to catch initialization exceptions in onModuleLoad2
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
          @Override
          public void execute() {
            onModuleLoad2();
          }
        });

        

    }
    
    private void onModuleLoad2()
    {
        if (Log.isDebugEnabled()) 
        {
            startTimeMillis = System.currentTimeMillis();
          }
        
        Log.info("onModuleLoad()");
        MainPage mp = new MainPage();
        
        
        if (Log.isDebugEnabled()) {
            long endTimeMillis = System.currentTimeMillis();
            float durationSeconds = (endTimeMillis - startTimeMillis) / 1000F;
            Log.debug("Duration: " + durationSeconds + " seconds");
          }
    }
}


