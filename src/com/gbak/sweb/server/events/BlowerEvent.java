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

package com.gbak.sweb.server.events;

import java.util.EventObject;

import com.gbak.sweb.shared.model.data.SBlowerDataPoint;


public class BlowerEvent extends EventObject
{
    private static final long serialVersionUID = 453604118970890691L;

    public enum BlowerEventType { OFF, ON }

    private BlowerEventType m_BlowerEventType = BlowerEventType.OFF;
    private SBlowerDataPoint m_BDP = null;


    public BlowerEvent(Object source, SBlowerDataPoint bdp)
    {
        super(source);
        if ( bdp.isFanOn() )
           m_BlowerEventType = BlowerEventType.ON;
        else
           m_BlowerEventType = BlowerEventType.OFF;

        m_BDP = bdp;
    }

    public BlowerEventType getEventType()
    {
        return m_BlowerEventType;
    }

    public SBlowerDataPoint getBlowerDataPoint()
    {
        return m_BDP;
    }
}

