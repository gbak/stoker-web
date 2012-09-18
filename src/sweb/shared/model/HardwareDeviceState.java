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

package sweb.shared.model;

import java.io.Serializable;
import java.util.Date;

public class HardwareDeviceState implements Serializable
{
    private static final long serialVersionUID = 2014172667830685173L;
    public static enum Status { UNKNOWN ,CONNECTED,DISCONNECTED  };
                                
    private Status m_HardwareStatus;
    private Date m_Time;
    
    public HardwareDeviceState( Status s, Date d )
    {
        m_HardwareStatus = s;
        m_Time = d;
    }

    private HardwareDeviceState() { }

    public Status getHardwareStatus()
    {
        return m_HardwareStatus;
    }

    public void setHardwareStatus( Status status )
    {
        // if the state is changed and the time value is not, that can cause problems
        // in determining how long it's been in this state, so it's best to probably set the time
        // if the state is changing.
        
        if ( m_HardwareStatus != status )
        {
           m_HardwareStatus = status;
          // m_Time = Calendar.getInstance().getTime();
        }
        else
        {
            // Idiots...
            
        }
    }

    public Date getDate()
    {
        return m_Time;
    }

    public void setDate( Date d )
    {
        m_Time = d;
    }
    
    public boolean isConnected()
    {
        return m_HardwareStatus == Status.CONNECTED;
    }
}

