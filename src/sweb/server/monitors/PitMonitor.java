package sweb.server.monitors;

import java.util.ArrayList;

import sweb.server.controller.events.BlowerEvent;
import sweb.server.controller.events.BlowerEventListener;
import sweb.server.controller.events.ConfigChangeEvent;
import sweb.server.controller.events.ConfigChangeEventListener;
import sweb.server.controller.events.DataPointEvent;
import sweb.server.controller.events.DataPointEventListener;
import sweb.server.controller.events.StateChangeEvent;
import sweb.server.controller.events.StateChangeEventListener;
import sweb.shared.model.CookerList;
import sweb.shared.model.HardwareDeviceStatus;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.devices.SDevice;


public interface PitMonitor
{

    /**
     * Check the state of the hardware device
     * @return Current state of the device
     */
    public HardwareDeviceStatus getState();
    
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
    
    public void addTempListener( DataPointEventListener dataListener );
    
    public void removeTempListener( DataPointEventListener dataListener );
    
    public abstract void fireTempEvent( DataPointEvent dataEvent );
    
    public void addConfigChangeListener( ConfigChangeEventListener configListener );
    
    public void removeConfigChangeListener( ConfigChangeEventListener configListener );
    
    public void fireConfigEvent( ConfigChangeEvent configEvent );
    
    public void addStateChangeListener( StateChangeEventListener stateChangeListener );
    
    public void removeStateChangeListener( StateChangeEventListener stateChangeListener );
    
    public void fireChangeEvent( StateChangeEvent changeEvent );
    
    public void addBlowerChangeListener( BlowerEventListener blowerListener );
    
    public void removeBlowerChangeListener( BlowerEventListener blowerListener );
    
    public void fireBlowerEvent( BlowerEvent blowerEvent );
    
    public SDevice getDeviceByID( String ID );
}
