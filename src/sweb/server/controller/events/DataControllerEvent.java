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

import java.util.EventObject;

public class DataControllerEvent extends EventObject
{

    public enum EventType { NONE, LOST_CONNECTION, CONNECTION_ESTABLISHED, EXTENDED_CONNECTION_LOSS }

    private EventType m_EventType = EventType.NONE;

    private static final long serialVersionUID = 1L;

    public DataControllerEvent(Object source, EventType et)
    {
        super(source);
        m_EventType = et;
    }

    public EventType getEventType()
    {
        return m_EventType;  // TODO: implement this
    }

}
