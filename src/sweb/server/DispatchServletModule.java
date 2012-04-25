package sweb.server;

import com.google.inject.servlet.ServletModule;
import sweb.server.StokerInit;
import sweb.server.controller.StokerConfiguration;
import sweb.server.controller.StokerWebConfiguration;

public class DispatchServletModule extends ServletModule
{
    @Override
    public void configureServlets() {
    //  serve("/" + ActionImpl.DEFAULT_SERVICE_NAME + "*").with(
    //      DispatchServiceImpl.class);
      
      bind(sweb.server.StokerInit.class).asEagerSingleton();
      bind(StokerConfiguration.class).asEagerSingleton();
      bind(StokerWebConfiguration.class).asEagerSingleton();
      
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
