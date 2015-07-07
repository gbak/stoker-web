## Planned for Upcoming Releases ##
  * Alarm improvements, add snooze with configurable snooze time.
  * Updates to Report:  Add cooker name, weather information and format the configuration data nicely.
  * Beautify popup dialog boxes.
  * Add Timed and Temp Alert functionality.  (non-stoker alerts)
  * Annotate graph with config temp changes and user notes

## Release: stoker-web-0.4.1.zip ##
**Fixed**
  * Added code to shutdown specific internal services when shutdown is initiated from Tomcat manager.  There is a small memory leak when cycling through Tomcat, too many cycles (without cycling the JVM and the JVM may run out of PermGen space).
  * Fixed buttons becoming disabled when logged in and browser refreshed.

## Release: stoker-web-0.4.0.zip ##
**New Features**
  * RESTful services to request data from stokerweb.  These services are used by the Android client, but can be consumed by any program.  Jersey documentation for the services can be pulled with: http://localhost/stokerweb/api/application.wadl
  * It is strongly recommended that https is used with the REST services.  Plain text login ID and passwords are sent as POST data for updates to stoker-web.
**Upgrade Notice**
  * If you are upgrading, add this line to the end of your existing stokerweb.properties.  It is required for the weather to work and it is important that there are no trailing spaces after the '=' sign.
`weather_url=http://query.yahooapis.com/v1/public/yql?format=json&q=select%20*%20from%20weather.forecast%20where%20location=`

