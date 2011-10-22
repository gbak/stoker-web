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
import sweb.shared.model.LogItem;
import sweb.shared.model.SDataPoint;
import sweb.shared.model.SDevice;
import sweb.shared.model.alerts.AlertBase;
import sweb.shared.model.logfile.LogDir;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("stoke")
public interface StokerCoreService extends RemoteService
{
    ArrayList<SDataPoint> getNewGraphDataPoints(String name) throws IllegalArgumentException;
    HashMap<String,SDevice> getConfiguration() throws IllegalArgumentException;
    Integer updateConfiguration( ArrayList<SDevice> asd) throws IllegalArgumentException;

    Long countDownServer() throws  IllegalArgumentException;

    String login(String user, String pass) throws IllegalArgumentException;
    Integer validateSession( String sessionID) throws IllegalArgumentException;
    void logout() throws IllegalArgumentException;

    void setupCallBack() throws IllegalArgumentException;
    ArrayList<ArrayList<SDataPoint>> getAllGraphDataPoints(String logName ) throws IllegalArgumentException;
    ArrayList<LogItem> getLogList() throws IllegalArgumentException;
    Integer startLog(String strCookerName, String strLogName, ArrayList<SDevice> arSD) throws IllegalArgumentException;
    Integer stopLog(String strCookerName, String strLogName) throws IllegalArgumentException;

    LogDir getLogFileNames() throws IllegalArgumentException;

    Integer attachToExistingLog( String cookerName, String selectedLog, String fileName ) throws IllegalArgumentException;

    void cometRequest(CallBackRequestType cometRequestType) throws IllegalArgumentException;
    
    void setAlertConfiguration( ArrayList<AlertBase> alertBaseList ) throws IllegalArgumentException;
}
