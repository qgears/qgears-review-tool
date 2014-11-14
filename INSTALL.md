# Installation guide

This guide will help you through the installation of the QGears Review tool with the Eclipse user interface.

Assumed environment: ```Ubuntu 14.04.1```

Required software:

 * Eclipse Indigo 3.7 SR2
 * Java 1.7+ - (tested with openjdk-7-jre:amd64 7u71-2.5.3-0ubuntu0.14.04.1)
 * maven (tested with version 3.0.5-1)

# Steps

Create an arbitrary directory used for building the review tool! This directory will further be referred to as $BUILDDIR, then execute the following commands.
```
cd $BUILDDIR
git clone https://github.com/qgears/opensource-utils.git
git clone https://github.com/qgears/jopt-simple.git
git clone https://github.com/qgears/qgears-review-tool.git
```
Start the maven build in folder: $BUILDDIR/qgears-review-tool/hu.qgears.review.build/
```
mvn -DeclipseRepoUrl=http://download.eclipse.org/eclipse/updates/3.7 -DsubversiveUpdateSite=http://download.eclipse.org/technology/subversive/1.1/update-site package
```
Note that subversive update size can be one of the following:

 * http://download.eclipse.org/technology/subversive/1.1/update-site
 * http://download.eclipse.org/technology/subversive/2.0/update-site

The output will be an Eclipse P2 repository, placed in this directory:

```
$BUILDDIR/qgears-review-tool/hu.qgears.review.build/p2/target/repository
```
Set the Java heap size used by Eclipse at least to 1GBytes by adding the -Xmx1024m line to eclipse.ini.
Start Eclipse.
'Help' menu -> 'Install new software' -> 'Add' button in the dialog.
Here, set the following parameters:

 * 'Name' -> QGears Review tool - Local maven build output 
 * 'Local' button -> browse to the '$BUILDDIR/qgears-review-tool/hu.qgears.review.build/p2/target/repository' directory and select it

Press OK.

Allow installation of required components of the Subversive team provider; add the relevant repository to Eclipse with the parameters below, adjusted to the repository chosen in the maven execution step above:

 * 'Name' -> Subversive 1.1 update site
 * 'Location' -> http://download.eclipse.org/technology/subversive/1.1/update-site/
 
In the 'Install' window, select the 'QGears Review tool - Local maven build output' item from the 'Work with' repo list.

Check the 'Review tool' item.

Click 'Finish'.

Accept the prompt that warns on installing software without certificate.

Restart Eclipse.

The installation is done.
