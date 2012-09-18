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

package sweb.server;

public class StokerWebConstants
{

    // Environment Variables
    public static final String ENV_STOKERWEB_DIR = "STOKERWEB_DIR";
    
    // Properties
    public static final String PROPS_STOKER_IP_ADDRESS = "stoker_ip";
    public static final String PROPS_STOKER_PORT       = "stoker_port";
    public static final String PROPS_STOKER_LOGIN =      "stoker_login";
    public static final String PROPS_STOKER_PASSWORD =   "stoker_pass";
    
    public static final String PROPS_LOGS_DIR =    "stokerweb_logs_dir";
    public static final String PROPS_LOG_FILE_PERIOD = "stokerweb_log_file_period";
    public static final String PROPS_STOKERWEB_DIR   = "stokerweb_dir";
    public static final String PROPS_ALARM_SETTINGS_LOCATION = "alarmSettings_location";
    
    public static final String PROPS_WEATHER_ZIPCODE =          "weather_zipcode";
    public static final String PROPS_WEATHER_WOEID_URL =        "weather_woeid_url";
    public static final String PROPS_WEATHER_GET_BY_WOEID_URL = "weather_get_by_woeid_url";

    public static final String PROPS_TIMEOUT_TO_EXTENDED_LOSS = "timeout_to_extended_disconnect";
    public static final String PROPS_TIMEOUT_TO_RECONNECT = "timeout_to_reconnect";
    public static final String TIMEOUT_TO_EXTENDED_LOSS = "30";
    public static final String TIMEOUT_TO_RECONNECT = "15";
    
    // Files
    public static final String FILE_STOKERWEB_PROPERTIES = "stokerweb.properties";
    public static final String FILE_LOGIN_PROPERTIES     = "login.properties";
    
    // Directories
    public static final String PATH_COOK_LOG = "cookLogs";
    
    // Stoker Strings
    public static final String STOKER_PROMPT_LOGIN    =  "login:";
    public static final String STOKER_PROMPT_PASSWORD =  "password:";
    public static final String STOKER_CONDITION_START =  "stoker: start";
    public static final String STOKER_CONDITION_STOP  =  "stkcmd: stop";
    
}