**Fixed:**
  * [Issue 24](http://code.google.com/p/stoker-web/issues/detail?id=24)  Yahoo Weather not working.  Corrected the API call to gather Yahoo weather.
  * [Issue 25](http://code.google.com/p/stoker-web/issues/detail?id=25) If blower goes missing from a pit probe can cause a Null pointer exception.
  * [Issue 23](http://code.google.com/p/stoker-web/issues/detail?id=23) Issues was not fixed correctly in 0.3.5.  Configurations screen would popup if stoker-web was correctly configured, but the stoker was offline.

## Release: stoker-web-0.3.5.zip ##
**Fixed:**
  * [Issue 23](http://code.google.com/p/stoker-web/issues/detail?id=23) Stoker-web does not produce the configuration screen if CookerConfig.json does not exist on the server.  This leaves the user with no way to configure stoker-web and create the file.  Main screen shows only the header and buttons and not connected status.

## Release: addUser-0.3.4.zip ##
**Fixed:**
  * [Issue 20](http://code.google.com/p/stoker-web/issues/detail?id=20) Class not found exception running addUser.jar

Only addUser.jar released for 0.3.4

## Release: stoker-web-0.3.3.zip ##
**Fixed:**
  * Fixed merge issue causing Firefox layout problems to be missed in 0.3.2 build.

Please read notes for 0.3.0, 0.3.2 for this release.

## Release: stoker-web-0.3.2.zip ##
**New Features:**
  * [Issue 9](http://code.google.com/p/stoker-web/issues/detail?id=9) Offline mode.  Stoker-web will start and present a page with the stoker offline.  Users will be able to login and create reports from past cooks.  Stoker-web polls for the stoker every 15 minutes, when it finds the stoker online, a new default log will start and the cooker pane will paint in the browser.  Once the stoker has been unavailable for 30 minutes, stoker-web will go into offline mode again, cancel all logs and drop the cooker pane(s).

New entries in `stokerweb.properties`:
```
# Minutes from after a disconnect is detected until stoker-web goes in offline mode
timeout_to_extended_disconnect=30

# Minutes to test if the stoker is available while stoker is in offline mode
timeout_to_reconnect=15
```
> If the Stoker becomes unavailable while stoker-web is in online mode, it will attempt a reconnect every minute, if it connects it will continue where it left off, if the disconnect lasts for the extended period above (30 min), it will go offline.

  * Added ability to control the alarm repeat period.  New property in the stokerweb.properties file will control how long it takes the alarm to sound after silencing it.  Setting defaults to 5 if it does not exist in the properties file.
```
# Number of minutes that a alarm will silence before repeating
minutes_to_repeat_alarm=10
```

**Fixed:**
  * [Issue 2](http://code.google.com/p/stoker-web/issues/detail?id=2). Browser refresh can bring log back to life.  Using incorrect hash key to remove the log.
  * [Issue 11](http://code.google.com/p/stoker-web/issues/detail?id=11).  Color indicator on digit display not changing from Red when alarm condition removed.
  * [Issue 16](http://code.google.com/p/stoker-web/issues/detail?id=16). Alerts not working correctly in 0.3.0.
  * [Issue 17](http://code.google.com/p/stoker-web/issues/detail?id=17).  Firefox display issues corrected.

## Release: stoker-web-0.3.0.zip ##

**New Features:**
  * Drag and drop configuration changes,  [Issue 7](http://code.google.com/p/stoker-web/issues/detail?id=7).  This allows saving stoker configuration locally and allows logical grouping of probe sensors.
  * Multiple cooker support.  Create as many cookers as you want and assign probes to them.  A cooker can be defined with one or more probes, it will also work fine without a fan assigned.
  * Rename probes on the fly.  Switching items in the smoker?  Rename your brisket probe to chicken without restarting Stoker-web.  PDF report will show a new series for the renamed probe, this will be done to the screen graph in the next release.
  * Local alarms, [Issue 3](http://code.google.com/p/stoker-web/issues/detail?id=3).  Browser and email alerts without stoker alarm enabled.  Add the following line to your stokerweb.properties file:
```
alarmSettings_location=local
```
  * Graph reflow on browser resize!  Yes, resize the browser and the graph now resizes to fit.
  * Added dependency injection to server side code with Guice.

**Fixed:**
  * [Issue 6](http://code.google.com/p/stoker-web/issues/detail?id=6)  Multiple fans connected to Stoker cause no cooker to appear
  * [Issue 12](http://code.google.com/p/stoker-web/issues/detail?id=12)  Long cooks can slow down the browser.  A fix for this was partly implemented.  On browser refresh, the graph data is load differently, and it happens much quicker now, just a few seconds.  I'll monitor to see if removing redundant points is necessary.


## Release: stoker-web-0.2.2.zip ##

**New Features:**
  * None

**Fixed:**
  * [Issue 10](http://code.google.com/p/stoker-web/issues/detail?id=10)  JSON configuration interface was not setting the Alarm type from stoker.

## Release: stoker-web-0.2.1.zip ##

**New Features:**
  * None

**Fixed:**
  * [Issue 8](http://code.google.com/p/stoker-web/issues/detail?id=8)  Black box stokers with Wifi firmware upgrade not working correctly.

## Release: stoker-web-0.2.zip ##

**New Features:**
  * Addressed [Issue 4](http://code.google.com/p/stoker-web/issues/detail?id=4) by added ability to use stoker-web without an Internet connection.  Replaced the gauges with text so visualization API is not needed on start up.
  * Added settings to stokerweb.properties to allow adjustments to gauges.  Min temp, Max temp and gauge increments can now be adjusted.
  * Added ability for addUser.jar to locate the login.properties file from the classpath.  The STOKERWEB\_DIR environment variable must be set prior to running addUser.jar
  * Improved server side event handling.  Reduced overall number of event listeners.

**Fixed:**
  * [Issue 5](http://code.google.com/p/stoker-web/issues/detail?id=5)  Added log4j classes to addUser jar file.
  * Failure to retrieve weather data will no longer halt page load.

**Additional settings for stokerweb.properties:**
```
# This can be 'gauge' or 'text'
client.tempDisplayType=gauge

client.tempDisplayGauge.gaugeMax=400
client.tempDisplayGauge.gaugeMin=0
client.tempDisplayGauge.gaugeStepping=50
```

## Release: stoker-web-0.1.zip ##

**New Features:**
  * PDF report generation added.  New Report button launches the file chooser and any current or previous log can be chosen for report generation.  Ending a running log with the 'End' button will also trigger a report generation.  Reports are sent to a new browser tab or window.
  * Added new panel layout if more 4 or more probes are detected.  New layout will position the probes in a line and place the graph beneath it.  (This feature is largely untested.)
  * Added log4j server side logging.
  * Added ability to press Enter on login dialog to submit data.
  * Changed blower series on highcharts graph to use the step option, reducing the complexity and number of points on the graph.  The jFreeChart in the pdf report and the fan time tallies motivated this change.
  * Cleaned up warnings from code.

**Fixed:**
  * Fixed [Issue 1](http://code.google.com/p/stoker-web/issues/detail?id=1) Updates to the Stoker configuration are not being logged correctly to the log file.
  * Fixed Alerts code, Food probes was using the upper setting to trigger the alarm instead of the target temperature setting.  Thanks Jeff
  * Correctly handle trailing slash on STOKERWEB\_DIR env variable if it exists.