[![Oxalis NG Master Build](https://github.com/OxalisCommunity/oxalis-ng/workflows/Oxalis-NG%20Master%20Build/badge.svg)](https://github.com/OxalisCommunity/oxalis-ng/actions?query=workflow%3A%22Oxalis-NG%20Master%20Build%22)
[![Maven Central](https://img.shields.io/maven-central/v/network.oxalis/oxalis-ng.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22network.oxalis%22%20AND%20a%3A%22oxalis-ng%22)
---
# Oxalis
[Oxalis](http://en.wikipedia.org/wiki/Common_wood_sorrel) is the leading open-source software implementation of OpenPeppol eDelivery Access Point (AS4) specifications. This repository was originally developed by Steinar Overbeck Cook (SendRegning) and later looked after by the Norwegian agency for Public Management and eGovernment (Difi) until March 2020.

Starting November 2020, Oxalis is being maintained by [**NorStella Oxalis Community**](https://www.oxalis.network/).

## Oxalis Community
Oxalis Community run as not-for-profit organization under NorStella Foundation based in Norway, dedicated to the continued support and development of Oxalis, to secure [Peppol](https://peppol.org/about/) compliance and value for its users. It is organized according to democratic non-for-profit principles and established as an independent and autonomous part of the NorStella association with independent budgets.

The goals of Oxalis Community:
- Secure sustainability and managed development of the Oxalis software
- Encourage continued implementation of eInvoicing and eProcurement using Peppol specifications.
- Support innovative Peppol-based services that promotes the goal of harmonized and interoperable processes.

## Oxalis-NG
Oxalis-NG is latest avatar of Oxalis. Oxalis-NG can be used either as a standalone component or can be integrated as an API component with your backend solution. Oxalis-NG comes with command line standalone component called `oxalis-ng-standalone` which you can use for sending messages. Oxalis-NG also persists inbound messages to the filesystem out of the box. Persistence have been modularized so you can provide your own implementation if you need to send inbound messages to a message queue, a workflow engine, a document archive etc.

Binary distribution is available both at [Maven Central](https://repo1.maven.org/maven2/network/oxalis/) and [GitHub](https://github.com/OxalisCommunity/oxalis-ng/releases).

## Latest Information
Oxalis users can find latest information either via [Oxalis Network site](https://www.oxalis.network/) or [Github Wiki](https://github.com/OxalisCommunity/oxalis-ng/wiki). In addition to that we encourage all Oxalis Members to make effective use of [Norstella Oxalis](https://norstellaoxalis.slack.com) slack channel to receive faster, up-to-date, consolidated, and real time information. By joining slack channel, you will get valuable insight and support for your day-to-day queries.

---
## Oxalis NG components

| Component | Type | Description |
| --------- | ---- | ----------- |
| oxalis-ng-inbound    | war  | Inbound access point implementation which runs on Tomcat (1) |
| oxalis-ng-outbound   | jar  | Outbound component for sending Peppol business documents (2) |
| oxalis-ng-standalone | main | Command line application for sending Peppol business documents (3) |

(1) Receives messages using AS4 protocol and stores them in the filesystem as default.

(2) Can be incorporated into any system which needs to send Peppol documents.

(3) Serves as example code on how to send a business documents using the oxalis-ng-outbound component.


## Installation

* make sure the latest version of Tomcat is installed. See [installation guide](/doc/installation.md) for additional details.
* make sure that Tomcat is up and running and that manager is available with user manager/manager
* make sure that Tomcat is also up and running on SSL at localhost:443 (unless you terminate SSL in front of Tomcat)
* make sure that ''your'' keystore is installed in a known directory (separate instructions for constructing the keystore)
* Create an `OXALIS_HOME` directory and edit the file `oxalis.conf`
* Add `OXALIS_HOME` environment variable to reference that directory
* Build Oxalis yourself (see below) or download the binary artifacts provided by Norstella from [Maven Central](https://search.maven.org)
  Search for "oxalis" and download the latest version of `oxalis-distribution`.
* Deploy `oxalis.war` to your Tomcat `webapps` directory
* Send a sample invoice; modify `example.sh` to your liking and execute it.
* See the [installation guide](/doc/installation.md) for more additional details.
* To install or replace the Peppol certificate, see the [keystore document](/doc/keystore.adoc).
* Oxalis is meant to be extended rather than changing the Oxalis source code.


## Troubleshooting

* `Sending failed ... Received fatal alert: handshake_failure` happens when Oxalis cannot establish HTTPS connection with the remote server.  Usually because destination AccessPoint has "poodle patched" their HTTPS server.  Oxalis v3.1.0 contains fixes for this, so you need to upgrade.  See the https://github.com/OxalisCommunity/oxalis/issues/197 for more info.

* `Provider net.sf.saxon.TransformerFactoryImpl not found` might be an XSLT implementation conflice between Oxalis and the [VEFA validator](https://github.com/difi/vefa-validator-app).  VEFA needs XSLT 2.0 and explicitly set Saxon 9 as the transformer engine to the JVM.  Since Saxon 9 is not used and included with Oxalis you'll end up with that error on the Oxalis side.  To get rid of the error make sure you run Oxalis and VEFA in separate Tomcats/JVM processes.

## Build from source

Note that the Oxalis "head" revision on *master* branch is often in "flux" and should be considered a "nightly build".
The official releases are tagged and may be downloaded by clicking on [Tags](https://github.com/OxalisCommunity/oxalis-ng/tags).

* make sure [Maven 3+](http://maven.apache.org/) is installed
* make sure [JDK 11](http://www.oracle.com/technetwork/java/javase/) is installed (the version we have tested with)
* pull the version of interest from [GitHub](https://github.com/OxalisCommunity/oxalis-ng).
* from `oxalis-ng` root directory run : `mvn clean install -Pdist`
* locate assembled artifacts in `oxalis-dist/oxalis-distribution/target/oxalis-distribution-<version.number>-distro/`


## Securing Oxalis

By default Oxalis publish the web addresss listed in the table below.  
The table describes their use and give some hints on how to secure those addresses.  
A pretty standard scenario is to use some kind of load balancer and SSL offloader in front of the appserver running Oxalis.  
This could be free/open software like [Nginx](http://nginx.org/) and Apache or commercial software like NetScaler and BigIP.  
All such front end software should be able to enforce security like the one suggested below.

| URL | Function | Transport | Security |
| --- | -------- | --------- | -------- |
| oxalis-ng/status | Status information, for internal use and debugging | HTTP/HTTPS | Internet access can be blocked |
| oxalis-ng/statistics | RAW statistics for DIFI | HTTPS with proper certificates | Used by DIFI to collect statistics |
