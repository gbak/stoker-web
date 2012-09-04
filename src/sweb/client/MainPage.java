package sweb.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.zschech.gwt.comet.client.CometClient;
import net.zschech.gwt.comet.client.CometListener;
import net.zschech.gwt.comet.client.CometSerializer;
import net.zschech.gwt.comet.client.SerialTypes;
//import sweb.client.StokerWeb.LoadedPage;
//import sweb.client.StokerWeb.StokerCometSerializer;
import sun.security.krb5.internal.ccache.CCacheInputStream;
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
import sweb.shared.model.ConfigurationSettings;
import sweb.shared.model.CookerHelper;
import sweb.shared.model.HardwareDeviceStatus;
import sweb.shared.model.CallBackRequestType.RequestType;
import sweb.shared.model.HardwareDeviceStatus.Status;
import sweb.shared.model.alerts.AlertModel;
import sweb.shared.model.alerts.BrowserAlarmModel;
import sweb.shared.model.data.SBlowerDataPoint;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.data.SProbeDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.events.ControllerEventLight;
import sweb.shared.model.events.LogEvent;
import sweb.shared.model.events.ControllerEventLight.EventTypeLight;
import sweb.shared.model.logfile.LogDir;
import sweb.shared.model.stoker.StokerProbe;
import sweb.shared.model.stoker.StokerDeviceTypes.DeviceType;
import sweb.shared.model.weather.WeatherData;
import sweb.shared.model.Cooker;
import sweb.shared.model.CookerHelper;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import sweb.shared.model.CookerList;

public class MainPage
{
    /**
     * The message displayed to the user when the server cannot be reached or
     * returns an error.
     */
    private static final String SERVER_ERROR = "An error occurred while "
            + "attempting to contact the server. Please check your network "
            + "connection and try again.";


    private final StokerCoreServiceAsync stokerService = GWT.create(StokerCoreService.class);

    private static enum LoadedPage { NONE, CONNECTED_PAGE, NOT_CONNECTED_PAGE };
    EventBus eb;
    
    
    LoadedPage currentPage = LoadedPage.NONE;

    ArrayList<CookerComponent> alCookers = new ArrayList<CookerComponent>();
    WeatherData weatherData = new WeatherData();
    WeatherComponent wc = null;

    EventTypeLight eventState = EventTypeLight.NONE;

    DockPanel dp = new DockPanel(); // Outermost panel
    HorizontalPanel hp = new HorizontalPanel();
    VerticalPanel vpCookers = new VerticalPanel(); // For Multiple cookers
    Button loginButton = new Button();
    Button updateButton = new Button();
    Button reportsButton = new Button();
    Button configButton = new Button();
    Button statusFauxButton = new Button();
    Button testButton = new Button();  // Test various functions.

    boolean requiresUpdate = true;  // TODO: hard coding this to so the button stays enabled.

    boolean bConnected = false;
    
    String httpSessionID = "";
    
    HashMap<String,String> properties = null;


    @SerialTypes(
    { SDataPoint.class, SProbeDataPoint.class, SBlowerDataPoint.class, ControllerEventLight.class, WeatherData.class, CallBackRequestType.class,
        HardwareDeviceStatus.class, LogEvent.class, AlertModel.class, BrowserAlarmModel.class, CookerList.class, Cooker.class, CookerHelper.class })

    public static abstract class StokerCometSerializer extends CometSerializer {
    }

    public MainPage()
    {
        initCallBack();
    }

