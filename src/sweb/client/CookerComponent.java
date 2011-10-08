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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import sweb.client.dialog.GeneralMessageDialog;
import sweb.client.dialog.LogFileChooser;
import sweb.client.dialog.NewLogDialog;

import sweb.client.dialog.handlers.NewLogDialogHandler;
import sweb.client.dialog.handlers.LogFileChooserHandler;
import sweb.client.gauge.GaugeComponent;
import sweb.client.graph.HighChartLineGraph;
import sweb.client.graph.StokerLineGraph;

import sweb.shared.model.LogItem;
import sweb.shared.model.SBlowerDataPoint;
import sweb.shared.model.SDataPoint;
import sweb.shared.model.SDevice;
import sweb.shared.model.StokerPitSensor;
import sweb.shared.model.StokerProbe;
import sweb.shared.model.StokerDeviceTypes.DeviceType;
import sweb.shared.model.logfile.LogDir;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author gary.bak
 *
 */
public class CookerComponent extends Composite
{

    private static final String strConnectedImageURL = new String("StokerConnected.png");
    private static final String strNotConnectedImageURL = new String("StokerDisConnected.png");

    private static final String strConnectedText = "Connected";
    private static final String strNotConnectedText = "Not Connected";


    private HashMap<String,GaugeComponent> mapGuages = new HashMap<String,GaugeComponent>();
    private HashMap<String,SDevice> mapDeviceList = new HashMap<String,SDevice>();

    // List of logs that are currently active on the server.
    //private ArrayList<LogItem> arLogItems = null;
    private HashMap<String,LogItem> hmLogItems = new HashMap<String,LogItem>();


    private DecoratorPanel decPanel = new DecoratorPanel();
    private VerticalPanel outerPanel = new VerticalPanel();
    private HorizontalPanel hpStokerElements = new HorizontalPanel();
    private HorizontalPanel hpStokerHeader = new HorizontalPanel();
  //  private FlowPanel fpStokerHeader = new FlowPanel();
    private SimplePanel sGraphPanel = new SimplePanel();
   // private DecoratorPanel sGraphPanel = new DecoratorPanel();


    private Label cookerLabel = new Label("Cooker: ");
    private Label logsLabel = new Label("Logs ");
    private Label profileLabel = new Label("Profiles: ");

    private HTML statusLabel = new HTML("Status: ");
    private TextBox cookerNameLabel = new TextBox();

    private ListBox listBoxProfiles  = new ListBox();
    private ListBox logListBox = new ListBox();

    private Button newLogButton = new Button("New");
    private Button manageLogsButton = new Button("Manage");
    private Button stopLogsButton = new Button("End");
    private Button noteLogsButton = new Button("Note");
    private Button applyProfileButton = new Button("Apply Profile");

    private Image StatusImage = new Image(strConnectedImageURL);
    private HTML  StatusText = new HTML(strConnectedText);

    private String strCookerName = null;
    private String m_PitID = null;

    private int gaugePanelWidth = 0;
    private int gaugePanelHeight = 0;

    private boolean bConnected = false;
    private boolean bGraphUpdate = false;
    private Date    lastGraphUpdateDate = null;

    int m_Width = 0;
    int m_Height = 0;

  //  private StokerLineGraph graphStoker = null;
   // private HashMap<String,StokerLineGraph> listGraphStoker = new HashMap<String,StokerLineGraph>();
    StokerLineGraph graphStoker = null;

    StokerCoreServiceAsync stokerService;

