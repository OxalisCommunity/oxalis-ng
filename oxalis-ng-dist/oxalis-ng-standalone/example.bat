@echo off
REM
REM Sends a sample invoice to Remote Access point
@echo on
java -jar oxalis-ng-distribution-1.0.0-distro\bin\oxalis-ng-standalone.jar -f Sample.xml

@echo off

REM Sends a sample invoice to your own local access point
@echo on
java -jar oxalis-ng-distribution-1.0.0-distro\bin\oxalis-ng-standalone.jar -f Sample.xml -u http://localhost:8080/oxalis-ng/as4 --cert /path/to/your/POP000XXX_Test_AP.cer

@echo off