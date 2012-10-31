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

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import com.google.common.eventbus.EventBus;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.internal.ImmutableMap;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import sweb.server.alerts.AlertManager;
import sweb.server.alerts.AlertsManagerImpl;
import sweb.server.alerts.delivery.BrowserDelivery;
import sweb.server.alerts.delivery.Messenger;
import sweb.server.alerts.delivery.NotifyByBrowser;
import sweb.server.config.HardwareDeviceConfiguration;
import sweb.server.config.StokerWebConfiguration;
import sweb.server.config.stoker.StokerHardwareDevice;
import sweb.server.data.DataController;
import sweb.server.data.telnet.StokerTelnetController;
import sweb.server.log.LogManager;
import sweb.server.log.LogManagerImpl;
import sweb.server.log.file.StokerFile;
import sweb.server.monitors.ConnectionMonitor;
import sweb.server.monitors.PitMonitor;
import sweb.server.monitors.stoker.StokerPitMonitor;
import sweb.server.report.ReportData;
import sweb.server.rest.RestServices;
import sweb.server.weather.WeatherController;

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

      //bind(StokerTelnetController.class).asEagerSingleton();   // Working version
      bind( DataController.class).to(StokerTelnetController.class).in(Singleton.class);

      bind(PitMonitor.class).to(StokerPitMonitor.class).in(Singleton.class); 
      
 
      bind(LogManager.class).to(LogManagerImpl.class).in(Singleton.class);

      bind(sweb.server.StokerInit.class).asEagerSingleton();

      bind(AlertManager.class).to(AlertsManagerImpl.class);

      bind(WeatherController.class).asEagerSingleton();
      bind(ClientMessenger.class).to(CometMessenger.class).in(Singleton.class);
      
      bind(ReportData.class);
      
      
      
      bind(StokerFile.class);
      bind(ConnectionMonitor.class);
      bind(NotifyByBrowser.class);
      bind(Messenger.class);
      bind(BrowserDelivery.class);
      
      bind(StokerSharedServices.class);
      
      //requestStaticInjection(SDataPointHelper.class);
      bind(GuiceContainer.class);
      bind(RestServices.class);
      //bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
      bind(JacksonJsonProvider.class).in(Singleton.class);
      
      //serve("/stokerweb/rest/*").with(GuiceContainer.class);
      
      bind(net.zschech.gwt.comet.server.CometServlet.class).asEagerSingleton();
      
      serve("/stokerweb/comet").with(net.zschech.gwt.comet.server.CometServlet.class);
      serve("/stokerweb/comet/*").with(net.zschech.gwt.comet.server.CometServlet.class);
      
      bind(sweb.server.StokerCoreServiceImpl.class).asEagerSingleton();
      serve("/stokerweb/stoke").with(sweb.server.StokerCoreServiceImpl.class);
      
      bind(sweb.server.ReportServlet.class).asEagerSingleton();
      serve("/stokerweb/report").with(sweb.server.ReportServlet.class);
      serve("/stokerweb/report/*").with(sweb.server.ReportServlet.class);
      
     // serve("/stokerweb/rest").with(GuiceContainer.class);
      serve("/api/*").with(GuiceContainer.class);
      
      
    }
}
