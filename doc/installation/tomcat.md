# Install Oxalis NG Inbound (Tomcat 10+)

## Oxalis NG

First you should download the [oxalis-ng-war-1.0.0.war](https://github.com/OxalisCommunity/oxalis-ng/releases/download/oxalis-1.0.0/oxalis-war-1.0.0.war) file
from this [page](https://github.com/OxalisCommunity/oxalis-ng/releases) and
put it a directory of your choice. We recommend naming the folder oxalis.

Then create a folder named home inside the oxalis folder and place the oxalis.conf file, together with the necessary JKS files.

Then you should have something looking like this like this:

![Oxalis-NG folder](../img/tomcat_oxalis_folder.PNG "Oxalis folder") - //TODO

For general instructions on how to install and use Oxalis-NG, please refer to [oxalis installation guide](https://github.com/OxalisCommunity/oxalis-ng/blob/master/doc/installation.md).

## Tomcat installation

First you need to [download](https://tomcat.apache.org/download-10.cgi) Tomcat and unzip the files to a directory.

![Tomcat folder](../img/tomcat_folder.PNG "Tomcat folder") - //TODO

Then you should set the CATALINA_BASE environment variable to the chosen installation folder.

## Tomcat configuration for Oxalis 

The next step is to create a folder named Catalina in the Tomcat conf directory.

```bash
cd conf
mkdir Catalina 
```

And inside the newly created Catalina folder - create a folder named localhost.

```bash
cd Catalina
mkdir localhost 
```

Inside this localhost folder - create a file named oxalis.xml containing:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- For a default Tomcat setup, name this file 'oxalis-ng.xml' and place it in the following directory -->
<!-- $CATALINA_BASE/conf/Catalina/localhost/ -->

<!-- The Context element represents our application. -->
<!-- The 'docBase' attribute will define the main application to be run. -->
<!-- For more information see https://tomcat.apache.org/tomcat-8.5-doc/config/context.html -->
<Context docBase="C:\dev\oxalis-ng\oxalis-ng-war-1.0.0.war"  >
    
   <!-- Optionally define the OXALIS_HOME environment variable  -->
   <Environment name="OXALIS_HOME" value="C:\dev\oxalis\home" type="java.lang.String" override="false" />

   <!-- Here we can add a list of additional resources to add to the application --> 
   <!-- For more information see https://tomcat.apache.org/tomcat-8.5-doc/config/resources.html -->
   <Resources className="org.apache.catalina.webresources.ExtractingRoot">
            
      <!-- Add the other dependencies of oxalis -->
      <!--
      <PreResources 
          base="/path/to/oxalis/dir" 
          className="org.apache.catalina.webresources.DirResourceSet"
          webAppMount="/WEB-INF/lib"
          readOnly="true" />
      --> 
      
     
      <!-- The name 'PreResources' and 'PostResources' refers to whether the resources will be added before or after the resource we defined in the 'Context' element -->
      <!-- There is also 'JarResources' that will let us alter the content of the 'Context' resource itself -->
      <!-- the precedence only affects witch version to use in case multiple resources provide the same content (the latest element will win) -->
      
   </Resources>

</Context>
```

This is only an example deployment XML file for tomcat 10+. Ensure that your file locations match with the paths in the XML.

Now you are ready to start the Tomcat server.

# Verifying the installation

The easiest way to see that the AS4 endpoint is up and running is to visit its endpoint address.
If we now visit ``localhost:8080/oxalis-ng/as4`` we will be greeted with the message ``Hello AS4 world``

