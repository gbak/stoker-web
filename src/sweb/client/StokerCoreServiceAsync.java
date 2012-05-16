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
import sweb.shared.model.Cooker;
import sweb.shared.model.CookerList;
import sweb.shared.model.LogItem;
import sweb.shared.model.alerts.AlertModel;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.logfile.LogDir;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface StokerCoreServiceAsync
{
    void getNewGraphDataPoints(String input, AsyncCallback<ArrayList<SDataPoint>> callback)
            throws IllegalArgumentException;

    void getDeviceConfiguration(AsyncCallback<HashMap<String,SDevice>> callback)
            throws IllegalArgumentException;

    void getStokerWebConfiguration(AsyncCallback<CookerList> callback )
            throws IllegalArgumentException;
    
    void updateSettings( ArrayList<SDevice> asd, AsyncCallback<Integer> callback)
           throws IllegalArgumentException;
    
    void updateStokerWebConfig( CookerList cookerList, AsyncCallback<Integer> callback )
            throws IllegalArgumentException;

    void countDownServer(AsyncCallback<Long> asyncCallback);

    void login( String user, String pass,  AsyncCallback<String> callback);
    void validateSession( String sessionID, AsyncCallback<Integer> callback);
    void logout( AsyncCallback<Void> callback);

    void setupCallBack(AsyncCallback<Void> asyncCallback);

    void getAllGraphDataPoints( String logName, AsyncCallback<ArrayList<ArrayList<SDataPoint>>> callback)
            throws IllegalArgumentException;

    void getLogList(AsyncCallback<ArrayList<LogItem>> asyncCallback ) throws IllegalArgumentException;

    void startLog( String strCookerName, String strLogName, ArrayList<SDevice> arSD, AsyncCallback<Integer> callback) throws IllegalArgumentException;
    void stopLog( String strCookerName, String strLogName, AsyncCallback<Integer> callback) throws IllegalArgumentException;

    void getLogFileNames(AsyncCallback<LogDir> asyncCallback ) throws IllegalArgumentException;
    void attachToExistingLog( String cookerName, String selectedLog, String fileName, AsyncCallback<Integer> asyncCallback ) throws IllegalArgumentException;


    void cometRequest( CallBackRequestType cometRT, AsyncCallback<Void> callback) throws IllegalArgumentException;

    void getAlertConfiguration( AsyncCallback<ArrayList<AlertModel>> callback) throws IllegalArgumentException;
    void setAlertConfiguration( ArrayList<AlertModel> alertBaseList, AsyncCallback<Void> callback ) throws IllegalArgumentException;
    void addNoteToLog( String note, ArrayList<String> logList, AsyncCallback<Integer> callback ) throws IllegalArgumentException;
    
    void getClientProperties( AsyncCallback<HashMap<String,String>> properties) throws IllegalArgumentException;
}
