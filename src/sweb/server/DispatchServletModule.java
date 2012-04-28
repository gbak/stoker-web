package sweb.server;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import sweb.server.StokerInit;
import sweb.server.controller.HardwareDeviceConfiguration;
import sweb.server.controller.StokerWebConfiguration;
import sweb.server.controller.alerts.AlertsController;
import sweb.server.controller.config.ConfigurationController;
import sweb.server.controller.config.stoker.StokerConfigurationController;
import sweb.server.controller.weather.WeatherController;

public class DispatchServletModule extends ServletModule
{
    @Override
    public void configureServlets() {
    //  serve("/" + ActionImpl.DEFAULT_SERVICE_NAME + "*").with(
    //      DispatchServiceImpl.class);
      
      
      bind(ConfigurationController.class).to(StokerConfigurationController.class).in(Singleton.class);;
      
      bind(HardwareDeviceConfiguration.class).asEagerSingleton();
      bind(StokerWebConfiguration.class).asEagerSingleton();
      
      bind(WeatherController.class).asEagerSingleton();
      bind(ClientMessenger.class).to(CometMessenger.class).in(Singleton.class);
      bind(AlertsController.class);
      bind(sweb.server.StokerInit.class).asEagerSingleton();
      
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
