# Overview #

These instructions are for installing Tomcat and deploying stoker-web on Ubuntu Linux, they were provided by stoker-web user Gary Noles.


## Download and Install ##

  1. Change directory to /var/local
  1. Download tomcat
```
sudo wget http://apache.mirrors.lucidnetworks.net/tomcat/tomcat-7/v7.0.27/bin/apache-tomcat-7.0.27.tar.gz
```
  1. Extract tar file and create symbolic link called tomcat
```
sudo tar zxvf apache-tomcat-7.0.27.tar.gz  
sudo ln –s apache-tomcat-7.0.27 tomcat 
```
## Configure ##
  1. Change directory to the tomcat/conf directory
```
cd tomcat/conf
```
  1. Edit the tomcat-users.xml file and add the lines below.  Careful to avoid the commented areas:
```
<role rolename=”manager-gui”/>
	<user username=”YOURUSER” password=”YOURPASSWORD” roles=”manager-gui”/>
```
  1. Change to the init.d directory
```
cd /etc/init.d
```
  1. Create a file called **tomcat** and add the contents below.  The paths to JAVA\_HOME and STOKERWEB\_DIR are specific to your machine
```
export JAVA_HOME=/usr/lib/jvm/java-6-sun
export STOKERWEB_DIR=/location/to/StokerWebDir
case $1 in
   start)
      sh /var/local/tomcat/bin/startup.sh
      ;;
   stop)
      sh /var/local/tomcat/bin/shutdown.sh
      ;;
    restart)
      sh /var/local/tomcat/bin/shutdown.sh
      sh /var/local/tomcat/bin/startup.sh
       ;;
esac
exit 0
```
  1. Change the permissions on the tomcat file and add the necessary aliases in the rc directories:
```
sudo chmod 755 /etc/init.d/tomcat
sudo ln –s /etc/init.d/tomcat /etc/rc1.d/K99tomcat
sudo ln –s /etc/init.d/tomcat /etc/rc2.d/S99tomcat
```
## Startup and deploy ##
  1. Start up Tomcat
```
cd /var/local/tomcat/bin
sudo ./startup.s
```
  1. Configure your stokerweb.properties file in your STOKERWEB\_DIR directory.
  1. Don't forget to use the addUser.jar to add a user to the stoker-web users file
  1. Open your web browser and go to:
```
http://localhost:8080/manager
```
  1. Click on choose file in the WAR file to deploy box (near the bottom)
  1. Select the stokerweb.war file and click deploy
  1. If you have it all setup correctly, plug in your stoker and hit the server again
```
http://localhost:8080/stokerweb
```