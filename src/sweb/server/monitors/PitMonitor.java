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

package sweb.server.monitors;

import java.util.ArrayList;

import sweb.shared.model.CookerList;
import sweb.shared.model.HardwareDeviceState;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.devices.SDevice;


public interface PitMonitor
{

    public boolean testConnection();
    
    public boolean start();
    
    public void stop();
    /**
     * Check the state of the hardware device
     * @return Current state of the device
     */
    public HardwareDeviceState getState();
    
    /**
     * Checks to see if hardware device is active
     * @return true if device is active
     */
    public boolean isActive();
    
    /**
     * Query PitMontitor to see if the Cooker setup has been completed.
     * This says if the local configuration is required.
     * @return true if configuration is required
     */
    public boolean isConfigRequired();
    
    
    /**
     * Gets all devices from pitMonitor.
     * @return List of raw devices
     */
    public ArrayList<SDevice> getRawDevices();
    
    public CookerList getCookers();
    
    public void updateCooker( CookerList cookerList );   
    
    public void updateSettings( ArrayList<SDevice> deviceList );
    
    public ArrayList<SDataPoint> getCurrentTemps();
    
    public SDevice getDeviceByID( String ID );
}
