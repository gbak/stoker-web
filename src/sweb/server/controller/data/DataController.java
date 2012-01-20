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

package sweb.server.controller.data;

import java.util.ArrayList;

import sweb.server.controller.events.DataControllerEvent;
import sweb.server.controller.events.DataControllerEventListener;

/**
 * @author gary.bak
 *
 */
public abstract class DataController
{
    protected DataOrchestrator m_StokerDataStore = null;
    ArrayList<DataControllerEventListener> arListener = new ArrayList<DataControllerEventListener>();

    public abstract void start();
    public abstract void stop();
    public abstract boolean isReady();

    /**
     * Waits for the Controller to be in a Ready state.  If
     * the timeout is reached the method returns true
     *
     * @param lWaitTimeMills Time to wait before timeout
     * @return true if the timeout condition occurred.
     *
     */
    public abstract boolean waitForReady(long lWaitTimeMills );

    public void setDataStore( DataOrchestrator sds)
    {
        m_StokerDataStore = sds;
    }

    public void addEventListener( DataControllerEventListener listener )
    {
        synchronized( this )
        {
           arListener.add( listener );
        }
    }

    public void removeEventListener( DataControllerEventListener listener )
    {
        synchronized( this )
        {
            arListener.remove(listener);

        }
    }

    protected void fireActionPerformed( DataControllerEvent ce )
    {
        synchronized( this )
        {
            for ( DataControllerEventListener listener : arListener )
            {
                listener.actionPerformed(ce);
            }
        }
    }


}
