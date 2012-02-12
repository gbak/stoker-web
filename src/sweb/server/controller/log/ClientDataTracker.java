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

package sweb.server.controller.log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import sweb.server.controller.data.DataOrchestrator;
import sweb.server.controller.events.BlowerEvent;
import sweb.server.controller.events.BlowerEventListener;
import sweb.shared.model.data.SBlowerDataPoint;
import sweb.shared.model.data.SDataPoint;

/*
 * This class saves the last datapoints that were requested.
 * on the next request, if the dps have not changed, they are
 * not sent.
 *
 * This was originally in the StokerDataStore class, but if
 * multiple callers are requesting the data, then they may
 * get inaccurate results, since they may not have been the
 * last requestor.
 *
 * each entity that is requesting data needs to create
 * an instance of this class and call it.
 *
 */
public class ClientDataTracker
{
    HashMap<String,SDataPoint> hmLastChecked = new HashMap<String,SDataPoint>();
    ArrayList<SDataPoint> arBlowerHistory = new ArrayList<SDataPoint>();

    public ClientDataTracker()
    {

       DataOrchestrator.getInstance().addListener( new BlowerEventListener() {

        public void stateChange(BlowerEvent be)
        {
            synchronized (this)
            {
               SBlowerDataPoint bdp = be.getBlowerDataPoint();

               arBlowerHistory.add( bdp );
            }

        }

       });
    }

    /* Only send updated Datapoint since the last time calling getUpdateDPs() */
    public ArrayList<SDataPoint> getUpdatedDPs()
    {
        ArrayList<SDataPoint> returnDPs = null;
        synchronized (this)
        {
            returnDPs = new ArrayList<SDataPoint>(arBlowerHistory);
            arBlowerHistory.clear();

        }
        for ( Entry<String, SDataPoint> entrySD : DataOrchestrator.getInstance().getData())
        {
            SDataPoint lastClientDP = hmLastChecked.get(entrySD.getKey());

             if ( lastClientDP == null || ! entrySD.getValue().compare(lastClientDP))
            {
                 if ( lastClientDP != null && lastClientDP instanceof SBlowerDataPoint )
                 {
                     continue;
                 }
                 else
                 {
                   returnDPs.add( entrySD.getValue());
                 }
                hmLastChecked.put( entrySD.getKey(), entrySD.getValue());
                continue;
            }

        }

      return returnDPs;

    }


}
