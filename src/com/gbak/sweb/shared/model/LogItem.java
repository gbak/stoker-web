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

package com.gbak.sweb.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import com.gbak.sweb.shared.model.devices.SDevice;



public class LogItem implements Serializable
{

    public Date getStartDate()
    {
        return m_startDate;
    }

    public void setStartTime(Date startDate)
    {
        this.m_startDate = startDate;
    }

    private static final long serialVersionUID = -5911027953140218913L;

    String m_strLogName;
    String m_strCookerName;
    Date   m_startDate;
    ArrayList<SDevice> m_asd = null;
    
    public String toString()
    {
        return m_strLogName;
    }

    public LogItem() { }

    public LogItem( String CookerName, String logName, Date startTime)
    {
        setCookerName(CookerName);
        m_strLogName = logName;
        m_startDate = startTime;
    }

    public LogItem( String CookerName,  String LogName, Date startTime, ArrayList<SDevice> deviceList )
    {
        this( CookerName, LogName, startTime );
        m_asd = deviceList;
        
    }

    public void setCookerName( String name )
    {
        m_strCookerName = name;
    }
    
    public String getLogName()
    {
        return m_strLogName;
    }

    public String getCookerName()
    {
        return m_strCookerName;
    }

    public ArrayList<SDevice> getLogItems()
    {
        return m_asd;
    }


}
