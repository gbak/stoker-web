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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import sweb.client.dialog.GeneralMessageDialog;

import net.zschech.gwt.comet.client.CometClient;
import net.zschech.gwt.comet.client.CometListener;
import net.zschech.gwt.comet.client.CometSerializer;
import net.zschech.gwt.comet.client.SerialTypes;
import sweb.client.dialog.StokerMenu;
import sweb.server.controller.StokerConfiguration;
import sweb.shared.model.CallBackRequestType;
import sweb.shared.model.CallBackRequestType.RequestType;
import sweb.shared.model.HardwareDeviceStatus;
import sweb.shared.model.HardwareDeviceStatus.Status;
import sweb.shared.model.LogItem;
import sweb.shared.model.SBlowerDataPoint;
import sweb.shared.model.SDataPoint;
import sweb.shared.model.SDevice;
import sweb.shared.model.SProbeDataPoint;
import sweb.shared.model.StokerProbe;
import sweb.shared.model.alerts.Alert;
import sweb.shared.model.events.ControllerEventLight;
import sweb.shared.model.events.ControllerEventLight.EventTypeLight;
import sweb.shared.model.events.LogEvent;
import sweb.shared.model.weather.WeatherData;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
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
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.Gauge;

import sweb.client.dialog.LoginDialog;
import sweb.client.dialog.handlers.LoginDialogHandler;
import sweb.client.weather.WeatherComponent;

public class StokerWeb implements EntryPoint
{
    /**
     * The message displayed to the user when the server cannot be reached or
     * returns an error.
     */
    private static final String SERVER_ERROR = "An error occurred while "
            + "attempting to contact the server. Please check your network "
            + "connection and try again.";


    private final StokerCoreServiceAsync stokerService = GWT
            .create(StokerCoreService.class);

    private static enum LoadedPage { NONE, CONNECTED_PAGE, NOT_CONNECTED_PAGE };

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

    boolean requiresUpdate = true;  // TODO: hardcoding this to so the button stays enabled.

    String httpSessionID = "";


    @SerialTypes(
    { SDataPoint.class, SProbeDataPoint.class, SBlowerDataPoint.class, ControllerEventLight.class, WeatherData.class, CallBackRequestType.class,
        HardwareDeviceStatus.class, LogEvent.class, Alert.class })

    public static abstract class StokerCometSerializer extends CometSerializer {
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
                System.out.println("Failure calling makeCometRequest()");

            }

