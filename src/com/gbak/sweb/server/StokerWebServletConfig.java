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


package com.gbak.sweb.server;



import com.gbak.sweb.server.monitors.ConnectionMonitor;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class StokerWebServletConfig extends GuiceServletContextListener {

  @Override
  protected Injector getInjector() {
      Injector injector;
      System.out.println("StokerWebServletConfig...");
      StokerWebProperties.getInstance();  // This is here to set the classpath for log4j.  Ideally we should
                                          // create this with guice.
      injector = Guice.createInjector(new DispatchServletModule());
      
     // injector.getInstance(StokerPitMonitor.class).start();
      injector.getInstance(ConnectionMonitor.class).start();
      
    return injector;
  }
  
  
}