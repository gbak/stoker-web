/**
 *  Stoker-web
 *
 *  Copyright (C) 2012  Gary Bak
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

package com.gbak.sweb.shared.model.events;

import java.io.Serializable;


public class LogEvent implements Serializable
{
   public enum LogEventType { NONE, NEW, UPDATED, DELETED }

   private LogEventType m_LogType = LogEventType.NONE;
   private String m_CookerName;
   private String m_LogName;

   private static final long serialVersionUID = 1L;

   private LogEvent()
   {

   }

   public LogEvent( LogEventType et, String cookerName, String logName )
   {
      m_LogType = et;
      m_CookerName = cookerName;
      m_LogName = logName;
   }

   public LogEventType getEventType()
   {
       return m_LogType;
   }
   
   public void setCookerName( String cookerName )
   {
      m_CookerName = cookerName;
   }
   
   public void setLogName( String logName )
   {
      m_LogName = logName;
   }
   
   public String getCookerName()
   {
      return m_CookerName;
   }
   
   public String getLogName()
   {
      return m_LogName;
   }
}