            public void onSuccess(Void result)
            {


            }

        });
    }

    public void onModuleLoad()
    {
     //   RootPanel.get().clear();

        initCallBack();



     //   RootPanel.get().add( dp );
    }

    private void initNotConnectedPage(Date d)
    {
        if ( currentPage != LoadedPage.NOT_CONNECTED_PAGE )
        {
           RootPanel.get().clear();

           DockPanel dpDisconnected = new DockPanel();
           HTML ht = new HTML("Stoker not connected!");
           dpDisconnected.add( ht, DockPanel.NORTH);

           currentPage = LoadedPage.NOT_CONNECTED_PAGE;
           RootPanel.get().add( dpDisconnected );
        }
    }

    private void initStokerPage(Date d)
    {

        if ( currentPage != LoadedPage.CONNECTED_PAGE )
        {
            RootPanel.get().clear();

            StokerMenu sm = new StokerMenu();

            dp.setWidth("100%");
            dp.setHeight("100%");

            hp.add( new Image("stokerweb5.png"));
            hp.setWidth("100%");

            final String sessionID = Cookies.getCookie("sid");
            if ( sessionID != null )
            {
                stokerService.validateSession( sessionID, new AsyncCallback<Integer>() {
                    public void onFailure(Throwable caught)
                    {
                        // TODO Auto-generated method stub

                    }

                    public void onSuccess(Integer result)
                    {
                        if ( result.intValue() == 1)
                        {
                           httpSessionID = sessionID;
                           userLoggedIn( true );
                           loginButton.setText(getLoginButtonText());
                        }
                    }
                });
            }
            else
            {
                userLoggedIn( false );
            }

            updateButton = new Button( getUpdateButtonText(), new ClickHandler() {

                public void onClick(ClickEvent event)
                {
                    ArrayList<SDevice> alUpdates = new ArrayList<SDevice>();
                    for ( CookerComponent cc : alCookers )
                    {
                        alUpdates.addAll( cc.getConfigUpdates());
                    }
                    stokerService.updateConfiguration( alUpdates, new AsyncCallback<Integer>() {

                        public void onFailure(Throwable caught)
                        {
                            // TODO Auto-generated method stub

                        }

                        public void onSuccess(Integer result)
                        {
                            if ( result.intValue() == 1 )
                            {
                                new GeneralMessageDialog( "Success", "New Settings saved to Stoker").center();
                            }

                        }

                    } );


                }

            });

            loginButton = new Button(getLoginButtonText(), new ClickHandler() {

                public void onClick(ClickEvent event)
                {
                    //Integer loginStatus;
                    if ( LoginStatus.getInstance().getLoginStatus())
                    {
                        stokerService.logout(new AsyncCallback<Void>() {

                            public void onFailure(Throwable caught)
                            {
                                // TODO Auto-generated method stub

                            }

                            public void onSuccess(Void result)
                            {
                                LoginStatus.getInstance().setLoginStatus(false);
                                userLoggedIn(false );
                                loginButton.setText(getLoginButtonText());
                                Cookies.removeCookie("sid");
                            }

                        });
                    }
                    else
                    {
                        new LoginDialog(stokerService, new LoginDialogHandler() {

                            public void onLoginReturn(String st)
                            {
                                if ( st != null)
                                {
                                    httpSessionID = st;
                                    userLoggedIn( true );
                                    loginButton.setText(getLoginButtonText());

                                    final long DURATION = 1000 * 60 * 60 * 24 * 1;  // TODO: make this a parameter  ( 1 day )
                                    Date expires = new Date(System.currentTimeMillis() + DURATION);
                                    Cookies.setCookie("sid", httpSessionID, expires, null, "/", false);
                                }
                                else
                                {
                                    userLoggedIn(false );
                                    loginButton.setText(getLoginButtonText());
                                }

                            }

                        }).center();
                    }
                }

            });
            updateButton.setEnabled(false);
            updateButton.setStyleName("Button-login");
            hp.add( updateButton );

            loginButton.setStyleName("Button-login");
            hp.add( loginButton );

            hp.setCellHorizontalAlignment(loginButton, HasHorizontalAlignment.ALIGN_RIGHT);
            hp.setCellVerticalAlignment(loginButton, HasVerticalAlignment.ALIGN_MIDDLE);
           // hp.add( sm );
            sm.setWidth("100%");


            dp.add( hp, DockPanel.NORTH );

            dp.add( vpCookers, DockPanel.CENTER );

            wc = new WeatherComponent( weatherData );

            dp.add( wc, DockPanel.SOUTH );
            Runnable onLoadCallBack = new Runnable() {

                public void run()
                {
                   getStokerConfiguration();

                }

            };


            VisualizationUtils.loadVisualizationApi(onLoadCallBack, Gauge.PACKAGE, AnnotatedTimeLine.PACKAGE);

            currentPage = LoadedPage.CONNECTED_PAGE;
            RootPanel.get().add( dp );
        }

    }

    private void initCallBack()
    {
          stokerService.setupCallBack(new AsyncCallback<Void>() {

                    public void onSuccess(Void result) {
                            System.out.println("connect Success");
                            CometSerializer serializer = GWT.create(StokerCometSerializer.class);
                            CometClient client = new CometClient(GWT.getModuleBaseURL() + "comet",serializer, new CometListener() {

                              public void onConnected(int heartbeat)
                              {
                                 System.out.println("Connected, heartbeat: " + heartbeat );
                              }

                              public void onDisconnected()
                              {
                                 System.out.println("Disconnected");
                              }

                              public void onError(Throwable exception, boolean connected)
                              {
                                 System.out.println("Error");
                                 //exception.printStackTrace();
                                 System.out.println("Connected: " + connected);
                              }

                              public void onHeartbeat()
                              {
                                 System.out.println("Client, Heartbeat");
                              }

                              public void onRefresh()
                              {
                                 System.out.println("Refresh");
                               }

                              public void onMessage(List<? extends Serializable> messages)
                              {
                                for (Serializable message : messages)
                                {
                                    if (message instanceof SDataPoint)
                                    {

                                        SDataPoint sdp = (SDataPoint) message;

                                        for ( CookerComponent cc : alCookers )
                                        {
                                            boolean bUpdateGraph = false;

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
                                        ControllerEventLight event = (ControllerEventLight) message;
                                        switch ( event.getEventType() )
                                        {
                                            case LOST_CONNECTION:   // Add this code
                                                eventState = EventTypeLight.LOST_CONNECTION;
                                                for ( CookerComponent cc : alCookers )
                                                {
                                                    cc.setConnected(false);
                                                   // cc.updateGauges( null );  // empty class will 0 out gauges
                                                }
                                                break;
                                            case CONNECTION_ESTABLISHED:
                                                eventState = EventTypeLight.CONNECTION_ESTABLISHED;
                                                if ( currentPage != LoadedPage.CONNECTED_PAGE )
                                                {
                                                    initStokerPage(null);  // TODO: implement date
                                                    makeCallBackRequest( new CallBackRequestType( RequestType.FORCE_DATA_PUSH ));
                                                }
                                                for ( CookerComponent cc : alCookers )
                                                {
                                                    cc.setConnected(true);
                                                   // cc.updateGauges( null );  // empty class will 0 out gauges
                                                }
                                                break;
                                            case CONFIG_UPDATE:
                                                if ( currentPage != LoadedPage.CONNECTED_PAGE )
                                                {
                                                    initStokerPage(null);  // TODO: implement date
                                                    makeCallBackRequest( new CallBackRequestType( RequestType.FORCE_DATA_PUSH ));
                                                }
                                             //   getStokerConfiguration();
                                                break;
                                            default:
                                        }
                                    }
                                    else if ( message instanceof WeatherData )
                                    {
                                        if ( wc != null)
                                           wc.update((WeatherData) message);
                                    }
                                    else if ( message instanceof HardwareDeviceStatus )  // GET_STATUS
                                    {
                                        HardwareDeviceStatus hds = (HardwareDeviceStatus) message;

                                        if ( hds.getHardwareStatus() == Status.CONNECTED )
                                        {
                                            initStokerPage(hds.getDate() );
                                            makeCallBackRequest( new CallBackRequestType( RequestType.FORCE_DATA_PUSH ));
                                        }
                                        else if ( hds.getHardwareStatus() == Status.DISCONNECTED )
                                        {
                                            initNotConnectedPage( hds.getDate() );
                                        }

                                    }
                                    else if ( message instanceof LogEvent )
                                    {
                                       LogEvent le = (LogEvent) message;
                                       
                                       
                                       for ( CookerComponent cc : alCookers )
                                       {
                                           if ( cc.getName().compareTo(le.getCookerName()) == 0)
                                           {
                                              switch ( le.getEventType() )
                                              {
                                                 case NONE:
                                                    break;
                                                 case NEW:
                                                    cc.logAdded();
                                                    break;
                                                 case UPDATED:
                                                    break;
                                                 case DELETED:
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
                                        System.out.println("unknown message "
                                                + message);
                                    }
                                }
                                 // Draw here
                                for ( CookerComponent cc : alCookers )
                                {
                                    cc.draw();
                                }
                              }

                            });
                            client.start();
                            makeCallBackRequest( new CallBackRequestType( RequestType.GET_STATUS ));

                    }

                    public void onFailure(Throwable caught) {
                       System.out.println("Failure");
                    }
            });
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

    private void getStokerConfiguration()
    {
        stokerService.getConfiguration(new AsyncCallback<HashMap<String,SDevice>>() {

            public void onFailure(Throwable caught)
            {
                caught.printStackTrace();
                System.out.println("Client configuration failure");

            }

            public void onSuccess(HashMap<String,SDevice> result)
            {
                System.out.println("Visualization init complete.");

               // if ( alCookers.size() == 0)
                   createCookers( result, alCookers );


                for ( CookerComponent cc : alCookers)
                   cc.draw();

               // startGraphUpdateThread();
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



    private void createCookers(HashMap<String,SDevice> result, ArrayList<CookerComponent> cooker)
    {
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

       int iCooker = 0;
       for ( int i = 1; i <= iNumCookers; i++ )
       {
           CookerComponent cc = new CookerComponent(stokerService);

         //  DecoratorPanel decp = new DecoratorPanel();
         //  decp.add( cc.getPanel());
           vpCookers.add( cc );  // This needs to be done before sending the data so the graph window size is correct.
          // decp.setWidth("100%");
          // dp.setStyleName("cookers-DecoratorPanel");
         //  dp.add( cc.getPanel());

         // vpCookers.setCellWidth(cc.getPanel(), "100%");
          vpCookers.setWidth("100%");

           Iterator<SDevice> deviceIter = alDevices.iterator();
           while (deviceIter.hasNext())
           {
               SDevice sd = deviceIter.next();
               if ( sd.getCookerNum() == i)
               {
                   cc.addDevice( sd );
                   // 1.  Implement add device for cc if possible.
                   // 2.  Comment out getStokerPanels below
                   // 3.  get this working
               }
           }
           cc.init();
           cooker.add( cc );
       }

    }

    private int getNumCookers( ArrayList<SDevice> sda )
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
    }

    private void userLoggedIn( boolean b )
    {
        LoginStatus.getInstance().setLoginStatus(b);

        updateButton.setEnabled(b);
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


