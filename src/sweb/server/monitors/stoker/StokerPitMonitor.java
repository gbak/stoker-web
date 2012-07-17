package sweb.server.monitors.stoker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import sweb.server.controller.HardwareDeviceConfiguration;
import sweb.server.controller.config.stoker.StokerHardwareDevice;
import sweb.server.controller.data.DataController;
import sweb.server.controller.data.telnet.StokerTelnetController;
import sweb.server.controller.events.BlowerEvent;
import sweb.server.controller.events.BlowerEventListener;
import sweb.server.controller.events.ConfigChangeEvent;
import sweb.server.controller.events.ConfigChangeEventListener;
import sweb.server.controller.events.DataPointEvent;
import sweb.server.controller.events.DataPointEventListener;
import sweb.server.controller.events.StateChangeEvent;
import sweb.server.controller.events.StateChangeEventListener;
import sweb.server.log.LogManagerImpl;
import sweb.server.monitors.PitMonitor;
import sweb.shared.model.CookerList;
import sweb.shared.model.HardwareDeviceStatus;
import sweb.shared.model.HardwareDeviceStatus.Status;
import sweb.shared.model.data.SBlowerDataPoint;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.devices.SDevice;

public class StokerPitMonitor implements PitMonitor
{

    private static final Logger logger = Logger.getLogger(StokerPitMonitor.class.getName());
    
    // Data members
    private DataController m_DataController = null;
 //   private HardwareDeviceConfiguration m_HardwareDeviceConfig = null;
    private CookerList m_CookerList = null;
    
    StokerHardwareDevice m_StokerHardware = null;
    HardwareDeviceStatus m_HardwareDeviceStatus = null;
    
    ConcurrentHashMap<String,SDataPoint> hmLatestData = new ConcurrentHashMap<String,SDataPoint>();

    
    // Listener Lists
    private ArrayList<StateChangeEventListener> stateChangeListenList = new ArrayList<StateChangeEventListener>();
    private ArrayList<BlowerEventListener> blowerEventListenList = new ArrayList<BlowerEventListener>();
    private Set<DataPointEventListener> dataEventListenList = Collections.newSetFromMap(new ConcurrentHashMap<DataPointEventListener,Boolean>());
    
    public interface Data
    {
        void addData( SDataPoint sdp );
    }
    
    public interface State
    {
        void stateChange( StateChangeEvent change);
    }
    
    public interface Config
    {
        void configChange( ConfigChangeEvent event );
    }
    
    public StokerPitMonitor()
    {
    
        Config c = new Config() {@Override public void configChange(ConfigChangeEvent cce) { configChange(cce);  } };
        
        m_StokerHardware = new StokerHardwareDevice( c );
        boolean bLoadStatus = m_StokerHardware.loadNow();
        
        if ( bLoadStatus == false )
        {
            // TODO: unable to pull config from stoker.  Set disconnected and retry later
            m_HardwareDeviceStatus.setHardwareStatus(Status.DISCONNECTED);
            return;
        }
        Data d = new Data() {@Override public void addData(SDataPoint sdp) { addDataPoint(sdp);  } };
        State s = new State() {@Override public void stateChange(StateChangeEvent change) { stateChange(change);  } };
        
        // Attempt to pull stoker config, if that succeeds connect telnet
        m_DataController= new StokerTelnetController(d, s);
    }
    

    @Override
    public HardwareDeviceStatus getState()
    {
        return m_HardwareDeviceStatus;
    }

    @Override
    public boolean isActive()
    {
        return m_DataController.isReady();
    }

