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

package com.gbak.sweb.server.monitors.stoker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;


import com.gbak.sweb.server.config.HardwareDeviceConfiguration;
import com.gbak.sweb.server.config.StokerWebConfiguration;
import com.gbak.sweb.server.config.stoker.StokerHardwareDevice;
import com.gbak.sweb.server.data.DataController;
import com.gbak.sweb.server.data.telnet.StokerTelnetController;
import com.gbak.sweb.server.events.ConfigChangeEvent;
import com.gbak.sweb.server.events.DataPointEvent;
import com.gbak.sweb.server.events.StateChangeEvent;
import com.gbak.sweb.server.events.StateChangeEvent.EventType;
import com.gbak.sweb.server.monitors.PitMonitor;
import com.gbak.sweb.shared.model.CookerList;
import com.gbak.sweb.shared.model.HardwareDeviceState;
import com.gbak.sweb.shared.model.HardwareDeviceState.Status;
import com.gbak.sweb.shared.model.data.SBlowerDataPoint;
import com.gbak.sweb.shared.model.data.SDataPoint;
import com.gbak.sweb.shared.model.devices.SDevice;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.org.apache.bcel.internal.generic.NEW;

@Singleton
public class StokerPitMonitor implements PitMonitor, DataController
{

    private static final Logger logger = Logger.getLogger(StokerPitMonitor.class.getName());
    
    // Data members
    private DataController m_DataController = null;
    
    private StokerHardwareDevice m_StokerHardware = null;
    private HardwareDeviceState m_HardwareDeviceState = new HardwareDeviceState(Status.UNKNOWN, Calendar.getInstance().getTime());
    
    private ConcurrentHashMap<String,SDataPoint> m_hmLatestData = new ConcurrentHashMap<String,SDataPoint>();

    private EventBus m_eventBus;
    
    @Inject
    public StokerPitMonitor(EventBus eventBus,
                            HardwareDeviceConfiguration stokerHardwareDevice,
                         //   StokerTelnetController stc,  // Guice problems created two instances, this is the working one
                            DataController stc,
                            StokerWebConfiguration swc )
    {
        this.m_eventBus = eventBus;
        this.m_StokerHardware = (StokerHardwareDevice)stokerHardwareDevice;
        this.m_DataController = stc;
        
        this.m_eventBus.register(this);
    
    }
    

    @Override
    public HardwareDeviceState getState()
    {
        return m_HardwareDeviceState;
    }

    @Override
    public boolean isActive()
    {
        return m_DataController.isReady();
    }

    /* (non-Javadoc)
     * @see com.gbak.sweb.server.monitors.PitMonitor#isConfigRequired()
     */
    @Override
    public boolean isConfigRequired()
    {
        // TODO Auto-generated method stub
        // Does the local configuration file exist?
        // Is there a cooker defined in the file?
        // Is there at least one probe - Pit or food assigned to the cooker
        // Is there a probe assigned to a cooker that no longer exists
        
        return false;
    }

    @Override
    public ArrayList<SDevice> getRawDevices()
    {
        return m_StokerHardware.getAllDevices();
    }

    @Override
    public CookerList getCookers()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateCooker(CookerList cookerList)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public SDataPoint getCurrentTemp( String deviceID )
    {
       return m_hmLatestData.get( deviceID );    
    }
    
    @Override
    public ArrayList<SDataPoint> getCurrentTemps()
    {
        ArrayList<SDataPoint> ar = null;

        // The Client should not be checking for data if the controller is down, but just in case.
        if ( isActive())
        {
           ar = new ArrayList<SDataPoint>(m_hmLatestData.values());
        }
        else
           ar = new ArrayList<SDataPoint>();

        return ar;
    }


    @Deprecated
    private void setHardwareState( StateChangeEvent.EventType eventType )
    {
        // EventType { NONE, LOST_CONNECTION, CONNECTION_ESTABLISHED, EXTENDED_CONNECTION_LOSS }
        switch ( eventType ) 
        {
            case  CONNECTION_ESTABLISHED:
                m_HardwareDeviceState.setHardwareStatus( HardwareDeviceState.Status.CONNECTED );
                break;
                
            case LOST_CONNECTION:
            case EXTENDED_CONNECTION_LOSS:
                m_HardwareDeviceState.setHardwareStatus( HardwareDeviceState.Status.DISCONNECTED );
                
            case NONE:
                m_HardwareDeviceState.setHardwareStatus( HardwareDeviceState.Status.UNKNOWN );
        
            default:
                m_HardwareDeviceState.setHardwareStatus( HardwareDeviceState.Status.UNKNOWN );
        }        
        
    }
    
    @Deprecated
    private void configChange( ConfigChangeEvent cce )
    {
        m_eventBus.post( cce );
        // fireConfigEvent(cce);
    }
    
    @Deprecated
    private void stateChange( StateChangeEvent change )
    {
        setHardwareState( change.getEventType() );
        m_eventBus.post( change );
       // fireChangeEvent( change );
    }
    
