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

package sweb.client.graph;

import java.util.ArrayList;
import java.util.HashMap;

import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.devices.SDevice;

import com.google.gwt.user.client.ui.Composite;




public abstract class StokerLineGraph extends Composite
{

    //protected HashMap<String,SDevice> mapDeviceList = new HashMap<String,SDevice>();
    ArrayList<SDevice> listDeviceList = new ArrayList<SDevice>();
    private HashMap<String,Integer> mapDeviceIndex = new HashMap<String,Integer>();
    String fanID = new String();

    public StokerLineGraph()
    {

    }

    public StokerLineGraph(int iWidth, int iHeight, ArrayList<SDevice> listDeviceList)
    {
        this.listDeviceList = listDeviceList;

    }

    /**
     * @param sdp
     * @param refresh
     */
    public abstract void addData( SDataPoint sdp, boolean refresh );


    /** Adds multiple data points to the graph that are marked as TimedEvent.
     * Refresh parameter allows all the data points in the array to be considered
     * and not just ones that are marked TimedEvent.  Usually only
     * points marked as timed will be added to the graph.
     * @param arSDP Array of data points
     * @param refresh flag to add data to the graph regardless of the
     *        TimedEvent flag in the data point.  This allows the graph to be refreshed
     *        with every data point set.  This is used on a browser refresh, or when switching
     *        log files
     */
    public abstract void addData( ArrayList<SDataPoint> arSDP );

    public abstract void redraw();

}
