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

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import com.gbak.sweb.server.alerts.AlertManager;
import com.gbak.sweb.server.alerts.AlertsManagerImpl;
import com.gbak.sweb.server.alerts.delivery.BrowserDelivery;
import com.gbak.sweb.server.alerts.delivery.Messenger;
import com.gbak.sweb.server.alerts.delivery.NotifyByBrowser;
import com.gbak.sweb.server.config.HardwareDeviceConfiguration;
import com.gbak.sweb.server.config.StokerWebConfiguration;
import com.gbak.sweb.server.config.stoker.StokerHardwareDevice;
import com.gbak.sweb.server.data.DataController;
import com.gbak.sweb.server.data.telnet.StokerTelnetController;
import com.gbak.sweb.server.log.LogManager;
import com.gbak.sweb.server.log.LogManagerImpl;
import com.gbak.sweb.server.log.file.StokerFile;
import com.gbak.sweb.server.monitors.ConnectionMonitor;
import com.gbak.sweb.server.monitors.PitMonitor;
import com.gbak.sweb.server.monitors.stoker.StokerPitMonitor;
import com.gbak.sweb.server.report.ReportData;
import com.gbak.sweb.server.rest.RestServices;
import com.gbak.sweb.server.weather.WeatherController;
import com.google.common.eventbus.EventBus;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.internal.ImmutableMap;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;


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

      bind(com.gbak.sweb.server.StokerInit.class).asEagerSingleton();

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
      bind(RestServices.class).asEagerSingleton();
      //bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
      bind(JacksonJsonProvider.class).in(Singleton.class);
      
      //serve("/stokerweb/rest/*").with(GuiceContainer.class);
      
      bind(net.zschech.gwt.comet.server.CometServlet.class).asEagerSingleton();
      
      serve("/stokerweb/comet").with(net.zschech.gwt.comet.server.CometServlet.class);
      serve("/stokerweb/comet/*").with(net.zschech.gwt.comet.server.CometServlet.class);
      
      bind(com.gbak.sweb.server.StokerCoreServiceImpl.class).asEagerSingleton();
      serve("/stokerweb/stoke").with(com.gbak.sweb.server.StokerCoreServiceImpl.class);
      
      bind(com.gbak.sweb.server.ReportServlet.class).asEagerSingleton();
      serve("/stokerweb/report").with(com.gbak.sweb.server.ReportServlet.class);
      serve("/stokerweb/report/*").with(com.gbak.sweb.server.ReportServlet.class);
      
     // serve("/stokerweb/rest").with(GuiceContainer.class);
      serve("/api/*").with(GuiceContainer.class);
      
      
    }
}
