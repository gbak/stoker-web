# Compiling Stokerweb #

### Download and install the necessary software ###
  1. Java 1.6
  1. Apache Ant
  1. GWT SDK > 2.4 http://code.google.com/webtoolkit/download.html


### Setup build.xml ###
Modify the build.xml file

Change the hard coded paths to match the location where gwt is installed on your machine.
```
<property name="gwt.dir" value="C:\java\gwt-2.4.0" />
<property name="gwt.sdk" value="C:\java\gwt-2.4.0" />
```

### Compile java code ###

Execute ant, the available targets are:
  1. **build**
  1. **war**
  1. **addUser**
  1. **clean**
```
# ant war
```