
package sweb.server;


import sweb.server.monitors.stoker.StokerPitMonitor;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class StokerWebServletConfig extends GuiceServletContextListener {

  @Override
  protected Injector getInjector() {
      Injector injector;
      injector = Guice.createInjector(new DispatchServletModule());
      injector.getInstance(StokerPitMonitor.class).start();
      
    return injector;
  }
  
  
}