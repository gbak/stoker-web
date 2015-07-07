# Deploy Stoker-web with Jetty on Windows #

## Overview ##

These are simple instructions for deploying Stoker-web in Jetty.  If Jetty is already downloaded, the rest should take only a few minutes.  The instructions specific to configuring Stoker-web are not here, they are on the main page.

## Download and deploy ##
  1. Download jetty and extract the contents of the .zip file to a directory of your choosing.  The examples below use the directory `C:\Temp\Jetty-6.1.23`
```
Jetty Downloads:  http://dist.codehaus.org/jetty/
```
```
C:\Temp\Jetty-6.1.23> cd contexts
C:\Temp\Jetty-6.1.23\contexts> notepad stokerweb.xml
```
  1. Create a file called **`stokerweb.xml`** in the **contexts** directory and add the lines below to the file.
```
<?xml version="1.0"  encoding="ISO-8859-1"?>
<!DOCTYPE Configure PUBLIC "-//Mort Bay Consulting//DTD Configure//EN" "http://jetty.mortbay.org/configure.dtd">

<Configure class="org.mortbay.jetty.webapp.WebAppContext">

  <Set name="contextPath">/stokerweb</Set>
  <Set name="war"><SystemProperty name="jetty.home" default="."/>/webapps/stokerweb.war</Set>

  <Set name="extractWAR">false</Set>
  <Set name="copyWebDir">false</Set>

</Configure>
```
  1. Copy your `stokerweb.war` file to the `webapps` directory.
```
c:\Temp> copy c:\some\location\stokerweb.war c:\Temp\Jetty-6.1.23\webapps
```
  1. Create your `StokerWebDir` and set the `STOKERWEB_DIR` environment variable correctly
```
C:\Temp>  mkdir StokerWebDir

C:\Temp>  set STOKERWEB_DIR=c:\Temp\StokerWebDir
```
  1. Add and edit your `stokerweb.properties` file to the `StokerWebDir`
```
# Stoker settings, you should only have to set the IP address
stoker_ip=192.168.13.182
...
# Associate the Serial ID of each blower to a Cooker Name
230000002a55c305=WSM22
...
# Mail settings
mail.sendTo=xxx@gmail.com
mail.password=
mail.smtp.user=email.account
mail.smtp.host=smtp.gmail.com
...
# Your local zip code to pull weather information.
weather_zipcode=30024
```
  1. Add an empty `login.properties` file to your `StokerWebDir`
  1. Run `addUser.jar` as necessary to add login ids and passwords
```
c:\Temp> java -jar addUser.jar
```
  1. Start jetty
```
c:\Temp> cd Jetty-6.1.23

c:\Temp\Jetty-6.1.23>  java -jar start.jar
```

## Connect to Stoker-web ##
In your browser, navigate to the stoker-web page using this URL:
```
http://localhost:8080/stokerweb
```

The 'stokerweb' path above can be customized or removed entirely.  Just change the `contextPath` in the `stokerweb.xml` file.  To have no path at all, just use a '/' for the path.  The 8080 port is the default port for Jetty.  That can be changed if desired in the `etc/jetty.xml` file. (Search for 8080)

To access this page from other machines on your network, the URL will have to be changed to use the hostname or IP address of the machine that jetty is running on.  Since most home networks don't have a name server, the IP address may be your best bet.

To look up your IP address on Windows use the ipconfig command:
```
C:\> ipconfig
```

Now use that in your URL:
```
http:\\192.168.10.10\stokerweb
```

If you have a Windows firewall configured, the configured port, in this case 8080, will have to be opened up for external use.