    public CookerComponent(StokerCoreServiceAsync s)
    {

        stokerService = s;
        listBoxProfiles.addItem("Custom");
        listBoxProfiles.setTitle("Profiles");

        hpStokerHeader.setWidth("100%");
      //  hpStokerHeader.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
      //  hpStokerHeader.setBorderWidth(1);

        cookerLabel.setStyleName("label-Cooker");
        cookerNameLabel.setStyleName("label-CookerName");

        FlexTable ftCookerType = new FlexTable();
        ftCookerType.setStyleName("panel-CookerType");
        ftCookerType.setWidget( 0,0, cookerLabel);
        ftCookerType.setWidget( 0,1, cookerNameLabel );
        ftCookerType.setWidget( 1,0, statusLabel );
       // ftCookerType.setWidget( 1,1, StatusImage );
        ftCookerType.setWidget( 1,1, StatusText );


        statusLabel.setStyleName("label-Status");

        hpStokerHeader.add( ftCookerType );

        HorizontalPanel hpProfiles = new HorizontalPanel();
    //    hpProfiles.add( profileLabel );
        //profileLabel.setStyleName("label-Cooker");
        //profileLabel.setSize(", height)
        hpProfiles.setCellHorizontalAlignment( profileLabel , HasHorizontalAlignment.ALIGN_RIGHT);
    //    hpProfiles.add( listBoxProfiles );
        hpProfiles.setCellHorizontalAlignment( listBoxProfiles , HasHorizontalAlignment.ALIGN_LEFT);
        hpProfiles.setCellVerticalAlignment( listBoxProfiles , HasVerticalAlignment.ALIGN_MIDDLE);
  //      hpProfiles.add( applyProfileButton );

        hpStokerHeader.add(  hpProfiles );
        hpStokerHeader.setCellHorizontalAlignment( hpProfiles , HasHorizontalAlignment.ALIGN_LEFT);
        hpStokerHeader.setCellVerticalAlignment( hpProfiles , HasVerticalAlignment.ALIGN_BOTTOM );

        newLogButton.setStyleName("buttons-log");
        manageLogsButton.setStyleName("buttons-log");
        stopLogsButton.setStyleName("buttons-log");
        noteLogsButton.setStyleName("buttons-log");

        // Logs
        FlexTable ftLogs = new FlexTable();
        FlexCellFormatter cellFormatter = ftLogs.getFlexCellFormatter();

        ftLogs.setWidth("100%");
        ftLogs.setWidget(0, 0, noteLogsButton );
        ftLogs.setWidget(0, 1, manageLogsButton );
        ftLogs.setWidget(0, 2, newLogButton );
        ftLogs.setWidget(0, 3, stopLogsButton );
        ftLogs.setWidget(1, 0, logListBox );
        logListBox.setSize("200px", "25px");
        cellFormatter.setColSpan(1, 0, 4);
        DecoratorPanel dpLogs = new DecoratorPanel();
        dpLogs.add( ftLogs );

     // Hide based on login Status
        newLogButton.setVisible(LoginStatus.getInstance().getLoginStatus());
        manageLogsButton.setVisible(LoginStatus.getInstance().getLoginStatus());
        stopLogsButton.setVisible(LoginStatus.getInstance().getLoginStatus());
        noteLogsButton.setVisible(LoginStatus.getInstance().getLoginStatus());

        //noteLogsButton.setEnabled(false);
        HorizontalPanel hpLogs = new HorizontalPanel();

        hpLogs.add( logsLabel );
        hpLogs.setSize("100%", "100%");
        hpLogs.setCellHorizontalAlignment( logsLabel , HasHorizontalAlignment.ALIGN_RIGHT);
        hpLogs.setCellVerticalAlignment( logsLabel , HasVerticalAlignment.ALIGN_TOP );
        logsLabel.setStyleName("label-Logs");

       // hpStokerHeader.add( hpLogs );
        hpLogs.add( dpLogs );

        hpLogs.setCellHorizontalAlignment( dpLogs , HasHorizontalAlignment.ALIGN_RIGHT);
        hpLogs.setCellVerticalAlignment( dpLogs , HasVerticalAlignment.ALIGN_BOTTOM );

        hpStokerHeader.add( hpLogs );
        hpStokerHeader.setCellHorizontalAlignment( hpLogs , HasHorizontalAlignment.ALIGN_RIGHT);
        hpStokerHeader.setCellVerticalAlignment( hpLogs , HasVerticalAlignment.ALIGN_BOTTOM );

       // hpStokerHeader.setBorderWidth(3);
        outerPanel.setWidth("100%");
        outerPanel.setHeight("100%");
        outerPanel.add( hpStokerHeader);

      //  hpStokerElements.setBorderWidth(2);
        hpStokerElements.setWidth("100%");

        hpStokerElements.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        outerPanel.add(hpStokerElements);
       // outerPanel.setBorderWidth(2);

        decPanel.setWidth("100%");

     //   decPanel.setHeight("100%");
        decPanel.add(outerPanel);

       // decPanel.add( cookerLabel);

        // Handlers
        logListBox.addChangeHandler(listBoxChangeHandler());
        stopLogsButton.addClickHandler(stopLogsButtonClickHandler());
        newLogButton.addClickHandler(newLogButtonClickHandler());

        manageLogsButton.addClickHandler(manageLogsClickHandler());

        initWidget(decPanel);


    }

