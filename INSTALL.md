# Installation guide

This guide will help you through the installation of the QGears Review tool with the Eclipse user interface.

Assumed environment: ```Ubuntu 14.04.1```

Required software:

 * Eclipse Luna 4.4.2
 * Java 1.6+

# Update site

Most convenient way is to install from update site:
```
http://qgears.net/opensource/updates/
```

After installing, you can proceed to the [wiki](https://github.com/qgears/qgears-review-tool/wiki) to learn how to how to configure the review tool and assemble a workspace.

# Installing from source

Create an arbitrary directory used for building the review tool! This directory will further be referred to as $BUILDDIR. 

Execute the following commands:
```
cd $BUILDDIR
git clone --recursive https://github.com/qgears/repository-builder.git .
mvn clean package
```

Note that [setting up a fast P2 mirror of Eclipse Luna, Indigo and subclipse in a settings.xml file is strongly recommended](https://wiki.eclipse.org/Tycho/Target_Platform/Authentication_and_Mirrors), otherwise downloading the build artifacts may last for hours. The mirror ids are expected to be, in order, the following: ```eclipse-luna```, ```indigo-repo```, ```subclipse-repo```.

The output will be an Eclipse P2 repository, placed in this directory:

```
$BUILDDIR/hu.qgears.opensource.repository/target/repository/
```

Recommended Eclipse configuration:
* Set the Java heap size used by Eclipse at least to 1GBytes by adding the -Xmx1024m line to eclipse.ini.
 * Start Eclipse.
 * 'Help' menu -> 'Install new software' -> 'Add' button in the dialog.
  * Here, set the following parameters:

   * 'Name' -> QGears Review tool - Local maven build output 
   * 'Local' button -> browse to the '$BUILDDIR/hu.qgears.opensource.repository/target/repository/' directory and select it

Press OK.

Check the 'Q-Gears review tool' item.
Check the 'Contact all update sites during install to find required software' box

Click 'Finish'.

Accept the prompt that warns on installing software without certificate.

Restart Eclipse.

The installation is done; you can proceed to the [wiki](https://github.com/qgears/qgears-review-tool/wiki) to learn how to how to configure the review tool and assemble a workspace.
