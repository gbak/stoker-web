## Introduction ##

Using the runStokerweb.jar file is the easiest and most convenient way of running stokerweb.  It includes the Stokerweb war file, all the configuration files and an embedded version of the Jetty servlet engine.

## Prerequisites ##

  * Java 7
  * Download copy of runStokerweb.jar

## Note for existing Stoker-web users ##

runStokerweb.jar will read the STOKERWEB\_DIR environment variable and attempt to extract files and work out of that directory.  It's best to backup the existing properties files in that directory and then remove all files except the logs directory from your STOKERWEB\_DIR prior to running runStokerweb for the first time.  If any of the files that runStokerweb attempts to extract already exist in that directory, then the newer copies will not be extracted. If you have a recent version (0.4.0) of stokerweb your exiting properties files can stay in that directory, there are no new entries for 0.4.1.

## Running runStokerweb.jar ##

runStokerweb.jar is an executable Java jar file.  It is run with this command:
```
java -jar runStokerweb.jar
```
The first time runStokerweb is executed the program will extract all the necessary files for running Stokerweb.  The files will be extracted to the current directory unless the environment variable STOKERWEB\_DIR is defined.  If that is defined, then the files will be extracted to the directory that STOKERWEB\_DIR is assigned.

**List of extracted files**
```
stokerweb.war
stokerweb.properties
log4j.properties
login.properties
runStokerweb.properties
addUser.jar
```

During the first execution of runStokerweb.jar if you don't already have a login.properties file in the target directory, you will be prompted to create a new user.  Fill in the username and password fields.

Once the files have been extracted, the following files need to be edited.

  * stokerweb.properties
```
# Stoker settings, you should only have to set the IP address
stoker_ip=192.168.13.182
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
  * runStokerweb.properties
```
ContextPath=/
ListenPort=8080
```
Choose a ContextPath and a port that stokerweb will listen on.  These are the default values.

  * log4j.properties
```
log4j.appender.file.File=C:\Temp\StokerWebDir\stokerweb_app.log
```
Put a valid path to where the log file should be written.

The verbosity of the log can be adjusted with the first line if the file
```
log4j.rootLogger=DEBUG, file, stdout
```
Valid options are:  ERROR, WARNING, INFO, DEBUG, TRACE
The stdout indicator (along with the comma) can be removed to reduce the amount of content that is sent to the console window.

## Launching Stokerweb ##

Launch stokerweb with the same command as before
```
java -jar runStokerweb.jar
```
If all the necessary files are already extracted, Jetty will be launched and stokerweb deployed.  It will take about a minute to get started.

## Check your Browser ##

After it has started, open your browser and try the URL:
```
http://localhost:8080/
```
and you should get the stokerweb page.  If you have altered the port or context path, you will have to adjust your URL.

## Additional Information ##

runStokerweb should be run from the command line the first time it is run.  If the login.properties file did not exist and had to be created, the program will prompt for a user and password, if it is not run from the console, this will be skipped.

runStokerweb.jar can be run from Windows by double clicking the jar file from within Windows Explorer if Java is properly setup.  It is best to run it this way only if stoker-web has already been configured. Running the jar this way executes the javaw command which has no associated console window.