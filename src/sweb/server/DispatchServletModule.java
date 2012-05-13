package sweb.server;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import sweb.server.StokerInit;
import sweb.server.controller.HardwareDeviceConfiguration;
import sweb.server.controller.StokerWebConfiguration;
import sweb.server.controller.alerts.AlertsController;
import sweb.server.controller.config.stoker.StokerHardwareDevice;
import sweb.server.controller.parser.stoker.SDataPointHelper;
import sweb.server.controller.weather.WeatherController;
import sweb.server.log.LogManager;
import sweb.server.log.LogManagerImpl;

public class DispatchServletModule extends ServletModule
{
    @Override
    public void configureServlets() {
    //  serve("/" + ActionImpl.DEFAULT_SERVICE_NAME + "*").with(
    //      DispatchServiceImpl.class);
      
      
     
     // bind(HardwareDeviceConfiguration.class).asEagerSingleton();
    //  bind(StokerWebConfiguration.class).asEagerSingleton();
      bind(LogManager.class).to(LogManagerImpl.class);
      bind(WeatherController.class).asEagerSingleton();
      bind(ClientMessenger.class).to(CometMessenger.class).in(Singleton.class);
      bind(AlertsController.class);
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
