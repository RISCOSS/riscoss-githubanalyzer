![](http://www.riscoss.eu/bin/download/Download/Software/logo_riscoss_DSP.png)
# Introduction

The [RISCOSS project](http://www.riscoss.eu) will offer novel risk identification, management and mitigation tools and methods for community-based and industry-supported OSS development. 

RISCOSS will deliver a decision-making management platform integrated in a business-oriented decision-making framework, which together support placing technical OSS adoption decisions into organizational, business strategy and broader OSS community context.

This is the main repository for the RISCOSS Platform code.

# Project structure

The project structure reflects the structure of the RISCOSS Platform architecture:

* riscoss-platform-dm contains the code of the Domain Manager (DM)
* riscoss-platform-rdr contains the code for the Risk Data Repository (RDR)
* riscoss-platform-analyser is the engine which calculates risk based on data-points stored in the RDR.
* riscoss-platform-jsmile is a wrapper around the proprietary jSmile project which is used by the riscoss-platform-analyser.

Please refer to the [RISCOSS White Paper](http://www.riscoss.eu/bin/download/Discover/Whitepaper/RISCOSS-Whitepaper.pdf) for a more detailed description.

# Building

## Prerequisites

In order to build the RISCOSS Platform you need to:

* Install [Java JDK7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
* Install [Maven 3.0.5](http://maven.apache.org)
* Set the environment variable `MAVEN_OPTS` to the value `-Xmx1024m -XX:MaxPermSize=256m`
* Put the file `settings.xml` in your local Maven directory (usually located under `.m2` in your home directory). The `settings.xml` file is available in the `etc` directory of this repository.
* Donwload and copy the [Smile](https://dslpitt.org/genie) binary library to `riscoss-platform-jsmile/src/main/resources/`. The library to copy depends on your operating system. For Linux/X86_64 it's called `libjsmile.so`
	
## How to build

To build the RISCOSS Platform just run the command `mvn install` from the root directory.

Depending on your connection speed, and the number of artifacts to download, it will take between 5 and 20 minutes to build the whole platform.

# Deploying the RISCOSS Platform (single domain)

After having built the RISCOSS Platform you will need the following artifacts in order to deploy it:

* The RISCOSS Platform Domain Manager WEB application, located in `riscoss-platform/riscoss-platform-dm/riscoss-platform-dm-war/target`
* The RISCOSS Platform Domain Manager XAR application, located in `riscoss-platform/riscoss-platform-dm/riscoss-platform-dm-distribution/riscoss-platform-dm-ui-wiki-all/target`
* The RISCOSS Platform Risk Data Repository WEB application, located in `riscoss-platform/riscoss-platform-rdr/riscoss-platform-rdr-war/target`
* A servlet container
* A database driver

In this document we will deploy the RISCOSS Platform using [Tomcat 7.0](http://tomcat.apache.org) and the [HSQLDB](http://hsqldb.org) database.

## Installing Tomcat

Please refer to http://tomcat.apache.org for installation instruction.

## Unpacking RISCOSS Platform WEB applications

* Create a directory `riscoss` in the `webapps` directory of your Tomcat installation.
* Unpack the RISCOSS Platform Domain Manager WEB application in this directory using the command `jar xvf riscoss-platform-dm-war-VERSION.war`
* Create a directory `rdr` in the `webapps` directory of your Tomcat installation.
* Unpack RISCOSS Platform Risk Data Repository WEB application in this directory using the command `jar xvf riscoss-platform-rdr-war-VERSION.war`

## Installing the database driver

* Download the HSQLDB database driver from `http://search.maven.org/remotecontent?filepath=hsqldb/hsqldb/1.8.0.10/hsqldb-1.8.0.10.jar`
* Copy it under `webapps/riscoss/WEB-INF/lib` of your Tomcat installation

## Starting the server and installing the RISCOSS Platform Domain Manager XAR application

* Open the file `webapps/riscoss/WEB-INF/xwiki.cfg` and uncomment the line `xwiki.superadminpassword=system`
* Start the Tomcat server using the script `bin/startup.sh` in your Tomcat installation.
* Open a browser and go to the following URL: `http://localhost:8080/riscoss/bin/login/XWiki/XWikiLogin`
* Login as `superadmin` with password `system`
* In the top `Wiki: xwiki` menu choose `Administration`
* Click on the button `Choose a file` and select on your filesystem the file `riscoss-platform-dm-ui-wiki-all.xar` located in `riscoss-platform/riscoss-platform-dm/riscoss-platform-dm-distribution/riscoss-platform-dm-ui-wiki-all/target` of the source code repository
* Click on the link `riscoss-platform-dm-ui-wiki-all.xar` in the availabe packages section. A list of documents should appear on the right half of the page.
* Go down this list and click on the `Import` button
* Once finished click on the `Log-out` button on the top right
* You can now re-login using the `Admin` user name, and `admin` as a password.

The RISCOSS Platform Domain Manager is built on top of the XWiki Platform. For more information you can also look at http://platform.xwiki.org/xwiki/bin/view/Main/Documentation


