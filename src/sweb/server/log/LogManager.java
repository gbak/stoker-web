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

package sweb.server.log;

import java.util.ArrayList;

import sweb.server.log.exceptions.LogExistsException;
import sweb.server.log.exceptions.LogNotFoundException;
import sweb.shared.model.LogItem;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.logfile.LogNote;

public interface LogManager
{
   
    /**
     * Return all the data points for the given log name
     * @param logName name of log to retrieve data points for
     * @return
     */
    public ArrayList<ArrayList<SDataPoint>> getAllDataPoints(String logName );

    public ArrayList<SDevice> getConfigSettings( String logName );
   
    /**
     * Returns all the logs currently running on the server
     * @return ArrayList of LogItem 
     */
    public ArrayList<LogItem> getLogList();
   
    public String getLogFilePath(String strLogName );
    
    public String getLogFileName(String strLogName );
    
    public boolean isLogRunning( String strLogName );

    public void startLog(String strCookerName, String strLogName) throws LogExistsException;

    public void startLog( LogItem logItem ) throws LogExistsException;

    public String stopLog( String strLogName ) throws LogNotFoundException;

    public void stopAllLogs();

    public Integer attachToExistingLog( String cookerName, String selectedLog, String fileName );
    
    public ArrayList<LogNote> getNotes(String logName);
    
    public void addNoteToLog( String note, ArrayList<String> logList );
    
}
