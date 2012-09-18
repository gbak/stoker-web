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

package sweb.client;

import java.util.ArrayList;
import java.util.HashMap;

import sweb.shared.model.CallBackRequestType;
import sweb.shared.model.ConfigurationSettings;
import sweb.shared.model.CookerList;
import sweb.shared.model.LogItem;
import sweb.shared.model.alerts.AlertModel;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.logfile.LogDir;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("stoke")
public interface StokerCoreService extends RemoteService
{
    ArrayList<SDataPoint> getNewGraphDataPoints(String name) throws IllegalArgumentException;
    
    /**
     * Returns both configured CookerList from server and all available 
     * raw devices from hardware.  CookerList may contain no cookers if there is no cookers
     * defined in the local configuration file.  
     * 
     * @return ConfiurationSettings - CookerList with already defined cookers and a list of all
     * available SDevices.
     * @throws IllegalArgumentException
     */
    ConfigurationSettings getDeviceConfiguration() throws IllegalArgumentException;
    
    CookerList getStokerWebConfiguration() throws IllegalArgumentException;
    
    /**
     * Updates only Temp, Alarm and probe name settings within a cooker.  These settings
     * will be saved to the stoker.  If local alarms are set in the stokerweb.properties
     * file the Alarm activation settings will be cleared prior to saving.
     * @param deviceList List of devices to be updated.
     * @return 1 on success<br>0 on failure
     * @throws IllegalArgumentException
     */
    Integer updateTempAndAlarmSettings( ArrayList<SDevice> deviceList) throws IllegalArgumentException;

    /**
     * Updates the cooker configuration settings built with the configuration dialog 
     * screen.  This will update cooker and all settings within each attached SDevice.
     * A browser refresh should be forced after this call since it could drastically 
     * change the current settings.<br>
     * Cooker name changes will cause any running logs under the old cooker name to
     * be removed.  A new default log will start under the new cooker name.
     * @return
     * @param cookerList Object which contains all cooker and probe information.  
     * @return
     * @throws IllegalArgumentException
     */
    Integer updateStokerWebConfig( CookerList cookerList ) throws IllegalArgumentException;
    
    Long countDownServer() throws  IllegalArgumentException;

    String login(String user, String pass) throws IllegalArgumentException;
    /**
     * Check to see if session ID is valid on the server
     * @param sessionID 
     * @return 1 if the session is valid<br>0 if not valid
     * @throws IllegalArgumentException
     */
    Integer validateSession( String sessionID) throws IllegalArgumentException;
    void logout() throws IllegalArgumentException;

    void setupCallBack() throws IllegalArgumentException;
    ArrayList<ArrayList<SDataPoint>> getAllGraphDataPoints(String logName ) throws IllegalArgumentException;
    ArrayList<LogItem> getLogList() throws IllegalArgumentException;
    Integer startLog(String strCookerName, String strLogName, ArrayList<SDevice> arSD) throws IllegalArgumentException;
    String stopLog(String strCookerName, String strLogName) throws IllegalArgumentException;

    LogDir getLogFileNames() throws IllegalArgumentException;

    /**
     * Attaches cooker to an existing log file.  This can be used if there is an existing log that 
     * was interrupted by a power outage or some other event where the stoker-web server was
     * restarted.
     * 
     * @param cookerName Name of cooker that will be attached to the new log
     * @param selectedLog Name of log to attach to
     * @param fileName File name of existing log
     * @return 1 if successful<br>0 otherwise
     * @throws IllegalArgumentException
     */
    Integer attachToExistingLog( String cookerName, String selectedLog, String fileName ) throws IllegalArgumentException;

    /**
     * Request where the response will be returned over the comet stream.
     * @param cometRequestType
     * @throws IllegalArgumentException
     */
    void cometRequest(CallBackRequestType cometRequestType) throws IllegalArgumentException;
    
    ArrayList<AlertModel> getAlertConfiguration() throws IllegalArgumentException;
    void setAlertConfiguration( ArrayList<AlertModel> alertBaseList ) throws IllegalArgumentException;
    Integer addNoteToLog( String note, ArrayList<String> logList ) throws IllegalArgumentException;
    
    /**
     * Gets client property list from server.  These properties are specific to operation
     * on the client (browser).  These properties are stored in the stokerweb.properties file
     * in the STOKERWEB_DIR on the server.
     * @return Hashmap of String key / value pairs.
     * @throws IllegalArgumentException
     */
    HashMap<String,String> getClientProperties() throws IllegalArgumentException;
}
