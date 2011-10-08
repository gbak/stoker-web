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

package sweb.server.controller.events;

import java.util.ArrayList;
import java.util.EventObject;

import sweb.shared.model.SBlowerDataPoint;
import sweb.shared.model.SDataPoint;
import sweb.shared.model.SProbeDataPoint;

public class DataPointEvent extends EventObject
{
    private static final long serialVersionUID = 1406200984218400564L;

    ArrayList<SProbeDataPoint> m_arPDP = null;
    SBlowerDataPoint m_mdp = null;
    boolean bTimedEvent = false;

    public DataPointEvent(Object source, boolean bTimedEvent)
    {
        super(source);
        this.bTimedEvent = bTimedEvent;
    }

    public DataPointEvent(Object source, boolean bTimedEvent, SDataPoint sdp)
    {
        this(source, bTimedEvent );
        if ( m_arPDP == null)
            m_arPDP = new ArrayList<SProbeDataPoint>();

        addDataPoint( sdp );
    }

    public boolean isTimedEvent()
    {
        return bTimedEvent;
    }

    public void setTimedEvent(boolean b )
    {
        bTimedEvent = b;
    }
    /*
    public DataPointEventType getEventType()
    {
        return ((m_sdp instanceof SProbeDataPoint) ? DataPointEventType.PROBE : DataPointEventType.BLOWER);
    }
    */
    public void addDataPoint(SDataPoint sdp)
    {
        sdp.setTimedEvent( bTimedEvent );
        if ( sdp instanceof SBlowerDataPoint)
            m_mdp = (SBlowerDataPoint)sdp;
        else
        {
            if ( m_arPDP == null)
                m_arPDP = new ArrayList<SProbeDataPoint>();

           m_arPDP.add( (SProbeDataPoint)sdp );
        }
    }

    public ArrayList<SProbeDataPoint> getSProbeDataPoints()
    {
        return m_arPDP;
    }

    public SBlowerDataPoint getSBlowerDataPoint()
    {
        return m_mdp;
    }

}