    /** Wrapper for cometRequest async server call.  This is a convenience method to server
     * calls without having to deal with the async response.  Comet calls only return
     * data to the comet stream.
     *
     * @param crt The enumerated request type
     */
    void makeCallBackRequest( CallBackRequestType crt )
    {

        stokerService.cometRequest( crt, new AsyncCallback<Void>() {

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
        if ( currentPage != LoadedPage.NOT_CONNECTED_PAGE )
        {
            Log.info("Stoker not connected, setting up not connected page");
           RootPanel.get().clear();

           DockPanel dpDisconnected = new DockPanel();
           HTML ht = new HTML("Stoker not connected!");
           dpDisconnected.add( ht, DockPanel.NORTH);

           currentPage = LoadedPage.NOT_CONNECTED_PAGE;
           RootPanel.get().add( dpDisconnected );
        }
    }

    private void presentConfigScreen()
    {
        stokerService.getDeviceConfiguration(new AsyncCallback<ConfigurationSettings>() 
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
                       stokerService.updateStokerWebConfig(cookerList, new AsyncCallback<Integer>() {

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
        if ( currentPage != LoadedPage.CONNECTED_PAGE )
        {
            RootPanel.get().clear();

            dp.setWidth("100%");
            dp.setHeight("100%");

            hp.add( new Image( "stokerweb5.png"));
            hp.setWidth("100%");

            final String sessionID = Cookies.getCookie("sid");
            
            if ( sessionID != null )
            {
                Log.info("Found sessionID: " + sessionID);
                stokerService.validateSession( sessionID, new AsyncCallback<Integer>() {
                    public void onFailure(Throwable caught)
                    {
                        // TODO Auto-generated method stub
                        Log.error("Failure attempting to validate session");
                    }

                    public void onSuccess(Integer result)
                    {
                        if ( result.intValue() == 1)
                        {
                            Log.info("Session validated successfully");
                           httpSessionID = sessionID;
                           userLoggedIn( true );
                           loginButton.setText(getLoginButtonText());
                        }
                    }
                });
            }
            else
            {
                Log.info("no session found for user.");
                userLoggedIn( false );
            }

            testButton = new Button( "Test", new ClickHandler() {

                public void onClick(ClickEvent event)
                {
/*                    SoundController soundController = new SoundController();
                    Sound sound = soundController.createSound(Sound.MIME_TYPE_AUDIO_WAV_PCM,
                        "Alarm1.wav");
                    LoadState s = sound.getLoadState();
                    
                    sound.play();
*/
                    
                }

            });

            
            updateButton = new Button( getUpdateButtonText(), new ClickHandler() {

                public void onClick(ClickEvent event)
                {
                    Log.info("Updating Stoker settings");
                    ArrayList<SDevice> alUpdates = new ArrayList<SDevice>();
                    for ( CookerComponent cc : alCookers )
                    {
                        Log.debug("Getting config updates from cooker: " + cc.getName());
                        alUpdates.addAll( cc.getConfigUpdates());
                        
                    }
                    stokerService.updateTempSettings( alUpdates, new AsyncCallback<Integer>() {

                        public void onFailure(Throwable caught)
                        {
                            // TODO Auto-generated method stub
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

            });

            configButton = new Button("Configuration", new ClickHandler() 
            {

                @Override
                public void onClick(ClickEvent event)
                {

                    Log.debug("Configuration button clicked");
                    presentConfigScreen();
                    
                }
            });

            
            reportsButton = new Button( "Reports", new ClickHandler() {

                public void onClick(ClickEvent event)
                {
                    Log.info("Opening reports log chooser");
                    Log.debug("Reports button clicked");
                    stokerService.getLogFileNames(new AsyncCallback<LogDir>() {

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
            });
            
            loginButton = new Button(getLoginButtonText(), new ClickHandler() {

                public void onClick(ClickEvent event)
                {
                    //Integer loginStatus;
                    Log.debug("Login button selected");
                    if ( LoginStatus.getInstance().getLoginStatus())
                    {
                        Log.info("Calling logout");
                        stokerService.logout(new AsyncCallback<Void>() {

                            public void onFailure(Throwable caught)
                            {
                                // TODO Auto-generated method stub

                            }

                            public void onSuccess(Void result)
                            {
                                Log.info("Logout success");
                                LoginStatus.getInstance().setLoginStatus(false);
                                userLoggedIn(false );
                                loginButton.setText(getLoginButtonText());
                                Cookies.removeCookie("sid");
                            }

                        });
                    }
                    else
                    {
                            new LoginDialog(stokerService,new LoginDialogHandler() {

                            public void onLoginReturn(String st)
                            {
                                if ( st != null)
                                {
                                    Log.info("Login success");
                                    httpSessionID = st;
                                    userLoggedIn( true );
                                    loginButton.setText(getLoginButtonText());

                                    final long DURATION = 1000 * 60 * 60 * 24 * 1;  // TODO: make this a parameter  ( 1 day )
                                    Date expires = new Date(System.currentTimeMillis() + DURATION);
                                    Cookies.setCookie("sid", httpSessionID, expires, null, "/", false);
                                }
                                else
                                {
                                    Log.info("Login failure");
                                    userLoggedIn(false );
                                    loginButton.setText(getLoginButtonText());
                                }

                            }

                        }).center();
                    }
                }

            });
            
           // testButton.setEnabled(true);
           // hp.add( testButton);
             
          //  hp.setBorderWidth(1);
            statusFauxButton.setEnabled(false);
            statusFauxButton.setStyleName("sweb-ConnectionButton");
            statusFauxButton.setText("No Connection");
            
            hp.add( statusFauxButton );
            
            configButton.setEnabled(false);
            configButton.setStyleName("sweb-MenuButton");
            hp.add( configButton );
            hp.setCellHorizontalAlignment(configButton, HasHorizontalAlignment.ALIGN_RIGHT);
            hp.setCellVerticalAlignment(configButton, HasVerticalAlignment.ALIGN_BOTTOM);
            
            reportsButton.setEnabled(false);
            reportsButton.setStyleName("sweb-MenuButton");
            hp.add( reportsButton );
            hp.setCellHorizontalAlignment(reportsButton, HasHorizontalAlignment.ALIGN_RIGHT);
            hp.setCellVerticalAlignment(reportsButton, HasVerticalAlignment.ALIGN_BOTTOM);
            
            updateButton.setEnabled(false);
            updateButton.setStyleName("sweb-MenuButton");
            hp.add( updateButton );
            hp.setCellHorizontalAlignment(updateButton, HasHorizontalAlignment.ALIGN_RIGHT);
            hp.setCellVerticalAlignment(updateButton, HasVerticalAlignment.ALIGN_BOTTOM);
            
            loginButton.setStyleName("sweb-MenuButton");
            hp.add( loginButton );

            hp.setCellHorizontalAlignment(loginButton, HasHorizontalAlignment.ALIGN_RIGHT);
            hp.setCellVerticalAlignment(loginButton, HasVerticalAlignment.ALIGN_BOTTOM);
           
            dp.add( hp, DockPanel.NORTH );

            dp.add( vpCookers, DockPanel.CENTER );

            wc = new WeatherComponent( weatherData );

            dp.add( wc, DockPanel.SOUTH );
            
            Log.debug("Getting client properties");
            stokerService.getClientProperties(new AsyncCallback<HashMap<String,String>>() {

                public void onFailure(Throwable caught)
                {
                    System.out.println("Failure getting client Properties.  This is serious!");

                }

                public void onSuccess(HashMap<String,String> hm)
                {
                    Log.debug("Client properties received");
                    properties = hm;
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
            
            currentPage = LoadedPage.CONNECTED_PAGE;
            RootPanel.get().add( dp );
        }

    }

    private void initCallBack()
    {
        
          stokerService.setupCallBack(new AsyncCallback<Void>() {

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

                                        for ( CookerComponent cc : alCookers )
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
                                            case LOST_CONNECTION:   // Add this code
                                                Log.info("Lost connection to Stoker");
                                                eventState = EventTypeLight.LOST_CONNECTION;
                                                for ( CookerComponent cc : alCookers )
                                                {
                                                    cc.setConnected(false);
                                                    setConnected(false);
                                                }
                                                break;
                                            case CONNECTION_ESTABLISHED:
                                                Log.info("Connection to Stoker established");
                                                eventState = EventTypeLight.CONNECTION_ESTABLISHED;
                                                if ( currentPage != LoadedPage.CONNECTED_PAGE )
                                                {
                                                    initStokerPage(null);  // TODO: implement date
                                                    makeCallBackRequest( new CallBackRequestType( RequestType.FORCE_DATA_PUSH ));
                                                }
                                                for ( CookerComponent cc : alCookers )
                                                {
                                                    cc.setConnected(true);
                                                    setConnected( true );
                                                }
                                                break;
                                            case CONFIG_UPDATE:
                                                Log.info("Configuration update received from server");
                                                if ( currentPage != LoadedPage.CONNECTED_PAGE )
                                                {
                                                    initStokerPage(null);  // TODO: implement date
                                                    makeCallBackRequest( new CallBackRequestType( RequestType.FORCE_DATA_PUSH ));
                                                }
                                             //   getStokerConfiguration();
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
                                        if ( wc != null)
                                           wc.update((WeatherData) message);
                                    }
                                    else if ( message instanceof HardwareDeviceStatus )  // GET_STATUS
                                    {
                                        Log.info("Hardware Device messaage");
                                        HardwareDeviceStatus hds = (HardwareDeviceStatus) message;

                                        if ( hds.getHardwareStatus() == Status.CONNECTED )
                                        {
                                            Log.info("Hardware Status: Connected");
                                            initStokerPage(hds.getDate() );
                                            makeCallBackRequest( new CallBackRequestType( RequestType.FORCE_DATA_PUSH ));
                                        }
                                        else if ( hds.getHardwareStatus() == Status.DISCONNECTED )
                                        {
                                            Log.info("Hardware status: Disconnected");
                                            initNotConnectedPage( hds.getDate() );
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
                                        new AlertDialog(stokerService, bam, new AlertDialogHandler() {

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
                                       
                                       
                                       for ( CookerComponent cc : alCookers )
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
                                for ( CookerComponent cc : alCookers )
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

    private void setConnected(boolean connected)
    {
        if ( bConnected == connected )
            return;
        
        if ( !connected )
        {
           statusFauxButton.setStyleName("sweb-ConnectionButton");
           statusFauxButton.setText("No Connection");
           bConnected = false;
        }
        else
        {
            statusFauxButton.setStyleName("sweb-ConnectionButtonOn");
            statusFauxButton.setText("  Connected  ");
            bConnected = true;
        }
    }
    
    private String getUpdateButtonText()
    {
        if ( requiresUpdate == true )
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
/*
    private void getStokerConfiguration()
    {
        Log.debug("getStokerConfiguration()");
        stokerService.getDeviceConfiguration(new AsyncCallback<HashMap<String,SDevice>>() {

            public void onFailure(Throwable caught)
            {
                caught.printStackTrace();
                Log.error("Client configuration failure");

            }

            public void onSuccess(HashMap<String,SDevice> result)
            {
                System.out.println("getConfiguration complete.");

                Log.info("Successfully retreived stoker configuration from server");
                createCookers( result, alCookers );

                for ( CookerComponent cc : alCookers)
                {
                    Log.trace("Calling draw for cooker: " + cc.getName());
                   cc.draw();
                }

            }

        });
    }
    */
    private void getStokerConfiguration()
    {
        Log.debug("getStokerConfiguration()");
        stokerService.getStokerWebConfiguration(new AsyncCallback<CookerList>() {

            public void onFailure(Throwable caught)
            {
                caught.printStackTrace();
                Log.error("Client configuration failure");

            }

            public void onSuccess(CookerList result)
            {
                System.out.println("getConfiguration complete.");

                Log.info("Successfully retreived stoker configuration from server");
                createCookers( result, alCookers );

                for ( CookerComponent cc : alCookers)
                {
                    Log.trace("Calling draw for cooker: " + cc.getName());
                   cc.draw();
                }

            }

        });
    }

    /*
    private void getLogList()
    {

        stokerService.getLogList(new AsyncCallback<ArrayList<LogItem>>() {

            public void onFailure(Throwable caught)
            {
                caught.printStackTrace();
                System.out.println("Failed to retreive LogList");

            }

            public void onSuccess(ArrayList<LogItem> result)
            {
                for ( CookerComponent cc : alCookers)
                {
                    cc.updateLogList(result);
                }

            }

        });

    }
    */



    private void createCookers(CookerList cookerList, ArrayList<CookerComponent> componentList )
    {
        Log.debug("createCookers()");
        /*
       ArrayList<SDevice> alDevices = new ArrayList<SDevice>(result.values());

       Collections.sort(alDevices, new Comparator<SDevice>() {

            public int compare(SDevice o1, SDevice o2)
            {

                int iNum = o1.getCookerNum() - o2.getCookerNum();
                if ( iNum != 0 )
                    return iNum;

                if ( o1.isProbe() == true)
                {
                    StokerProbe o3 = (StokerProbe) o1;
                    if ( o3.getFanDevice() != null )
                        return -1;
                    else
                        return 1;

                }

                return 0;
            }

           });

       int iNumCookers = getNumCookers( alDevices );
       Log.debug("Found [" + iNumCookers + "] cookers");

       int iCooker = 0;
       for ( int i = 1; i <= iNumCookers; i++ )
       {
           // Create a CookerComponent for each cooker.  This is a pit probe with a blower association
           CookerComponent cc = new CookerComponent(stokerService, properties);
           

           vpCookers.add( cc );  // This needs to be done before sending the data so the graph window size is correct.

           vpCookers.setWidth("100%");

           int numProbes = getNumProbesForCooker( i, alDevices );
           // Debug ***
           Log.debug("numProbes is [" + numProbes + "]");
           if ( numProbes > 3 )
              cc.setOrientation( Alignment.MULTIPLE );   // The default is Single, so only multiple needs to be set
           
           Iterator<SDevice> deviceIter = alDevices.iterator();
           while (deviceIter.hasNext())
           {
               SDevice sd = deviceIter.next();
               if ( sd.getCookerNum() == i)
               {
                   cc.addDevice( sd );
               }
           }
           cc.init();
           cooker.add( cc );
       }
       */
       if ( cookerList == null || cookerList.getCookerList().size() == 0)
       {
           presentConfigScreen();
       }
       else
       {
           ArrayList<Cooker> alc = cookerList.getCookerList();
            for ( Cooker cooker : alc )
            {
                CookerComponent cookerComponent = new CookerComponent(stokerService, properties);
                
                vpCookers.add( cookerComponent );  // This needs to be done before sending the data so the graph window size is correct.
    
                vpCookers.setWidth("100%");
    
                int numProbes = CookerHelper.getProbeCount(cooker);
                // Debug ***
                Log.debug("numProbes is [" + numProbes + "]");
                if ( numProbes > 3 )
                   cookerComponent.setOrientation( Alignment.MULTIPLE );   // The default is Single, so only multiple needs to be set
                cookerComponent.init( cooker );
                componentList.add(cookerComponent);
            }
       }        
    }

    /*
    @Deprecated
    private int getNumProbesForCooker( int cookerNum,  ArrayList<SDevice> alDevices )
    {
        int i = 0;
        Iterator<SDevice> deviceIter = alDevices.iterator();
        while (deviceIter.hasNext())
        {
            SDevice sd = deviceIter.next();
            if (( sd.getProbeType() == DeviceType.PIT || sd.getProbeType() == DeviceType.FOOD) && sd.getCookerNum() == cookerNum)
            {
               i++;
            }
        }
        return i;
    }
    */
   /* private int getNumCookers( ArrayList<SDevice> sda )
    {
        int iMax = 1;
        Iterator<SDevice> deviceIter = sda.iterator();
        while (deviceIter.hasNext())
        {
            SDevice sd = deviceIter.next();
            if ( sd.getCookerNum() > iMax )
                iMax = sd.getCookerNum();
        }
        return iMax;
    }*/

    private void userLoggedIn( boolean b )
    {
        Log.info("Setting login status: " + b );
        LoginStatus.getInstance().setLoginStatus(b);

        updateButton.setEnabled(b);
        reportsButton.setEnabled(b);
        configButton.setEnabled(b);
        for ( CookerComponent cc : alCookers)
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
