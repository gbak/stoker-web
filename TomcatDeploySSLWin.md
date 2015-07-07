#Tomcat Deploy with SSL on Windows in 10 minutes

# Deploy Stokerweb in Tomcat with SSL in 10 minutes #

## Prerequisites ##
  * Running copies of Windows XP or better
  * Installed version of the [Java 6 SDK](http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-javase6-419409.html) (Development Kit).

## Install and Deploy ##

  1. Download Tomcat 7
> > [Tomcat 7 download](http://tomcat.apache.org/download-70.cgi)
  1. Extract Tomcat 7 to convenient location.
    * for this guide, _C:\java\apache-tomcat-7.0.34_ was used.
  1. Download [Stoker-web](http://code.google.com/p/stoker-web/downloads/list) and unzip
  1. Copy the stokerweb.war file to Tomcat webapps directory
```
copy stokerweb.war c:\java\apache-tomcat-7.0.34\webapps
```

## Configure SSL ##
  1. Create a self signed certificate and keystore
```
%JAVA_HOME%\bin\keytool -genkey -alias tomcat -keyalg RSA -keystore c:\java\apache-tomcat-7.0.34\conf\server.jks
```
> > While running keytool, you'll be asked a series of questions when creating the certificate, you'll also be prompted to enter two passwords during the creation.  Replace path to Tomcat/conf directory to suite your install location.
```
C:\tmp>%JAVA_HOME%\bin\keytool -genkey -alias tomcat -keyalg RSA -keystore c:\ja
va\apache-tomcat-7.0.34\conf\server.jks
Enter keystore password:
Re-enter new password:
What is your first and last name?
  [Unknown]:  Gary
What is the name of your organizational unit?
  [Unknown]:  Development
What is the name of your organization?
  [Unknown]:  Stoker-web
What is the name of your City or Locality?
  [Unknown]:  Suwanee
What is the name of your State or Province?
  [Unknown]:  GA
What is the two-letter country code for this unit?
  [Unknown]:  US
Is CN=Gary, OU=Development, O=Stoker-web, L=Suwanee, ST=GA, C=US correct?
  [no]:  yes

Enter key password for <tomcat>
        (RETURN if same as keystore password):
Re-enter new password:
```
  1. Edit _server.xml_ in apache-tomcat-7.0.34\conf directory and add the following section above the line below
```
<Connector protocol="org.apache.coyote.http11.Http11NioProtocol"
           port="8443" SSLEnabled="true"
           maxThreads="200" scheme="https" secure="true"
           keystoreFile="${catalina.home}/conf/server.jks" keystorePass="tomcat"
           clientAuth="false" sslProtocol="TLS" />
```
`<!-- Define an AJP 1.3 Connector on port 8009 -->`
  1. Further tips and troubleshooting can be found at [Tomcat's SSL How-to](http://tomcat.apache.org/tomcat-7.0-doc/ssl-howto.html)

## Setup Environment ##
  1. Create a folder for Stokerweb files.  This directory should be outside of the Tomcat directory.
```
mkdir c:\tmp\StokerWebDir
```
  1. Add a system wide environment variable called STOKERWEB\_DIR and add the path to stokerweb folder.  Do this from the Advanced system properties or from a Command window running as Administrator
```
setx STOKERWEB_DIR "C:\tmp\StokerWebDir" /M
```
  1. Copy other files from stoker-web.zip file to StokerWebDir
  1. Edit the stokerweb.properties file as described on the main page.

## Start Tomcat ##
  1. Launch tomcat from startup.bat file
```
C:\java\apache-tomcat-7.0.34\bin\startup.bat
```
  1. Test Connection
  1. https://localhost:8443/stokerweb

## Android Client Setup ##
Use the following settings for using stoker-web Android with SSL.
On the settings page, apply the following settings:
  * Type: stoker-web
  * Additional URL: stokerweb
  * Check 'Use HTTPSs'