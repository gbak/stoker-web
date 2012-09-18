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

package sweb.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.zschech.gwt.comet.client.CometClient;
import net.zschech.gwt.comet.client.CometListener;
import net.zschech.gwt.comet.client.CometSerializer;
import net.zschech.gwt.comet.client.SerialTypes;
import sweb.client.dialog.AlertDialog;
import sweb.client.dialog.GeneralMessageDialog;
import sweb.client.dialog.LogFileChooser;
import sweb.client.dialog.LoginDialog;
import sweb.client.dialog.handlers.AlertDialogHandler;
import sweb.client.dialog.handlers.LogFileChooserHandler;
import sweb.client.dialog.handlers.LoginDialogHandler;
import sweb.client.gauge.ProbeComponent.Alignment;
import sweb.client.weather.WeatherComponent;
import sweb.client.widgets.Configuration;
import sweb.client.widgets.handlers.ConfigUpdateHandler;
import sweb.shared.model.CallBackRequestType;
import sweb.shared.model.CallBackRequestType.RequestType;
import sweb.shared.model.ConfigurationSettings;
import sweb.shared.model.Cooker;
import sweb.shared.model.CookerHelper;
import sweb.shared.model.CookerList;
import sweb.shared.model.HardwareDeviceState;
import sweb.shared.model.HardwareDeviceState.Status;
import sweb.shared.model.alerts.AlertModel;
import sweb.shared.model.alerts.BrowserAlarmModel;
import sweb.shared.model.data.SBlowerDataPoint;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.data.SProbeDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.events.ControllerEventLight;
import sweb.shared.model.events.ControllerEventLight.EventTypeLight;
import sweb.shared.model.events.LogEvent;
import sweb.shared.model.logfile.LogDir;
import sweb.shared.model.weather.WeatherData;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MainPage
{

    private static final String SERVER_ERROR = "An error occurred while "
            + "attempting to contact the server. Please check your network "
            + "connection and try again.";


    private final StokerCoreServiceAsync m_stokerService = GWT.create(StokerCoreService.class);
    private final String m_cookieID = "sid";

    private static enum LoadedPage { NONE, CONNECTED_PAGE, NOT_CONNECTED_PAGE };
    
    LoadedPage m_currentPage = LoadedPage.NONE;

    ArrayList<CookerComponent> m_cookerComponentList = null;
    WeatherData m_weatherData = new WeatherData();
    WeatherComponent m_weatherComponent = null;

    EventTypeLight m_eventState = EventTypeLight.NONE;

    DockPanel m_outerDocPanel = new DockPanel(); // Outermost panel
    HorizontalPanel m_HeaderHP = new HorizontalPanel();
    VerticalPanel m_cookerVP = null; // For Multiple cookers
    VerticalPanel m_cookerVPDummy = new VerticalPanel();
    SimplePanel m_cookerOuterPanel = new SimplePanel();
    
    
    Button m_loginButton = new Button();
    Button m_updateButton = new Button();
    Button m_reportsButton = new Button();
    Button m_configButton = new Button();
    Button m_statusFauxButton = new Button();
    Button m_testButton = new Button();  // Test various functions.

    boolean m_requiresUpdate = true;  // TODO: hard coding this to so the button stays enabled.

    private boolean m_bConnected = false;
    
    private String m_httpSessionID = "";
    
    private CookerList m_cookerList = null;
    
    HashMap<String,String> m_properties = null;


    @SerialTypes(
    { SDataPoint.class, SProbeDataPoint.class, SBlowerDataPoint.class, ControllerEventLight.class, WeatherData.class, CallBackRequestType.class,
        HardwareDeviceState.class, LogEvent.class, AlertModel.class, BrowserAlarmModel.class, CookerList.class, Cooker.class, CookerHelper.class })

    public static abstract class StokerCometSerializer extends CometSerializer {
    }

    public MainPage()
    {
        initCallBack();
        initStokerPage(null);
        makeCallBackRequest( new CallBackRequestType( RequestType.FORCE_DATA_PUSH ));
    }

    /** Wrapper for cometRequest async server call.  This is a convenience method to server
     * calls without having to deal with the async response.  Comet calls only return
     * data to the comet stream.
     *
     * @param crt The enumerated request type
     */
    void makeCallBackRequest( CallBackRequestType crt )
    {

        m_stokerService.cometRequest( crt, new AsyncCallback<Void>() {

            public void onFailure(Throwable caught)
            {
                Log.error("Failure calling makeCometRequest()");
            }

            public void onSuccess(Void result)
            {
                Log.debug("makeCallBackRequest cometRequest success");
            }

        });
    }


    private void initNotConnectedPage(Date d)
    {
        Log.debug("initNotConnectedPage");
        if ( m_currentPage != LoadedPage.NOT_CONNECTED_PAGE )
        {
            Log.info("Stoker not connected, setting up not connected page");
           RootPanel.get().clear();

           DockPanel dpDisconnected = new DockPanel();
           HTML ht = new HTML("Stoker not connected!");
           dpDisconnected.add( ht, DockPanel.NORTH);

           m_currentPage = LoadedPage.NOT_CONNECTED_PAGE;
           RootPanel.get().add( dpDisconnected );
        }
    }

    private void presentConfigScreen()
    {
        m_stokerService.getDeviceConfiguration(new AsyncCallback<ConfigurationSettings>() 
         {

            public void onFailure(Throwable caught)
            {
                caught.printStackTrace();
                System.out
                        .println("Client configuration failure");
            }

            public void onSuccess( ConfigurationSettings result)
            {
                Log.info("Opening configuration window");
 
                Configuration ccfg = new sweb.client.widgets.Configuration(result) ;
                
                ArrayList<Cooker> cookerList = new ArrayList<Cooker>();
                
                ccfg.setTitle("Cooker Configuration");
                
                ccfg.setWidth(920);
                ccfg.setHeight(500);
                ccfg.setCanDragReposition(true);
                ccfg.setCanDragResize(true);
                ccfg.addUpdateHandler( new ConfigUpdateHandler() {

                    @Override
                    public void onUpdate( CookerList cookerList)
                    {
                       m_stokerService.updateStokerWebConfig(cookerList, new AsyncCallback<Integer>() {

                        @Override
                        public void onFailure(
                                Throwable caught)
                        {
                            Log.error("unable to update StokerWebConfig");
                        }

                        @Override
                        public void onSuccess(Integer result)
                        {
                            Log.info("Successfully called save ");
                            
                        } 
                         
                       });
                        Log.debug("Config update fired");
                    }
                });
                ccfg.draw();
            }
        });
    }
    
    private void initStokerPage(Date d)
    {
        Log.debug("initStokerPage()");
        if ( m_currentPage != LoadedPage.CONNECTED_PAGE )
        {
            RootPanel.get().clear();

            m_outerDocPanel.setWidth("100%");
            m_outerDocPanel.setHeight("100%");
            
            m_outerDocPanel.setStyleName("sweb-OuterPanel");

            m_HeaderHP.add( new Image( "stokerweb5.png"));
            m_HeaderHP.setWidth("100%");

            final String sessionID = Cookies.getCookie(m_cookieID);
            validateSessionAndToggleSettings( sessionID );

            m_testButton = new Button( "Test", testButtonClickHandler() );
            m_updateButton = new Button( getUpdateButtonText(), updateButtonClickHandler() );
            m_configButton = new Button("Configuration",  configButtonClickHandler() );
            m_reportsButton = new Button( "Reports", reportButtonClickHandler() );
            m_loginButton = new Button(getLoginButtonText(), loginButtonClickHandler() );
            
           // testButton.setEnabled(true);
           // hp.add( testButton);
             
          //  hp.setBorderWidth(1);
            m_statusFauxButton.setEnabled(false);
            m_statusFauxButton.setStyleName("sweb-ConnectionButton");
            m_statusFauxButton.setText("No Connection");
            
            m_HeaderHP.add( m_statusFauxButton );
            
            m_configButton.setEnabled(false);
            m_configButton.setStyleName("sweb-MenuButton");
            m_HeaderHP.add( m_configButton );
            m_HeaderHP.setCellHorizontalAlignment(m_configButton, HasHorizontalAlignment.ALIGN_RIGHT);
            m_HeaderHP.setCellVerticalAlignment(m_configButton, HasVerticalAlignment.ALIGN_BOTTOM);
            
            m_reportsButton.setEnabled(false);
            m_reportsButton.setStyleName("sweb-MenuButton");
            m_HeaderHP.add( m_reportsButton );
            m_HeaderHP.setCellHorizontalAlignment(m_reportsButton, HasHorizontalAlignment.ALIGN_RIGHT);
            m_HeaderHP.setCellVerticalAlignment(m_reportsButton, HasVerticalAlignment.ALIGN_BOTTOM);
            
            m_updateButton.setEnabled(false);
            m_updateButton.setStyleName("sweb-MenuButton");
            m_HeaderHP.add( m_updateButton );
            m_HeaderHP.setCellHorizontalAlignment(m_updateButton, HasHorizontalAlignment.ALIGN_RIGHT);
            m_HeaderHP.setCellVerticalAlignment(m_updateButton, HasVerticalAlignment.ALIGN_BOTTOM);
            
            m_loginButton.setStyleName("sweb-MenuButton");
            m_HeaderHP.add( m_loginButton );

            m_HeaderHP.setCellHorizontalAlignment(m_loginButton, HasHorizontalAlignment.ALIGN_RIGHT);
            m_HeaderHP.setCellVerticalAlignment(m_loginButton, HasVerticalAlignment.ALIGN_BOTTOM);
           
            m_outerDocPanel.add( m_HeaderHP, DockPanel.NORTH );

           // m_cookerOuterPanel.add(m_cookerVP );
           
            m_outerDocPanel.add( m_cookerOuterPanel, DockPanel.CENTER );

            m_weatherComponent = new WeatherComponent( m_weatherData );

            m_outerDocPanel.add( m_weatherComponent, DockPanel.SOUTH );
            
            Log.debug("Getting client properties");
            m_stokerService.getClientProperties(new AsyncCallback<HashMap<String,String>>() {

                public void onFailure(Throwable caught)
                {
                    System.out.println("Failure getting client Properties.  This is serious!");

                }

                public void onSuccess(HashMap<String,String> hm)
                {
                    Log.debug("Client properties received");
                    m_properties = hm;
                    getStokerConfiguration();
                }

            });
            

            // Consider not calling this if the Stoker if off-line,this allows the reports
            // to be generated with Stoker off-line.  This could replace the Stoker not
            // connected page.
            
            
            //getStokerConfiguration();
            
            Window.addWindowClosingHandler(new Window.ClosingHandler() { 
                @Override 
                public void onWindowClosing(ClosingEvent event) { 
                      System.out.println("Browser Close event caught!"); 
                      
                      
                } 
          }); 
            
            m_currentPage = LoadedPage.CONNECTED_PAGE;
            RootPanel.get().add( m_outerDocPanel );
        }

    }

    /**
     * Enables or disables the cooker panel if an extended connection loss
     * is detected.
     */
    private void manageCookerPanel()
    {
       if ( m_cookerVP != null )
       {
            if ( m_eventState == EventTypeLight.EXTENDED_CONNECTION_LOSS)
               m_cookerOuterPanel.remove(m_cookerVP);
            else //if (m_eventState == EventTypeLight.CONNECTION_ESTABLISHED )
            {
                if ( m_cookerOuterPanel.getWidget() == null )
                   m_cookerOuterPanel.add( m_cookerVP );
            }
       }  
       else
       {
           getStokerConfiguration();
       }
    }
    private ClickHandler loginButtonClickHandler()
    {
        return new ClickHandler() {

            public void onClick(ClickEvent event)
            {
                //Integer loginStatus;
                Log.debug("Login button selected");
                if ( LoginStatus.getInstance().getLoginStatus())
                {
                    Log.info("Calling logout");
                    m_stokerService.logout(new AsyncCallback<Void>() {

                        public void onFailure(Throwable caught)
                        {
                            // TODO Auto-generated method stub

                        }

                        public void onSuccess(Void result)
                        {
                            Log.info("Logout success");
                            LoginStatus.getInstance().setLoginStatus(false);
                            userLoggedIn(false );
                            m_loginButton.setText(getLoginButtonText());
                            Cookies.removeCookie("sid");
                        }

                    });
                }
                else
                {
                        new LoginDialog(m_stokerService,new LoginDialogHandler() {

                        public void onLoginReturn(String st)
                        {
                            if ( st != null)
                            {
                                Log.info("Login success");
                                m_httpSessionID = st;
                                userLoggedIn( true );
                                m_loginButton.setText(getLoginButtonText());

                                final long DURATION = 1000 * 60 * 60 * 24 * 1;  // TODO: make this a parameter  ( 1 day )
                                Date expires = new Date(System.currentTimeMillis() + DURATION);
                                Cookies.setCookie("sid", m_httpSessionID, expires, null, "/", false);
                            }
                            else
                            {
                                Log.info("Login failure");
                                userLoggedIn(false );
                                m_loginButton.setText(getLoginButtonText());
                            }

                        }

                    }).center();
                }
            }

        };
    }
    private ClickHandler reportButtonClickHandler()
    {
        return new ClickHandler() {

            public void onClick(ClickEvent event)
            {
                Log.info("Opening reports log chooser");
                Log.debug("Reports button clicked");
                m_stokerService.getLogFileNames(new AsyncCallback<LogDir>() {

                    public void onFailure(Throwable caught)
                    {
                        caught.printStackTrace();
                        System.out.println("Failed to retreive LogFileNames");

                    }

                    public void onSuccess(LogDir result)
                    {
                       new LogFileChooser( result, new LogFileChooserHandler() {

                           public void onReturn(String st)
                           {
                               if ( st != null)
                               {
                                   Log.info("Generating Selected Report: " + st);
                                   String url = new String( GWT.getModuleBaseURL() + "report" + "?logFile=" + st );
                                   Window.open( url, "_blank", "enabled" );
                                   
                               //    new DownloadIFrame(url);
                               }
                               else
                               {

                               }
                           }
                       }).center();
                    }
                });
            }
        };
    }
    private ClickHandler configButtonClickHandler()
    {
       return new ClickHandler() 
        {
            @Override
            public void onClick(ClickEvent event)
            {
                Log.debug("Configuration button clicked");
                presentConfigScreen();
            }
        };
    }
    
    private ClickHandler updateButtonClickHandler()
    {
        return new ClickHandler() {

            public void onClick(ClickEvent event)
            {
                Log.info("Updating Stoker settings");
                ArrayList<SDevice> alUpdates = new ArrayList<SDevice>();
                for ( CookerComponent cc : m_cookerComponentList )
                {
                    Log.debug("Getting config updates from cooker: " + cc.getName());
                    alUpdates.addAll( cc.getConfigUpdates());
                    
                }
                m_stokerService.updateTempAndAlarmSettings( alUpdates, new AsyncCallback<Integer>() {

                    public void onFailure(Throwable caught)
                    {
                        Log.error("updateConfiguration Failure");
                    }

                    public void onSuccess(Integer result)
                    {
                        Log.info("Update Configuration success");
                        if ( result.intValue() == 1 )
                        {
                            new GeneralMessageDialog( "Success", "New Settings saved to Stoker").center();
                        }
                    }
                } );
            }
        };
    }
    private ClickHandler testButtonClickHandler()
    {
        return new ClickHandler() {

            public void onClick(ClickEvent event)
            {
/*                    SoundController soundController = new SoundController();
                Sound sound = soundController.createSound(Sound.MIME_TYPE_AUDIO_WAV_PCM,
                    "Alarm1.wav");
                LoadState s = sound.getLoadState();
                
                sound.play();
*/   
            }
        };
    }
    /**
     * Initializes and controls the comet call back connection.  Any unsolicited and requests made on the comet connection
     * will come through this method. 
     */
    private void initCallBack()
    {
        
          m_stokerService.setupCallBack(new AsyncCallback<Void>() {

                    public void onSuccess(Void result) {
                            Log.info("setupCallBacked returned Success");
                            CometSerializer serializer = GWT.create(StokerCometSerializer.class);
                            //CometClient client = new CometClient(GWT.getModuleBaseURL() + "comet",serializer, new CometListener() {
                             final CometClient client = new CometClient (GWT.getModuleBaseURL() + "comet" + "/" + Math.random (), serializer, new CometListener() {
          
                              public void onConnected(int heartbeat)
                              {
                                 Log.info("Connected, heartbeat: " + heartbeat );
                              }

                              public void onDisconnected()
                              {
                                 Log.info("Disconnected");
                                 
                              }

                              public void onError(Throwable exception, boolean connected)
                              {
                                 Log.error("Error from Comet");
                                 
                              }

                              public void onHeartbeat()
                              {
                                 Log.info("Client, Heartbeat");
                              }

                              public void onRefresh()
                              {
                                 Log.info("Comet Refresh");
                               }

                              public void onMessage(List<? extends Serializable> messages)
                              {
                                for (Serializable message : messages)
                                {
                                    if (message instanceof SDataPoint)
                                    {
                                        Log.trace("Datapoint received, " + message.toString());
                                        SDataPoint sdp = (SDataPoint) message;
                                        
                                        if ( m_cookerComponentList == null )
                                            continue;

                                        for ( CookerComponent cc : m_cookerComponentList )
                                        {
                                            boolean bUpdateGraph = false;
                                            setConnected( true );
                                            cc.updateGauges( sdp );
                                            if ( sdp instanceof SBlowerDataPoint )
                                                bUpdateGraph = true;
                                            else if ( sdp.isTimedEvent() )
                                               bUpdateGraph = true;

                                            if ( bUpdateGraph )
                                               cc.updateGraph( sdp, false );
                                        }

                                    }
                                    else if ( message instanceof ControllerEventLight)
                                    {
                                        Log.debug("ControllerEventLight message received, " + message.toString());
                                        ControllerEventLight event = (ControllerEventLight) message;
                                        switch ( event.getEventType() )
                                        {
                                            case EXTENDED_CONNECTION_LOSS:
                                                Log.info("Detected extended connection loss");
                                                m_eventState = event.getEventType();
                                                manageCookerPanel();
                                                updateButtonStatus();
                                                
                                                break;
                                            case LOST_CONNECTION:   // Add this code
                                                Log.info("Lost connection to Stoker");
                                                m_eventState = EventTypeLight.LOST_CONNECTION;
                                                for ( CookerComponent cc : m_cookerComponentList )
                                                {
                                                    cc.setConnected(false);
                                                    setConnected(false);
                                                }
                                                break;
                                            case CONNECTION_ESTABLISHED:
                                                Log.info("Connection to Stoker established");
                                                m_eventState = EventTypeLight.CONNECTION_ESTABLISHED;
                                                
                                                if (m_cookerList == null)
                                                    initStokerPage(null);
                                                manageCookerPanel();
                                                updateButtonStatus();
                                                
                                                /*if ( m_currentPage != LoadedPage.CONNECTED_PAGE )
                                                {
                                                    initStokerPage(null);  // TODO: implement date
                                                    makeCallBackRequest( new CallBackRequestType( RequestType.FORCE_DATA_PUSH ));
                                                }*/
                                                if ( m_cookerComponentList != null)
                                                    for ( CookerComponent cc : m_cookerComponentList )
                                                    {
                                                        cc.setConnected(true);
                                                        setConnected( true );
                                                    }
                                                break;
                                            case CONFIG_UPDATE:
                                                Log.info("Configuration update received from server");
                                                
                                               /* if ( m_currentPage != LoadedPage.CONNECTED_PAGE )
                                                {
                                                    initStokerPage(null);  // TODO: implement date
                                                    makeCallBackRequest( new CallBackRequestType( RequestType.FORCE_DATA_PUSH ));
                                                }*/

                                                break;
                                            case CONFIG_UPDATE_REFRESH:
                                                Window.Location.reload();
                                                break;
                                            default:
                                        }
                                    }
                                    else if ( message instanceof WeatherData )
                                    {
                                        Log.info("New weather information received");
                                        if ( m_weatherComponent != null)
                                           m_weatherComponent.update((WeatherData) message);
                                    }
                                    else if ( message instanceof HardwareDeviceState )  // GET_STATUS
                                    {
                                        Log.info("Hardware Device messaage");
                                        HardwareDeviceState hds = (HardwareDeviceState) message;

                                        if ( hds.getHardwareStatus() == Status.CONNECTED )
                                        {
                                            Log.info("Hardware Status: Connected");
                                            initStokerPage(hds.getDate() );
                                            makeCallBackRequest( new CallBackRequestType( RequestType.FORCE_DATA_PUSH ));
                                        }
                                        else if ( hds.getHardwareStatus() == Status.DISCONNECTED )
                                        {
                                            Log.info("Hardware status: Disconnected");
                                            initStokerPage(hds.getDate() );
                                            //initNotConnectedPage( hds.getDate() );
                                        }

                                    }
                                    else if ( message instanceof BrowserAlarmModel )
                                    {
                                        Log.info("Browser Alarm Model message received");
                                        // TODO: Open Alarm Dialog.  
                                        //       Have quiet button and suppress for either:
                                        //       x minutes
                                        //       until Alarm condition is gone
                                        //       forever ( remove alarm )
                                        BrowserAlarmModel bam = (BrowserAlarmModel) message;
                                        new AlertDialog(m_stokerService, bam, new AlertDialogHandler() {

                                            @Override
                                            public void onReturn( BrowserAlarmModel bam)
                                            {
                                                
                                                
                                            }

                                          }).center();
                                    }
                                    else if ( message instanceof LogEvent )
                                    {
                                        Log.info("LogEvent message received");
                                       LogEvent le = (LogEvent) message;
                                       
                                       
                                       for ( CookerComponent cc : m_cookerComponentList )
                                       {
                                           if ( cc.getName().compareTo(le.getCookerName()) == 0)
                                           {
                                              Log.info("Log Event for Cooker: " + le.getCookerName() );
                                              switch ( le.getEventType() )
                                              {
                                                 case NONE:
                                                    break;
                                                 case NEW:
                                                    cc.logAdded();
                                                    Log.info("Log added");
                                                    break;
                                                 case UPDATED:
                                                    break;
                                                 case DELETED:
                                                     Log.info("Log deleted, " + message.toString());
                                                    cc.removeLog(((LogEvent) message).getLogName());
                                                    break;
                                                 default:       
                                              }
                                          }
                                       }
                                    }

                                    /*
                                     * else if (message instanceof StatusUpdate) {
                                     * StatusUpdate statusUpdate = (StatusUpdate)
                                     * message; output(statusUpdate.getUsername() + ": "
                                     * + statusUpdate.getStatus(), "green"); }
                                     */
                                    else
                                    {
                                        Log.error("unknown message "
                                                + message);
                                    }
                                }
                                 // Draw here
                                if ( m_cookerComponentList != null)
                                    for ( CookerComponent cc : m_cookerComponentList )
                                    {
                                        Log.trace("Calling draw for cooker: " + cc.getName());
                                        cc.draw();
                                    }
                              }

                            });
                            client.start();
                            makeCallBackRequest( new CallBackRequestType( RequestType.GET_STATUS ));

                    }

                    public void onFailure(Throwable caught) {
                        Log.error("setupCallBacked returned Failure");
                       System.out.println("Failure");
                    }
            });
    }

    /**
     * Validate sessionID from cookie with the server.  Toggle visibility of adjustable settings
     * and buttons depending on the return from the server validation.
     * 
     * @param sessionID Session ID to be validated
     */
    private void validateSessionAndToggleSettings( final String sessionID )
    {
        if ( sessionID != null )
        {
            Log.info("Found sessionID: " + sessionID);
            m_stokerService.validateSession( sessionID, new AsyncCallback<Integer>() {
                public void onFailure(Throwable caught)
                {
                    Log.error("Failure attempting to validate session");
                }

                public void onSuccess(Integer result)
                {
                    if ( result.intValue() == 1)
                    {
                        Log.info("Session validated successfully");
                       m_httpSessionID = sessionID;
                       userLoggedIn( true );
                       m_loginButton.setText(getLoginButtonText());
                    }
                }
            });
        }
        else
        {
            Log.info("no session found for user.");
            userLoggedIn( false );
        }
    }
    
    
    /** 
     * Sets the connection on the screen based on the boolean value.
     * @param connected boolean value for connection state
     */
    private void setConnected(boolean connected)
    {
        if ( m_bConnected == connected )
            return;
        
        if ( !connected )
        {
           m_statusFauxButton.setStyleName("sweb-ConnectionButton");
           m_statusFauxButton.setText("No Connection");
           m_bConnected = false;
        }
        else
        {
            m_statusFauxButton.setStyleName("sweb-ConnectionButtonOn");
            m_statusFauxButton.setText("  Connected  ");
            m_bConnected = true;
        }
    }
    
    private String getUpdateButtonText()
    {
        if ( m_requiresUpdate == true )
        {
            return "Update";
        }
        else
            return "Up to Date";
    }
    private String getLoginButtonText()
    {
        if ( LoginStatus.getInstance().getLoginStatus())
            return "Sign out";
        else
            return "Sign in";
    }

    /**
     * Gets configuration information from the server and calls createCookers to create the cooker
     * components.  
     */
    private void getStokerConfiguration()
    {
        Log.debug("getStokerConfiguration()");
        m_stokerService.getStokerWebConfiguration(new AsyncCallback<CookerList>() {

            public void onFailure(Throwable caught)
            {
                caught.printStackTrace();
                Log.error("Client configuration failure");
                m_cookerList = null;
            }

            public void onSuccess(CookerList result)
            {
                System.out.println("getConfiguration complete.");

                Log.info("Successfully retreived stoker configuration from server");
                m_cookerList = result;
                buildCookers();
            }

        });
    }

    private void buildCookers()
    {
        if ( m_cookerComponentList == null && m_cookerList != null )
        {
            m_cookerComponentList = new ArrayList<CookerComponent>();
            createCookers( m_cookerList, m_cookerComponentList );
            if ( m_cookerList == null )
            {
                // configuration fetch failed, reset this so we come back in here.
                m_cookerComponentList = null;
                return;
            }
    
            for ( CookerComponent cc : m_cookerComponentList)
            {
                Log.trace("Calling draw for cooker: " + cc.getName());
               cc.draw();
            }
        }        
        
    }
 
    private void createCookers(CookerList cookerList, ArrayList<CookerComponent> componentList )
    {
        Log.debug("createCookers()");
    
       if ( cookerList == null || cookerList.getCookerList().size() == 0)
       {
           presentConfigScreen();
       }
       else
       {
           m_cookerVP = new VerticalPanel();
        
           m_cookerOuterPanel.add( m_cookerVP );
           ArrayList<Cooker> alc = cookerList.getCookerList();
            for ( Cooker cooker : alc )
            {
                CookerComponent cookerComponent = new CookerComponent(m_stokerService, m_properties);
                
                m_cookerVP.add( cookerComponent );  // This needs to be done before sending the data so the graph window size is correct.
    
                m_cookerVP.setWidth("100%");
    
                int numProbes = CookerHelper.getProbeCount(cooker);
                // Debug ***
                Log.debug("numProbes is [" + numProbes + "]");
                if ( numProbes > 3 )
                   cookerComponent.setOrientation( Alignment.MULTIPLE );   // The default is Single, so only multiple needs to be set
                cookerComponent.init( cooker );
                componentList.add(cookerComponent);
            }
       }    
       manageCookerPanel();  // this must be done after since the size is required to build the panel
    }

    private void updateButtonStatus( )
    {
        boolean status = LoginStatus.getInstance().getLoginStatus();
        m_updateButton.setEnabled(status && m_bConnected );
        m_reportsButton.setEnabled(status);
        m_configButton.setEnabled(status && m_bConnected );           
    }
    
    private void userLoggedIn( boolean b )
    {
        Log.info("Setting login status: " + b );
        LoginStatus.getInstance().setLoginStatus(b);

        updateButtonStatus();

        if ( m_cookerComponentList != null)
            for ( CookerComponent cc : m_cookerComponentList)
            {
               cc.loginEvent();
            }

    }

    public class APICounter {
        private int i = 0;

        public synchronized void increment() {
            i++;
        }

        public synchronized void decrement() {
            i--;
        }

        public synchronized int value() {
            return i;
        }
    }
}
