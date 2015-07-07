## Overview ##

Stoker-web is a web application that allows the [Stoker power draft system](https://www.rocksbarbque.com/) to be viewed and controlled from an external browser, this can be on the local network, wireless device or if properly setup from anywhere on the Internet.  Stoker-web offers additional functionality over the Stokers own internal browser, these features include: temperature graphs, logging, PDF cook reports, email alerts, local weather information, added Security and more.  Stoker-web is written Java so the server will run on most platforms that Java runs on.  It has been tested on Windows and Linux.  See Browser section below for client compatibility.

![http://wiki.stoker-web.googlecode.com/git/sweb6.png](http://wiki.stoker-web.googlecode.com/git/sweb6.png)

[Sample with digit display](http://wiki.stoker-web.googlecode.com/git/StokerWebDigitSample.png)

[PDF Report Sample](http://wiki.stoker-web.googlecode.com/git/report.pdf)

## Status ##

Status Update: Android client now available: https://play.google.com/store/apps/details?id=com.gbak.sweb.client.android.paid


Status Update: Beta 0.4.1 Released!  4/12/2013
> Runnable Jar file version v1.0\_0.4.1 also released

Status Update: Beta 0.4.0 Released!  12/10/2012

Status Update: Beta 0.3.0 Released!  9/10/2012

Status Update: Beta 0.2.1 Released!  3/08/2012

Status Update: Beta 0.2 Released!  2/10/2012

Status Update: Beta 0.1 Released!  1/22/2012

Please see Releases notes for details.

## Android ##
Native android app will have two connection modes: stoker direct and stoker-web
  * Stoker mode will allow
    * monitoring
    * Adjust settings
    * Android notifications
    * 2 minute updates
  * Stoker-web mode
    * Monitoring
    * Adjust settings
    * Android notifications
    * 5 second updates when active / 2 minute alert checks
    * Track running logs
    * Create / end logs
    * Add notes
    * View log graphs (version 2)

### Browsers ###

Stoker-web has been seen to work on the following Hardware / Browsers:
  * Chrome
  * Mozilla
  * IE 6, 7, 8
  * Safari ( Windows and Mac )
  * iPhone 4
  * iPad
  * Android with Firefox - Has been tested with the digit temp display and it works!

Not working:
  * iPod Touch
  * Android with native browser
  * IE 9 - Blank page, selecting compatibility mode brings up the page, but still has issues.
  * Windows 7 mobile devices

## Known Issues as of Beta 4 ##

  * Stoker-web stores the configuration of the stoker in a local file.  When stoker-web starts it will use this file to build the cooker list.  The initial settings in stoker-web will be those of the last time the stoker was updated via stoker-web.  If the stoker settings or probe configuration was modified outside of stoker-web, the settings that appear in stoker-web may not be the actual settings set on the stoker.  To get around this, after starting stoker-web.  Set the desired temp settings and it the Update button.  This will push new settings to the stoker.  An update for this is being looked into.
  * Resizing the browser to a smaller size than the size that was used when stoker-web was loaded may cause the graph to drop below the probes.  The easy fix for this is to use the browser refresh button.
  * See Issues for other open bugs

## Known Compatibility Issues ##

  * Running the server on Fedora / Redhat OS may cause the text labels in the PDF report chart to come up blank.  Easy workaround for this is to install a JVM from Sun in a new directory.  Define JAVA\_HOME with the new JVM and launch jetty with that copy of java.
  * PDF report may not download in Chrome.  New Chrome window/tab opens and then hangs after the report is generated and sent to the browser.  Right clicking on the screen and then choosing "Save as" will allow the report to be saved to the disk.  The PDF download works from a Windows server but does not from a Linux server.
  * Stoker-web can't be run in multiple tabs in the same browser, the comet server push library does not allow this.  Running it in multiple tabs sends it into a loop opening and destroying connections, the browser window goes blank and hangs.

## Getting Started ##

Stokerweb is now released as either an easy to setup and run Java jar file or a classic war file.  See the sections below.

## Run Stokerweb Jar file ##

The runStokerweb.jar file can be executed from the command line of your favorite operating system.  This one file has the stoker-web.war file and a Jetty server embedded within it.  Running the jar file will first extract the necessary files allowing them to be edited for your specific machine and then run again to start stokerweb.

You must be using a version of Java 7 to use the jar file.  Execute the jar file with this command:
```
java -jar runStokerweb.jar
```

A few files will be extracted.  The files ending in .properties need to be edited.
  * stokerweb.properties:  Edit the IP address for your stoker, email settings and local zip code.
  * log4j.properties: Setup the path to where the log file should be saved to
  * runStokerweb.properties:  Change the listen port and Context path if desired.  Defaults are port 8080 and context path /.  This will effectively be the URL with the default settings:  `http://localhost:8080/`

Run the same command again to launch stokerweb.
```
java -jar runStokerweb.jar
```


## War file distribution ##

Stoker-web is released as a .war file.  This file should be deployed within the web application server of your choosing, Stoker-web has been tested with both Tomcat and [Jetty](http://jetty.codehaus.org/jetty/).  In addition to the deployed .war file, there is also a separate directory where the Stoker-web configuration and log files exist.  This directory should be in a secure location and NOT be publicly accessible. This directory is accessed by the environment variable STOKERWEB\_DIR.

Installation and configuration steps:

  1. Download and extract the latest available version.
  1. Create a directory in a secure location on the webserver called StokwerWebDir, or anything else you'd like it to be.
  1. Set the environment variable STOKERWEB\_DIR to be the absolute path to the new folder above.  Add this path to your profile or machine settings so it is set automatically after boot up.
  1. Copy the stokerweb.properties file into this new directory, assign the correct permissions
  1. Create an empty file called login.properties, assign the correct permissions
```
# commands for *nix systems
cd /opt
mkdir StokerWebDir
chown nobody:nogroup StokerWebDir
export STOKERWEB_DIR=/opt/StokerWebDir
cd StokerWebDir
unzip stokerweb-release.zip

```
  1. Edit the stokerweb.properties file, change the IP, email and weather settings
```
# Stoker settings, you should only have to set the IP address
stoker_ip=192.168.18.210

# Mail settings
mail.sendTo=sendToEmail@address.com
mail.password=password
mail.smtp.user=email.user
mail.smtp.host=smtp.gmail.com
mail.smtp.port=465

# Your local zip code to pull weather information.
weather_zipcode=30024
```
  1. Run the addUser.jar to create a login/password for Stoker-web
```
# The STOKERWEB_DIR is required to be set so it can find the login.properties file
export STOKERWEB_DIR=/location/of/StokerWebDir
java -jar addUser.jar
```
  1. Deploy the war file within your web application.  [Deploy Stoker-web on Windows](http://code.google.com/p/stoker-web/wiki/JettyDeploy)
  1. Start the web application server

## Compiling Stoker-web ##

It is recommended that you use the available releases from the download section, but if you'd like to try to compile it yourself, these are the basic steps.

[Compiling Stoker-web](http://code.google.com/p/stoker-web/wiki/CompilingStokerWeb)