    @Subscribe
    public void addDataPoint( SDataPoint dp)
    {
        logger.trace("StokerPitMonitor:addDataPoint( SDataPoint )");

        if ( dp.getDeviceID() == null )
        {
            logger.warn("DeviceID is null");
            return;
        }
        SDataPoint dpFromMap =  m_hmLatestData.get(dp.getDeviceID());
        
        
        if ( dpFromMap != null)
        {
            boolean forceUpdate = false;
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, 1);
            if ( dpFromMap.getCollectedDate().after(cal.getTime()))
            {
               forceUpdate = true;
               logger.info("Forcing update");
            }

            
            boolean bChanged = false;
            if ( dpFromMap.compare(dp) == false || forceUpdate == true )
            {
                bChanged = true;
                // This adds the blower runtime to the BlowerDataPoint class.
                // It tracks the total runtime for the deviceID.  Time for specific
                // logs will either have to be calculated on the client or in the log file class.
                if ( dp instanceof SBlowerDataPoint )
                {
                    SBlowerDataPoint bdp = (SBlowerDataPoint) dp;
                    SBlowerDataPoint bdpFromMap = (SBlowerDataPoint) dpFromMap;
                    if ( bdp.isFanOn() == false )
                    {
                        Date last_d = bdpFromMap.getBlowerOnTime();
                        Date d = dp.getCollectedDate();
                        long lastSec = 0;
                        if ( last_d != null ) 
                        {
                           long elapsedSec = d.getTime() - last_d.getTime();
                           long totalRuntime = bdpFromMap.getTotalRuntime();
                           logger.debug("Total Runtime: " + totalRuntime );
                           bdpFromMap.setTotalRuntime(elapsedSec + totalRuntime );
                           logger.debug("Fan Off event, total runtime: " + bdpFromMap.getTotalRuntime() );
                        }
                    }
                    else
                    {
                         bdpFromMap.setBlowerOnTime(dp.getCollectedDate());
                    }
                
                }
            }
            
            dpFromMap.update( dp );
            if ( bChanged )
            {
                DataPointEvent be = new DataPointEvent(this, false, dpFromMap );
                m_eventBus.post( be );
                //fireTempEvent(be);
            }
            logger.trace("Debug: " + dpFromMap.getDebugString());
            
        }
        else
        {
           if ( dp instanceof SBlowerDataPoint )
           {
               // If blower is running, set the start time to now.  This needs to be done
               // in case Stoker-web is started while the fan is running, if it runs for a long
               // time without cycling, it will record no time.
               
               SBlowerDataPoint sdp = (SBlowerDataPoint) dp;
               if ( sdp.isFanOn() == true )
                  sdp.setBlowerOnTime(sdp.getCollectedDate());
           }
        
           m_hmLatestData.put(dp.getDeviceID(),dp);
        }
        
    }


    @Override
    public SDevice getDeviceByID(String ID)
    {
        return m_StokerHardware.getDevice(ID);
    }


    @Override
    public void updateSettings(ArrayList<SDevice> deviceList)
    {
        m_StokerHardware.update(deviceList);
        
    }

    @Subscribe
    public void setStatus(StateChangeEvent sce)
    {
        switch ( sce.getEventType() ) 
        {
            case  CONNECTION_ESTABLISHED:
                m_HardwareDeviceState.setHardwareStatus( HardwareDeviceState.Status.CONNECTED );
                break;
                
            case LOST_CONNECTION:
            case EXTENDED_CONNECTION_LOSS:
                m_HardwareDeviceState.setHardwareStatus( HardwareDeviceState.Status.DISCONNECTED );
                break;
            case NONE:
                m_HardwareDeviceState.setHardwareStatus( HardwareDeviceState.Status.UNKNOWN );
                break;
            default:
                m_HardwareDeviceState.setHardwareStatus( HardwareDeviceState.Status.UNKNOWN );
        }    
    }

    @Override
    public boolean start()
    {
        boolean ret = false;
        if ( m_StokerHardware.loadNow() )
        {
            m_eventBus.post( new ConfigChangeEvent( this, ConfigChangeEvent.EventType.CONFIG_INIT) );
            m_DataController.start();
            ret = true;
            // Data controller event should set the device state to true.
        }
        else
        {
            m_HardwareDeviceState.setHardwareStatus(Status.DISCONNECTED);
            
        }
        return ret;
    }


    @Override
    public void stop()
    {
        if (m_HardwareDeviceState.getHardwareStatus() == Status.CONNECTED )
        {
            m_DataController.stop();
        }
        
    }

    /**
     * This method accomplishes two tasks.  First it will return true or false if the current
     * value of the hardware is equal to the value passed in.  Second it will make sure that the
     * hardware status which is kept internal to the pitMonitor is correct.  The thrown events
     * should keep the values in line, but who knows. 
     * @param status
     * @return
     */
    private boolean verifyHardwareState( Status status )
    {
        // Perhaps writing the method this way will mask an event bus error?
        
        boolean connected = m_DataController.isConnected();
        
        if ( connected && m_HardwareDeviceState.isConnected() )
            return true;
        
        if ( connected && m_HardwareDeviceState.getHardwareStatus() == Status.DISCONNECTED )
        {
            logger.warn("Mismatch detected while checking the hardware state.  Setting state to connected" );
            m_HardwareDeviceState.setHardwareStatus(Status.CONNECTED);
            // TODO: do I submit an event here?
        }
        else if ( !connected && m_HardwareDeviceState.getHardwareStatus() == Status.CONNECTED )
        {
            logger.warn("Mismatch detected while checking the hardware state.  Setting state to disconnected" );
            m_HardwareDeviceState.setHardwareStatus(Status.DISCONNECTED );
        }
        
        return false;
    }

    @Override
    public boolean isReady()
    {
        return verifyHardwareState( Status.CONNECTED );
    }


    @Override
    public boolean isProcessing()
    {
       return m_DataController.isProcessing();
    }


    @Override
    public boolean isConnected()
    {
        return verifyHardwareState( Status.CONNECTED );
    }


    @Override
    public boolean testConnection()
    {
        return m_StokerHardware.loadNow();
    }

}
