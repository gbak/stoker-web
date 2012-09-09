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
import java.util.Date;
import java.util.HashMap;

import com.allen_sauer.gwt.log.client.Log;

import sweb.client.dialog.AlertsSettingsDialog;
import sweb.client.dialog.GeneralMessageDialog;
import sweb.client.dialog.LogFileChooser;
import sweb.client.dialog.NewLogDialog;
import sweb.client.dialog.NewNoteDialog;
import sweb.client.dialog.handlers.AlertsSettingsDialogHandler;
import sweb.client.dialog.handlers.LogFileChooserHandler;
import sweb.client.dialog.handlers.NewLogDialogHandler;
import sweb.client.dialog.handlers.NewNoteDialogHandler;
import sweb.client.gauge.ProbeComponent;
import sweb.client.gauge.ProbeComponent.Alignment;
import sweb.client.graph.HighChartLineGraph;
import sweb.client.graph.StokerLineGraph;
import sweb.shared.model.Cooker;
import sweb.shared.model.CookerHelper;
import sweb.shared.model.LogItem;
import sweb.shared.model.alerts.AlertModel;
import sweb.shared.model.data.SBlowerDataPoint;
import sweb.shared.model.data.SDataPoint;
import sweb.shared.model.devices.SDevice;
import sweb.shared.model.devices.stoker.StokerProbe;
import sweb.shared.model.logfile.LogDir;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
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

   // private static final String strConnectedImageURL = new String("StokerConnected.png");
   // private static final String strNotConnectedImageURL = new String("StokerDisConnected.png");

    private static final String strConnectedText = "Connected";
    private static final String strNotConnectedText = "Not Connected";

    private HashMap<String,ProbeComponent> m_guageMap = new HashMap<String,ProbeComponent>();

    // List of logs that are currently active on the server.
    //private ArrayList<LogItem> arLogItems = null;
    private HashMap<String,LogItem> m_activeLogItemsMap = new HashMap<String,LogItem>();


    private FlowPanel m_stokerElementsPanel = new FlowPanel();
    private SimplePanel m_graphPanel = new SimplePanel();
    private VerticalPanel m_outerVPanel = new VerticalPanel();
    private DecoratorPanel m_outerMostDecPanel = new DecoratorPanel();
    private DisclosurePanel m_graphDisclosurePanel = null;
    private HorizontalPanel m_StokerHeaderPanel = new HorizontalPanel();

    //  private HTMLPanel sGraphPanel = new HTMLPanel("<div class=\"content\" style=\"width: 100%; height: 400px; position: absolute; overflow: hidden \" id=\"graph\"></div>");
     
  
   
    private Label m_cookerLabel = new Label("Cooker: ");
    private Label m_logsLabel = new Label("Logs ");
    private Label m_profileLabel = new Label("Profiles: ");

    private HTML m_statusLabel = new HTML("Status: ");
    private TextBox m_cookerNameLabel = new TextBox();

    private ListBox m_listBoxProfiles  = new ListBox();
    private ListBox m_logListBox = new ListBox();

    private Button m_newLogButton = new Button("New");
    private Button m_manageLogsButton = new Button("Manage");
    private Button m_stopLogsButton = new Button("End");
    private Button m_noteLogsButton = new Button("Note");
    private Button m_applyProfileButton = new Button("Apply Profile");
    private Button m_alertsButton = new Button("Alerts");

    private HTML  m_statusText = new HTML(strConnectedText);

    private Cooker m_cooker = null;

    private int m_gaugePanelWidth = 0;
    private int m_gaugePanelHeight = 0;

    private boolean m_connected = false;
    private boolean m_graphUpdate = false;
    private Date    m_lastGraphUpdateDate = null;

    HashMap<String,String> m_properties = null;
    
    int m_Width = 0;
    int m_Height = 0;
    
    Alignment m_alignment = Alignment.SINGLE;
    
    StokerLineGraph m_graphStoker = null;

    StokerCoreServiceAsync m_stokerService;

    public CookerComponent(StokerCoreServiceAsync s, HashMap<String,String> p)
    {

        m_stokerService = s;
        m_properties = p;
        m_listBoxProfiles.addItem("Custom");
        m_listBoxProfiles.setTitle("Profiles");

        m_StokerHeaderPanel.setWidth("100%");

        m_cookerLabel.setStyleName("label-Cooker");
        m_cookerNameLabel.setStyleName("label-CookerName");

        FlexTable ftCookerType = new FlexTable();
        ftCookerType.setStyleName("panel-CookerType");
        ftCookerType.setWidget( 0,0, m_cookerLabel);
        ftCookerType.setWidget( 0,1, m_cookerNameLabel );
      //  ftCookerType.setWidget( 1,0, statusLabel );
      //  ftCookerType.setWidget( 1,1, StatusText );

      //  statusLabel.setStyleName("label-Status");

        m_StokerHeaderPanel.add( ftCookerType );

        HorizontalPanel hpProfiles = new HorizontalPanel();
    //    hpProfiles.add( profileLabel );
        //profileLabel.setStyleName("label-Cooker");
        //profileLabel.setSize(", height)
        hpProfiles.setCellHorizontalAlignment( m_profileLabel , HasHorizontalAlignment.ALIGN_RIGHT);
    //    hpProfiles.add( listBoxProfiles );
        hpProfiles.setCellHorizontalAlignment( m_listBoxProfiles , HasHorizontalAlignment.ALIGN_LEFT);
        hpProfiles.setCellVerticalAlignment( m_listBoxProfiles , HasVerticalAlignment.ALIGN_MIDDLE);
  //      hpProfiles.add( applyProfileButton );

        m_StokerHeaderPanel.add(  hpProfiles );
        m_StokerHeaderPanel.setCellHorizontalAlignment( hpProfiles , HasHorizontalAlignment.ALIGN_LEFT);
        m_StokerHeaderPanel.setCellVerticalAlignment( hpProfiles , HasVerticalAlignment.ALIGN_BOTTOM );

        /* old style - 'buttons-log' */
        m_newLogButton.setStyleName("sweb-LogButton");
        m_manageLogsButton.setStyleName("sweb-LogButton");
        m_stopLogsButton.setStyleName("sweb-LogButton");
        m_noteLogsButton.setStyleName("sweb-LogButton");

        // Logs
        FlexTable ftLogs = new FlexTable();
        FlexCellFormatter cellFormatter = ftLogs.getFlexCellFormatter();

        /*ftLogs.setWidth("100%");
        ftLogs.setWidget(0, 0, noteLogsButton );
        ftLogs.setWidget(0, 1, manageLogsButton );
        ftLogs.setWidget(0, 2, newLogButton );
        ftLogs.setWidget(0, 3, stopLogsButton );
        ftLogs.setWidget(1, 0, logListBox );*/
        ftLogs.setWidth("100%");
        ftLogs.setWidget(0, 0, m_noteLogsButton );
        ftLogs.setWidget(0, 1, m_manageLogsButton );
        ftLogs.setWidget(0, 2, m_newLogButton );
        ftLogs.setWidget(0, 3, m_stopLogsButton );
        ftLogs.setWidget(0, 4, m_logListBox );
        m_logListBox.setSize("200px", "25px");
      //  cellFormatter.setColSpan(1, 0, 4);
        DecoratorPanel dpLogs = new DecoratorPanel();
        dpLogs.addStyleName("sweb-LogsDecorator");
        dpLogs.add( ftLogs );

     // Hide based on login Status
        m_newLogButton.setVisible(LoginStatus.getInstance().getLoginStatus());
        m_manageLogsButton.setVisible(LoginStatus.getInstance().getLoginStatus());
        m_stopLogsButton.setVisible(LoginStatus.getInstance().getLoginStatus());
        m_noteLogsButton.setVisible(LoginStatus.getInstance().getLoginStatus());
        m_alertsButton.setVisible(LoginStatus.getInstance().getLoginStatus());

        //noteLogsButton.setEnabled(false);
        HorizontalPanel hpLogs = new HorizontalPanel();

      //  hpLogs.add( logsLabel );
        hpLogs.setSize("100%", "100%");
        hpLogs.setCellHorizontalAlignment( m_logsLabel , HasHorizontalAlignment.ALIGN_RIGHT);
        hpLogs.setCellVerticalAlignment( m_logsLabel , HasVerticalAlignment.ALIGN_TOP );
        m_logsLabel.setStyleName("label-Logs");

       // hpStokerHeader.add( hpLogs );
        hpLogs.add( dpLogs );

        hpLogs.setCellHorizontalAlignment( dpLogs , HasHorizontalAlignment.ALIGN_RIGHT);
        hpLogs.setCellVerticalAlignment( dpLogs , HasVerticalAlignment.ALIGN_BOTTOM );

        m_alertsButton.setStyleName("sweb-LogButton");
        m_alertsButton.addClickHandler( alertsButtonClickHandler());
        m_StokerHeaderPanel.add( m_alertsButton );
        m_StokerHeaderPanel.add( hpLogs );
        m_StokerHeaderPanel.setCellHorizontalAlignment( hpLogs , HasHorizontalAlignment.ALIGN_RIGHT);
        m_StokerHeaderPanel.setCellVerticalAlignment( hpLogs , HasVerticalAlignment.ALIGN_BOTTOM );

        m_outerVPanel.setWidth("100%");
     //   outerPanel.setHeight("100%");
        m_outerVPanel.add( m_StokerHeaderPanel);

       // hpStokerElements.setWidth("100%");
        
     
      //  hpStokerElements.setWidth(new Integer(Window.getClientWidth() - 40).toString() + "px");

        
      //  hpStokerElements.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        m_outerVPanel.add(m_stokerElementsPanel);
       // outerPanel.setBorderWidth(2);

        m_outerMostDecPanel.setWidth("100%");
     //   decPanel.setHeight("100%");
        
        m_outerMostDecPanel.add(m_outerVPanel);

        m_outerMostDecPanel.addStyleName("sweb-LogsDecorator");
       // decPanel.add( cookerLabel);

        // Handlers
        m_logListBox.addChangeHandler(listBoxChangeHandler());
        m_stopLogsButton.addClickHandler(stopLogsButtonClickHandler());
        m_newLogButton.addClickHandler(newLogButtonClickHandler());
        m_noteLogsButton.addClickHandler(newNoteButtonClickHandler());
        m_manageLogsButton.addClickHandler(manageLogsClickHandler());

        initWidget(m_outerMostDecPanel);


    }

    private ClickHandler alertsButtonClickHandler()
    {
        return new ClickHandler() {

            public void onClick(ClickEvent event)
            {
                m_stokerService.getAlertConfiguration(new AsyncCallback<ArrayList<AlertModel>>() {

                    public void onFailure(Throwable caught)
                    {
                        // TODO Auto-generated method stub
                    }

                    public void onSuccess(ArrayList<AlertModel> result)
                    {
                        new AlertsSettingsDialog(m_stokerService, result, new AlertsSettingsDialogHandler() {

                            public void onReturn(ArrayList<AlertModel> alertBaseList)
                            {
                               // TODO Pass Alert changes back to server
                               
                            }
                          }).center();
                    }
                });
                // Get Alert settings from server
                // Call dialog
                
/*                new AlertsSettingsDialog(stokerService, new AlertsDialogHandler() {

                  public void onReturn(ArrayList<AlertBase> alertBaseList)
                  {
                     // TODO Auto-generated method stub
                     
                  }

                }).center();
*/                
            }
        };
    }
    
    private ChangeHandler listBoxChangeHandler()
    {
        ChangeHandler ch = new ChangeHandler()
        {
            public void onChange(ChangeEvent event)
            {
                final int iSelectedIndex = m_logListBox.getSelectedIndex();
                String strLogName = m_logListBox.getItemText(iSelectedIndex);
                addGraph();
             }
        };
        return ch;
    }

    private ClickHandler manageLogsClickHandler()
    {
        return new ClickHandler() {

            public void onClick(ClickEvent event)
            {
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
                                   final int iSelectedIndex = m_logListBox.getSelectedIndex();
                                   final String strLogName = m_logListBox.getItemText(iSelectedIndex);

                                   m_stokerService.attachToExistingLog( m_cooker.getCookerName(), strLogName, st, new AsyncCallback<Integer>() {

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
                new NewLogDialog( CookerHelper.getDeviceList(m_cooker), new NewLogDialogHandler() {

                    public void onReturn(String sLogName,
                            ArrayList<SDevice> arSD)
                    {
                        createNewLog( sLogName, arSD );
                    }
                }).show();
                
            }
        };
    }

    private ClickHandler newNoteButtonClickHandler()
    {
        return new ClickHandler() {

            public void onClick(ClickEvent event)
            {
              
               ArrayList<String> logList = new ArrayList<String>();
               
               for ( int i = 0; i < m_logListBox.getItemCount(); i++ )
               {
                  logList.add( m_logListBox.getItemText(i));   
               }
               
                new NewNoteDialog( logList, m_logListBox.getItemText(m_logListBox.getSelectedIndex()), new NewNoteDialogHandler() {

                    public void onReturn(String note,
                            ArrayList<String> notedLogs)
                    {
                       //TODO: Log
                        System.out.println("Note from Dialog: " + note );
                        m_stokerService.addNoteToLog(note, notedLogs, new AsyncCallback<Integer>() {

                            public void onFailure(Throwable caught)
                            {
                                // TODO Auto-generated method stub

                            }

                            public void onSuccess(Integer result)
                            {
                               
                               // TODO: This is not correct.
                              //  String strLogName = logListBox.getItemText(iSelectedIndex);
                              //  logListBox.removeItem(iSelectedIndex);

                              //  addGraph();
                               
                            }
                        });
                    }
                }).center();
                
            }
        };
    }
    public void removeLog(String logName)
    {
       int size = m_logListBox.getItemCount();
       for ( int i = 0; i < size; i++ )
       {
          if ( m_logListBox.getItemText(i).compareTo(logName) == 0)
          {
             m_logListBox.removeItem(i);
             break;
          }
       }
       addGraph();
    }
    
    private ClickHandler stopLogsButtonClickHandler()
    {
       ClickHandler cl = new ClickHandler() {

        public void onClick(ClickEvent event)
        {
            final int iSelectedIndex = m_logListBox.getSelectedIndex();
            final String strLogName = m_logListBox.getItemText(iSelectedIndex);
            if( strLogName.compareTo("Default") == 0)
            {
                return;
            }
            m_stokerService.stopLog(m_cooker.getCookerName(), strLogName, new AsyncCallback<Integer>() {

                public void onFailure(Throwable caught)
                {
                    // TODO Auto-generated method stub

                }

                public void onSuccess(Integer result)
                {
                    
                    Window.open(GWT.getModuleBaseURL()+ "report" + "?logName=" + strLogName, "_blank", "enabled");
                }
            });
        }

       };
       return cl;
    }


    private void addComponents( Cooker c )
    {

        StokerProbe pit = (StokerProbe) c.getPitSensor();
        
        if ( pit != null )
        {
            ProbeComponent gc = new ProbeComponent( pit, m_alignment, m_properties );
            m_guageMap.put( pit.getID(), gc );
            gc.addStyleName("sweb-gaugeFlowPanel");
            m_stokerElementsPanel.add( gc );
            m_gaugePanelWidth = m_gaugePanelWidth +  gc.getOffsetWidth();
        }
        for ( StokerProbe probe : c.getProbeList())
        {
            ProbeComponent gcp = new ProbeComponent( probe, m_alignment, m_properties );
            m_guageMap.put( probe.getID(), gcp );
            gcp.addStyleName("sweb-gaugeFlowPanel");
            m_stokerElementsPanel.add( gcp );
            m_gaugePanelWidth = m_gaugePanelWidth +  gcp.getOffsetWidth();
        
        }
        
    }
  

    public void setOrientation(Alignment a )
    {
        m_alignment = a;
    }
    
    public String getName()
    {
       return m_cooker.getCookerName();
    }
    
    public void calculateGraphPanelSize()
    {
        
    }
    
    public void init(Cooker c)
    {
        if ( m_cooker != null )
        {
            System.out.println("Cooker init() already called.");
            return;
        }
        
        addComponents(c);
        
        System.out.println("Height is: " + m_stokerElementsPanel.getOffsetHeight());
        System.out.println("Width is: " + m_stokerElementsPanel.getOffsetWidth());
        
        m_cooker = c;
        
        m_cookerNameLabel.setText(m_cooker.getCookerName());

    //    sGraphPanel.setHeight(new Integer(m_Height).toString() + "px");
        final DecoratorPanel dpGraph = new DecoratorPanel();
        
       // dpGraph.addStyleName("sweb-graphPanel");

        m_Height = 385;
        if ( m_alignment == Alignment.SINGLE)
        {
            m_Width = m_stokerElementsPanel.getOffsetWidth() - m_gaugePanelWidth - 15;
            
            m_graphPanel.setWidth(m_Width + "px");
          //  sGraphPanel.setWidth("100%");
            m_graphPanel.setHeight(m_Height + "px");

           dpGraph.add( m_graphPanel);
           m_stokerElementsPanel.add( dpGraph );
        }
        else
        {
            m_Height = 325;
            m_Width = m_stokerElementsPanel.getOffsetWidth()- 50;

            m_graphPanel.setWidth(new Integer(m_Width).toString() + "px");

            m_graphPanel.setHeight( m_Height + "px");
            m_graphDisclosurePanel = new DisclosurePanel("Graph");
        //    dp.setWidth(new Integer(hpStokerElements.getOffsetWidth() - 10).toString() + "px");
          //  dp.setWidth(new Integer(m_Width).toString() + "px");
            
            // We can set 100% here because collapsed, 100% is quite small.
            m_graphDisclosurePanel.setWidth(new Integer(m_Width - 5).toString() + "px");
            m_graphDisclosurePanel.setVisible(true);
            m_graphDisclosurePanel.setContent( m_graphPanel );
            m_graphDisclosurePanel.setAnimationEnabled(true);
            m_graphDisclosurePanel.addOpenHandler(new OpenHandler<DisclosurePanel>() {

                @Override
                public void onOpen(OpenEvent<DisclosurePanel> event)
                {
                    m_Width = m_outerMostDecPanel.getOffsetWidth() - 50;
                    m_graphPanel.setWidth(new Integer(m_Width).toString() + "px");
                    m_graphDisclosurePanel.setWidth(new Integer(m_Width - 5).toString() + "px");
                    m_graphStoker.setPixelSize(m_Width-5, m_Height);
                    m_graphStoker.setNewSize(m_Width - 5, m_Height );

                }
                
            });
            dpGraph.add( m_graphDisclosurePanel );
           m_outerVPanel.add( m_graphDisclosurePanel );
        }
        
        
        getActiveLogListFromServer();
        

    }

    public void logAdded()
    {
       getActiveLogListFromServer();
    }
    
    private void addGraph() 
    {
        int index = m_logListBox.getSelectedIndex();
        if ( index < 0 )
            return;   // not ready for data points yet.
                
        final String strLogName = m_logListBox.getItemText(index);

        
        m_stokerService.getAllGraphDataPoints( strLogName, new AsyncCallback<ArrayList<ArrayList<SDataPoint>>>() {
            public void onFailure(Throwable caught)
             {
                 caught.printStackTrace();
                 System.out.println("refreshGraphData failure");

             }

             public void onSuccess(ArrayList<ArrayList<SDataPoint>> result)
             {

                 m_graphStoker = new HighChartLineGraph(m_Width, m_Height, m_activeLogItemsMap.get( strLogName ).getLogItems(), result);

                 Window.addResizeHandler(new ResizeHandler()
                 {
                    public void onResize( ResizeEvent event )
                    {
                     //   System.out.println("Window Width: " + Window.getClientWidth());
                     //   System.out.println("Event Width: " + event.getWidth());
                     //   System.out.println("Width: " + m_outerMostDecPanel.getOffsetWidth());
                     //   m_stokerElementsPanel.setWidth(event.getWidth() - 20 + "px");
                     //   m_outerMostDecPanel.setWidth(Window.getClientWidth() - 10 + "px");
                        
                        if ( m_alignment == Alignment.SINGLE)
                        {
                           //m_Width = m_stokerElementsPanel.getOffsetWidth() - m_gaugePanelWidth - 10;
                           // m_Width = event.getWidth() - m_gaugePanelWidth - 20;
                            m_Width = m_outerMostDecPanel.getOffsetWidth() - m_gaugePanelWidth - 20;
                            m_graphPanel.setWidth(new Integer(m_Width).toString() + "px");
                           
                            
                        }
                        else
                        {
                            m_Height = 325;
                           // m_Width = m_stokerElementsPanel.getOffsetWidth()- 20;
                            m_Width = m_outerMostDecPanel.getOffsetWidth() - 50;
                            m_graphPanel.setWidth(new Integer(m_Width).toString() + "px");
                           m_graphDisclosurePanel.setWidth(new Integer(m_Width - 5).toString() + "px");
                       //    
                        }
                         
                        m_graphStoker.setPixelSize(m_Width-5, m_Height);
                       
                       m_graphStoker.setNewSize(m_Width - 5, m_Height );

                      // c.redraw();
                    }
                 });
                 

         //        refreshGraphData(strLogName);

                 Widget w = m_graphPanel.getWidget();
                 if ( w != null )
                    m_graphPanel.remove(w);
                 
                 m_graphPanel.add(m_graphStoker);

             }
         });
    }
    /*private void addGraph()
    {
        int index = logListBox.getSelectedIndex();
        if ( index < 0 )
            return;   // not ready for data points yet.
                
        String strLogName = logListBox.getItemText(index);

     // graphStoker = new StokerLineGraph(Width, gaugePanelHeight, mapDeviceList);
        graphStoker = new HighChartLineGraph(m_Width, m_Height, hmLogItems.get( strLogName ).getLogItems());
     //   graphStoker = new HighstockLineGraph(m_Width, m_Height, hmLogItems.get( strLogName ).getLogItems());

        Window.addResizeHandler(new ResizeHandler()
        {
           public void onResize( ResizeEvent event )
           {
               hpStokerElements.setWidth(event.getWidth() - 20 + "px");
               if ( alignment == Alignment.SINGLE)
               {
                   m_Width = hpStokerElements.getOffsetWidth() - m_gaugePanelWidth - 10;
                   sGraphPanel.setWidth(new Integer(m_Width).toString() + "px");
               }
               else
               {
                   m_Height = 325;
                   m_Width = hpStokerElements.getOffsetWidth()- 20;
                   sGraphPanel.setWidth(new Integer(m_Width).toString() + "px");
               }
                
               graphStoker.setPixelSize(m_Width-5, m_Height);
              
              graphStoker.setNewSize(m_Width - 5, m_Height );

             // c.redraw();
           }
        });
        

        refreshGraphData(strLogName);

        Widget w = sGraphPanel.getWidget();
        if ( w != null )
           sGraphPanel.remove(w);
        
        sGraphPanel.add(graphStoker);

    }*/

    /** 
     * Creates new log with associated probe devices.  Once server has created the log, the 
     * getActiveLogListFromServer method is called to update the log combo and update the graph.
     * 
     * @param newLogName Name of the new log
     * @param deviceList Array List of probes that the log will track.
     */
    private void createNewLog( String newLogName, ArrayList<SDevice> deviceList )
    {
        final String strFinalLogName = newLogName;

        m_stokerService.startLog( m_cooker.getCookerName(), newLogName, deviceList, new AsyncCallback<Integer>() {

            public void onFailure(Throwable caught)
            {
                // TODO Auto-generated method stub

            }

            public void onSuccess(Integer result)
            {
                if ( result.intValue() == 1)
                {
                    getActiveLogListFromServer();
                }
            }
        });
    }
 
    private void setConnectedImage()
    {
        if ( m_connected == true) {
            //StatusImage.setUrl(strConnectedImageURL);
            m_statusText.setText(strConnectedText);
            m_statusText.setStyleName("ConnectedText");
        }
        else {
            //StatusImage.setUrl(strNotConnectedImageURL);
            m_statusText.setText(strNotConnectedText);
            m_statusText.setStyleName("NotConnectedText");
        }

    }
    public void setConnected( boolean b )
    {
        if ( m_connected != b )
        {
            m_connected = b;

            if ( b == false )
               updateGauges( null );

           // setConnectedImage();
        }
    }

    /**
     * Gets active log list from server, updates the log list drop down and calls addGraph()
     */
    private void getActiveLogListFromServer()
    {
        m_stokerService.getLogList(new AsyncCallback<ArrayList<LogItem>>() {

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
     * @param logItemList List of LogItems to be updated.  Only logs for the specific cooker
     * display in the drop down.
     */
    private void updateLogList( ArrayList<LogItem> logItemList)
    {
        m_logListBox.clear();
        for ( LogItem l : logItemList )
        {
            if ( l.getCookerName() == null || m_cooker.getCookerName() == null )
            {
                // This is a programmatic error if it hits.  The server is responsible
                // for cleaning up logs if the cooker is renamed.
                Log.error("updateLogList: log ["+ l.getLogName() + "] does not have an active cooker named: [" + l.getCookerName() + "[");
                return;
            }
            if ( l.getCookerName().compareTo(m_cooker.getCookerName()) == 0)
            {
               m_activeLogItemsMap.put( l.getLogName(), l );
               m_logListBox.addItem(l.getLogName());

            }
        }

    }

    /** 
     * Update cooker sub-components with new data.
     * @param sdp
     */
    public void updateGauges( SDataPoint sdp )
    {

        m_graphUpdate = false;
        if ( sdp != null)
        {
            setConnected(true);
            try
            {
                if ( sdp instanceof SBlowerDataPoint )
                {
                    if ( m_cooker.getPitSensor() != null)
                    {
                        ProbeComponent pc = m_guageMap.get(m_cooker.getPitSensor().getID());
                        if ( pc != null )
                           pc.updateData(sdp);
                    }
                }
                else
                {
                    ProbeComponent pc = m_guageMap.get(sdp.getDeviceID());
                    if ( pc != null )
                       pc.updateData(sdp);
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            for ( ProbeComponent gc : m_guageMap.values() )
            {
                gc.updateCurrentTemp(0);
            }
        }
    }

    private void updateGraph( ArrayList<SDataPoint> arSDP )
    {
        setConnected(true);
        m_graphUpdate = true;
        m_graphStoker.addData( arSDP );

    }

    public void updateGraph( SDataPoint sdp, boolean refresh)
    {
        setConnected(true);
        m_graphUpdate = true;
        if ( existsInActiveLog( sdp ))
           m_graphStoker.addData( sdp, refresh );

    }

    
    /**
     * Calls server and gets all data points for the requested graph
     * @param logName Log name for which to get the data points for
     */
    public void refreshGraphData( String logName )
    {
        m_stokerService.getAllGraphDataPoints( logName, new AsyncCallback<ArrayList<ArrayList<SDataPoint>>>() {
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
                 m_graphStoker.redraw();

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
     * @return true if data point exists in the actively selected log
     *         false if not
     */
    private boolean existsInActiveLog( SDataPoint sdp)
    {
        int selected = m_logListBox.getSelectedIndex();
        if ( selected < 0 )
            return false;    // ignore data points if the list box is not ready
        
      //  System.out.println("existsInActiveLog - Selected Index: " + selected );
        String strSelectedLog = m_logListBox.getItemText(selected);
        LogItem li = m_activeLogItemsMap.get( strSelectedLog );

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
        for (ProbeComponent gc : m_guageMap.values())
        {
           gc.draw();
        }
        if ( m_graphUpdate )
        {
          // graphStoker.draw();
           m_graphUpdate = false;
        }
    }

    public void loginEvent()
    {
        m_newLogButton.setVisible(LoginStatus.getInstance().getLoginStatus());
        m_manageLogsButton.setVisible(LoginStatus.getInstance().getLoginStatus());
        m_stopLogsButton.setVisible(LoginStatus.getInstance().getLoginStatus());
        m_noteLogsButton.setVisible(LoginStatus.getInstance().getLoginStatus());
        m_alertsButton.setVisible(LoginStatus.getInstance().getLoginStatus());

       // graphStoker.loginEvent();
        for (ProbeComponent gc : m_guageMap.values())
        {
           gc.loginEvent();
        }
        draw();
        //LoginStatus.getInstance().getLoginStatus()
    }

    public ArrayList<SDevice> getConfigUpdates()
    {
        ArrayList<SDevice> alDevList = new ArrayList<SDevice>();
        for (ProbeComponent gc : m_guageMap.values())
        {
            SDevice s = gc.getConfigUpdates();
            if ( s != null)
              alDevList.add( s );
        }
        return alDevList;
    }
}