    private ChangeHandler listBoxChangeHandler()
    {
        ChangeHandler ch = new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                final int iSelectedIndex = logListBox.getSelectedIndex();
                String strLogName = logListBox.getItemText(iSelectedIndex);

                addGraph();
             //   sGraphPanel.remove(sGraphPanel.getWidget());
              //  sGraphPanel.add(graphStoker);

                // problem here.  Adding graphStoker back
            }

        };

        return ch;

    }

    private ClickHandler manageLogsClickHandler()
    {
        return new ClickHandler() {

            public void onClick(ClickEvent event)
            {
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
                                   final int iSelectedIndex = logListBox.getSelectedIndex();
                                   final String strLogName = logListBox.getItemText(iSelectedIndex);

                                   stokerService.attachToExistingLog( strCookerName, strLogName, st, new AsyncCallback<Integer>() {

                                    public void onFailure(Throwable caught)
                                    {
                                        System.out.println("Failed to attach to existing Log");

                                    }

                                    public void onSuccess(Integer result)
                                    {
                                        if ( result.intValue() == 1 )
                                        {
                                            new GeneralMessageDialog( "Success", "Attached to log").center();
                                            refreshGraphData(strLogName);
                                        }

                                    }


                                   });
                                  // TODO: create a resumeLog method and pass result to it
                                   // The config must be checked at some point to make sure they match
                                   // Also, the currently selected log needs to be passed in.
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

    private ClickHandler newLogButtonClickHandler()
    {
        return new ClickHandler() {

            public void onClick(ClickEvent event)
            {

                new NewLogDialog( new ArrayList<SDevice>(mapDeviceList.values()), new NewLogDialogHandler() {

                    public void onReturn(String sLogName,
                            ArrayList<SDevice> arSD)
                    {
                        createNewLog( sLogName, arSD );
                    }
                }).show();
            }
        };
    }

    private ClickHandler stopLogsButtonClickHandler()
    {
       ClickHandler cl = new ClickHandler() {

        public void onClick(ClickEvent event)
        {
            final int iSelectedIndex = logListBox.getSelectedIndex();
            String strLogName = logListBox.getItemText(iSelectedIndex);
            if( strLogName.compareTo("Default") == 0)
            {
                return;
            }
            stokerService.stopLog(strCookerName, strLogName, new AsyncCallback<Integer>() {

                public void onFailure(Throwable caught)
                {
                    // TODO Auto-generated method stub

                }

                public void onSuccess(Integer result)
                {
                    String strLogName = logListBox.getItemText(iSelectedIndex);
                    logListBox.removeItem(iSelectedIndex);

                    addGraph();
                    //sGraphPanel.remove(sGraphPanel.getWidget());
                    //sGraphPanel.add(graphStoker);
                }
            });
        }

       };
       return cl;
    }

    public void addDevice( SDevice sd1 )
    {
        if ( sd1.isProbe())
        {
           GaugeComponent gc = new GaugeComponent((StokerProbe)sd1) ;
           mapGuages.put(sd1.getID(),gc);
           hpStokerElements.add( gc );
           hpStokerElements.setCellHorizontalAlignment(gc, HasHorizontalAlignment.ALIGN_LEFT);
           gaugePanelWidth = gaugePanelWidth +  gc.getOffsetWidth();

           mapDeviceList.put( sd1.getID(), sd1);
           if ( sd1.getProbeType() == DeviceType.PIT )
           {
               m_PitID = sd1.getID();
               strCookerName = sd1.getCookerName();
           }
        }
        else
        {
            mapDeviceList.put( sd1.getID(), sd1);

        }
    }

    public void init()
    {
        System.out.println("Height is: " + hpStokerElements.getOffsetHeight());
        System.out.println("Width is: " + hpStokerElements.getOffsetWidth());

        m_Width = hpStokerElements.getOffsetWidth() - gaugePanelWidth - 5;
        m_Height = hpStokerElements.getOffsetHeight();

        cookerNameLabel.setText("Large Egg");

        sGraphPanel.setWidth(new Integer(m_Width).toString() + "px");
        sGraphPanel.setHeight(new Integer(m_Height).toString() + "px");
        DecoratorPanel dp = new DecoratorPanel();
        dp.add( sGraphPanel);
         hpStokerElements.add( dp );

        getActiveLogListFromServer();

    }

    private void addGraph()
    {
        String strLogName = logListBox.getItemText(logListBox.getSelectedIndex());

     // graphStoker = new StokerLineGraph(Width, gaugePanelHeight, mapDeviceList);
        graphStoker = new HighChartLineGraph(m_Width, m_Height, hmLogItems.get( strLogName ).getLogItems());
     //   graphStoker = new HighstockLineGraph(m_Width, m_Height, hmLogItems.get( strLogName ).getLogItems());


        refreshGraphData(strLogName);

        Widget w = sGraphPanel.getWidget();
        if ( w != null )
           sGraphPanel.remove(w);
        sGraphPanel.add(graphStoker);

    }

    private void createNewLog( String newLogName, ArrayList<SDevice> arSD )
    {
        final String strFinalLogName = newLogName;

        stokerService.startLog( strCookerName, newLogName, arSD, new AsyncCallback<Integer>() {

            public void onFailure(Throwable caught)
            {
                // TODO Auto-generated method stub

            }

            public void onSuccess(Integer result)
            {
                if ( result.intValue() == 1)
                {
                    // TODO: this is inefficient, we are fetching data we may already have

                    //logListBox.addItem( strFinalLogName );
                    getActiveLogListFromServer();
                   /* int Width = hpStokerElements.getOffsetWidth() - gaugePanelWidth - 5;
                    int Height = hpStokerElements.getOffsetHeight();
                    StokerLineGraph graphStoker = new HighChartLineGraph(Width, Height, mapDeviceList);
                    listGraphStoker.put( "Default", graphStoker );

                    sGraphPanel.remove(sGraphPanel.getWidget());
                    sGraphPanel.add(graphStoker);
                    */

                }
            }
        });
    }

    private void setConnectedImage()
    {
        if ( bConnected == true) {
            //StatusImage.setUrl(strConnectedImageURL);
            StatusText.setText(strConnectedText);
            StatusText.setStyleName("ConnectedText");
        }
        else {
            //StatusImage.setUrl(strNotConnectedImageURL);
            StatusText.setText(strNotConnectedText);
            StatusText.setStyleName("NotConnectedText");
        }

    }
    public void setConnected( boolean b )
    {
        if ( bConnected != b )
        {
            bConnected = b;

            if ( b == false )
               updateGauges( null );

            setConnectedImage();
        }
    }

    private void getActiveLogListFromServer()
    {
        stokerService.getLogList(new AsyncCallback<ArrayList<LogItem>>() {

            public void onFailure(Throwable caught)
            {
                caught.printStackTrace();
                System.out.println("Failed to retreive LogList");

            }

            public void onSuccess(ArrayList<LogItem> result)
            {
                updateLogList(result);
                addGraph();
            }

        });
    }

    /**
     * Update the list of logs that are currently being run.
     *
     * @param li
     */
    private void updateLogList( ArrayList<LogItem> li)
    {
        logListBox.clear();
        for ( LogItem l : li )
        {
            if ( l.getCookerName().compareTo(strCookerName) == 0)
            {
               hmLogItems.put( l.getLogName(), l );
               logListBox.addItem(l.getLogName());

            }
        }

    }

    public void updateGauges( SDataPoint sdp )
    {

        bGraphUpdate = false;
        if ( sdp != null)
        {
            setConnected(true);
            try
            {
                if ( sdp instanceof SBlowerDataPoint )
                {
                    mapGuages.get(m_PitID).updateData(sdp);
                }
                else
                {
                    mapGuages.get(sdp.getDeviceID()).updateData(sdp);
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            for ( GaugeComponent gc : mapGuages.values() )
            {
                gc.updateCurrentTemp(0);
            }
        }
    }

    private void updateGraph( ArrayList<SDataPoint> arSDP )
    {
        setConnected(true);
        bGraphUpdate = true;
        graphStoker.addData( arSDP );

    }

    public void updateGraph( SDataPoint sdp, boolean refresh)
    {
        setConnected(true);
        bGraphUpdate = true;
        if ( existsInActiveLog( sdp ))
           graphStoker.addData( sdp, refresh );

    }

    /**
     * Calls server and gets all data points for the requested graph
     * @param logName Log name for which to get the data points for
     */
    public void refreshGraphData( String logName )
    {
        stokerService.getAllGraphDataPoints( logName, new AsyncCallback<ArrayList<ArrayList<SDataPoint>>>() {
            public void onFailure(Throwable caught)
             {
                 caught.printStackTrace();
                 System.out.println("refreshGraphData failure");

             }

             public void onSuccess(ArrayList<ArrayList<SDataPoint>> result)
             {
                // graphStoker.clearDataTable();
                 for ( ArrayList<SDataPoint> arData : result)
                 {
                     updateGraph( arData);

                 }
                 graphStoker.redraw();

               //  draw();

             }
         });
    }

    /**
     * Checks to see if the data point passed in exists in the actively
     * selected log.  This is necessary so that only the data points which
     * are included in the log show up on the graph, since all data points
     * are passed back in the comet stream.
     *
     * @param sdp Data point to check if it is included in active log
     * @return true if datapoint exists in the actively selected log
     *         false if not
     */
    private boolean existsInActiveLog( SDataPoint sdp)
    {
        String strSelectedLog = logListBox.getItemText(logListBox.getSelectedIndex());
        LogItem li = hmLogItems.get( strSelectedLog );

        if ( li == null )
        {
            System.out.println("Error in existsInActiveLog: Selected log [" + strSelectedLog + "] not found in map");
            return false;
        }
        for ( SDevice sd : li.getLogItems())
        {
            if ( sd.getID().compareTo(sdp.getDeviceID()) == 0 )
            {
                return true;
            }
        }
        return false;
    }

    public void draw()
    {
        for (GaugeComponent gc : mapGuages.values())
        {
           gc.draw();
        }
        if ( bGraphUpdate )
        {
          // graphStoker.draw();
           bGraphUpdate = false;
        }
    }

    public void loginEvent()
    {
        newLogButton.setVisible(LoginStatus.getInstance().getLoginStatus());
        manageLogsButton.setVisible(LoginStatus.getInstance().getLoginStatus());
        stopLogsButton.setVisible(LoginStatus.getInstance().getLoginStatus());
        noteLogsButton.setVisible(LoginStatus.getInstance().getLoginStatus());

       // graphStoker.loginEvent();
        for (GaugeComponent gc : mapGuages.values())
        {
           gc.loginEvent();
        }
        draw();
        //LoginStatus.getInstance().getLoginStatus()
    }

    public ArrayList<SDevice> getConfigUpdates()
    {
        ArrayList<SDevice> alDevList = new ArrayList<SDevice>();
        for (GaugeComponent gc : mapGuages.values())
        {
            SDevice s = gc.getConfigUpdates();
            if ( s != null)
              alDevList.add( s );
        }
        return alDevList;
    }
}

