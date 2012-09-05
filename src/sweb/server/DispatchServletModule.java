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

package sweb.server;

import com.google.common.eventbus.EventBus;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import sweb.server.config.HardwareDeviceConfiguration;
import sweb.server.config.StokerWebConfiguration;
import sweb.server.config.stoker.StokerHardwareDevice;
import sweb.server.controller.alerts.AlertManager;
import sweb.server.controller.alerts.AlertsManagerImpl;
import sweb.server.controller.data.telnet.StokerTelnetController;
import sweb.server.controller.weather.WeatherController;
import sweb.server.log.LogManager;
import sweb.server.log.LogManagerImpl;
import sweb.server.log.StokerFile;
import sweb.server.monitors.PitMonitor;
import sweb.server.monitors.stoker.StokerPitMonitor;

public class DispatchServletModule extends ServletModule
{
    @Override
    public void configureServlets() {
    //  serve("/" + ActionImpl.DEFAULT_SERVICE_NAME + "*").with(
    //      DispatchServiceImpl.class);
 
     // bind(HardwareDeviceConfiguration.class).asEagerSingleton();
        
        
      bind( EventBus.class).asEagerSingleton();

      
      //bind(HardwareDeviceConfiguration.class).to(StokerHardwareDevice.class).in(Singleton.class);
      bind(HardwareDeviceConfiguration.class).to(StokerHardwareDevice.class).asEagerSingleton();
      
      bind(StokerWebConfiguration.class).asEagerSingleton();

      bind(StokerTelnetController.class).asEagerSingleton();

      bind(PitMonitor.class).to(StokerPitMonitor.class).in(Singleton.class); 
      
 
      bind(LogManager.class).to(LogManagerImpl.class).in(Singleton.class);

      bind(sweb.server.StokerInit.class).asEagerSingleton();

      bind(AlertManager.class).to(AlertsManagerImpl.class);

      bind(WeatherController.class).asEagerSingleton();
      bind(ClientMessenger.class).to(CometMessenger.class).in(Singleton.class);
      
      bind(StokerFile.class);
      
      //requestStaticInjection(SDataPointHelper.class);
      
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