    /* (non-Javadoc)
     * @see sweb.server.monitors.PitMonitor#isConfigRequired()
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
    public ArrayList<SDataPoint> getCurrentTemps()
    {
        ArrayList<SDataPoint> ar = null;

        // The Client should not be checking for data if the controller is down, but just in case.
        if ( isActive())
        {
           ar = new ArrayList<SDataPoint>(hmLatestData.values());
        }
        else
           ar = new ArrayList<SDataPoint>();

        return ar;
    }

    @Override
    public void addTempListener(DataPointEventListener dataListener)
    {
        synchronized ( this )
        {
            dataEventListenList.add( dataListener );
        }

    }

    @Override
    public void removeTempListener(DataPointEventListener dataListener)
    {
        synchronized ( this )
        {
           for ( DataPointEventListener d : dataEventListenList )
           {
               if ( d == dataListener )
               {
                   dataEventListenList.remove(d);
               }
           }
        }
    }

    @Override
    public void fireTempEvent(DataPointEvent dataEvent)
    {
        Object[] copy;
        synchronized ( this )
        {
           copy = dataEventListenList.toArray();  
        }
            //for ( DataPointEventListener listener : m_dpListener )
            for ( int i = 0; i < copy.length; ++i )
            {
                // Store the desired listener type ALL, UPDATED, TIMED
                // in the listener object,
    
                //listener.stateChange(dpe);
               ((DataPointEventListener)copy[i]).stateChange(dataEvent);
            }

    }

    @Override
    public void addConfigChangeListener(ConfigChangeEventListener configListener)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeConfigChangeListener(
            ConfigChangeEventListener configListener)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void fireConfigEvent(ConfigChangeEvent configEvent)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addStateChangeListener(
            StateChangeEventListener stateChangeListener)
    {
        synchronized( this )
        {
            stateChangeListenList.add( stateChangeListener );
        }

    }

    @Override
    public void removeStateChangeListener(
            StateChangeEventListener stateChangeListener)
    {
        synchronized( this )
        {
            stateChangeListenList.remove( stateChangeListener );
        }


    }

    @Override
    public void fireChangeEvent(StateChangeEvent changeEvent)
    {
        synchronized( this )
        {
            for ( StateChangeEventListener listener : stateChangeListenList )
            {
                listener.actionPerformed(changeEvent);
            }
        }

    }

    @Override
    public void addBlowerChangeListener(BlowerEventListener blowerListener)
    {
        synchronized ( this )
        {
            blowerEventListenList.add( blowerListener );
        }
        
    }

    @Override
    public void removeBlowerChangeListener(BlowerEventListener blowerListener)
    {
        synchronized ( this )
        {
            blowerEventListenList.remove( blowerListener );
        }
        
    }

    @Override
    public void fireBlowerEvent(BlowerEvent blowerEvent)
    {
        Object[] copy;
        // Make a copy of the array list so the subscribers do not hold up the synchronized block
        synchronized ( this )
        {
            copy = blowerEventListenList.toArray();
        }
        
            //for ( BlowerEventListener listener : m_arListener )
            for ( int i = 0; i < copy.length; ++i )
            {
                ((BlowerEventListener)copy[i]).stateChange(blowerEvent);
            }
        
        
    }

    private void setHardwareState( StateChangeEvent.EventType eventType )
    {
        // EventType { NONE, LOST_CONNECTION, CONNECTION_ESTABLISHED, EXTENDED_CONNECTION_LOSS }
        switch ( eventType ) 
        {
            case  CONNECTION_ESTABLISHED:
                m_HardwareDeviceStatus.setHardwareStatus( HardwareDeviceStatus.Status.CONNECTED );
                break;
                
            case LOST_CONNECTION:
            case EXTENDED_CONNECTION_LOSS:
                m_HardwareDeviceStatus.setHardwareStatus( HardwareDeviceStatus.Status.DISCONNECTED );
                
            case NONE:
                m_HardwareDeviceStatus.setHardwareStatus( HardwareDeviceStatus.Status.UNKNOWN );
        
            default:
                m_HardwareDeviceStatus.setHardwareStatus( HardwareDeviceStatus.Status.UNKNOWN );
        }        
        
    }
    
    private void configChange( ConfigChangeEvent cce )
    {
        fireConfigEvent(cce);
    }
    
    private void stateChange( StateChangeEvent change )
    {
        setHardwareState( change.getEventType() );
        fireChangeEvent( change );
    }
    
    private void addDataPoint( SDataPoint dp)
    {

        if ( dp.getDeviceID() == null )
        {
            logger.warn("DeviceID is null");
            return;
        }
        SDataPoint dpFromMap =  hmLatestData.get(dp.getDeviceID());
        
        
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
                fireTempEvent(be);
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
        
           hmLatestData.put(dp.getDeviceID(),dp);
        }
        
    }


    @Override
    public SDevice getDeviceByID(String ID)
    {
        return m_CookerList.getDeviceByID(ID);
    }


    @Override
    public void updateSettings(ArrayList<SDevice> deviceList)
    {
        StokerHardwareDevice.postUpdate(deviceList);
        
    }


}
