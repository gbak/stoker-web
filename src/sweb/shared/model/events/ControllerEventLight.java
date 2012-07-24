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

package sweb.shared.model.events;

import java.io.Serializable;


public class ControllerEventLight implements Serializable
{
    public enum EventTypeLight { NONE, CONFIG_UPDATE, CONFIG_UPDATE_REFRESH, LOST_CONNECTION, CONNECTION_ESTABLISHED }

    private EventTypeLight m_EventTypeLight = EventTypeLight.NONE;

    private static final long serialVersionUID = 1L;

    private ControllerEventLight()
    {

    }

    public ControllerEventLight( EventTypeLight et)
    {
        m_EventTypeLight = et;
    }

    public EventTypeLight getEventType()
    {
        return m_EventTypeLight;
    }

}
