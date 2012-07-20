package sweb.server;

import com.google.common.eventbus.EventBus;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import sweb.server.StokerInit;
import sweb.server.controller.HardwareDeviceConfiguration;
import sweb.server.controller.StokerWebConfiguration;
import sweb.server.controller.alerts.AlertManager;
import sweb.server.controller.alerts.AlertsManagerImpl;
import sweb.server.controller.config.stoker.StokerHardwareDevice;
import sweb.server.controller.data.telnet.StokerTelnetController;
import sweb.server.controller.parser.stoker.SDataPointHelper;
import sweb.server.controller.weather.WeatherController;
import sweb.server.log.LogManager;
import sweb.server.log.LogManagerImpl;
import sweb.server.monitors.PitMonitor;
import sweb.server.monitors.stoker.StokerPitMonitor;

public class DispatchServletModule extends ServletModule
{
    @Override
    public void configureServlets() {
    //  serve("/" + ActionImpl.DEFAULT_SERVICE_NAME + "*").with(
    //      DispatchServiceImpl.class);
      
      
     
     // bind(HardwareDeviceConfiguration.class).asEagerSingleton();
    //  bind(StokerWebConfiguration.class).asEagerSingleton();
      bind( EventBus.class).asEagerSingleton();
      
    //  bind(StokerHardwareDevice.class).asEagerSingleton();
      bind(StokerTelnetController.class).asEagerSingleton();
      bind(HardwareDeviceConfiguration.class).to(StokerHardwareDevice.class).in(Singleton.class);
      bind(PitMonitor.class).to(StokerPitMonitor.class).in(Singleton.class); 
      bind(LogManager.class).to(LogManagerImpl.class);
      bind(WeatherController.class).asEagerSingleton();
      bind(ClientMessenger.class).to(CometMessenger.class).in(Singleton.class);
      bind(AlertManager.class).to(AlertsManagerImpl.class);
      bind(sweb.server.StokerInit.class).asEagerSingleton();
      
      requestStaticInjection(SDataPointHelper.class);
      
      bind(net.zschech.gwt.comet.server.CometServlet.class).asEagerSingleton();
      
      serve("/stokerweb/comet").with(net.zschech.gwt.comet.server.CometServlet.class);
      serve("/stokerweb/comet/*").with(net.zschech.gwt.comet.server.CometServlet.class);
      
      bind(sweb.server.StokerCoreServiceImpl.class).asEagerSingleton();
      serve("/stokerweb/stoke").with(sweb.server.StokerCoreServiceImpl.class);
      
      bind(sweb.server.ReportServlet.class).asEagerSingleton();
      serve("/stokerweb/report").with(sweb.server.ReportServlet.class);
      serve("/stokerweb/report/*").with(sweb.server.ReportServlet.class);
      
      
    }
}
