@echo off

cd /d "%~dp0"

set JAVA_HOME=C:\Program Files\Java\jdk-26.0.1
set PATH=%JAVA_HOME%\bin;C:\Program Files\Apache\Maven\apache-maven-3.9.15\bin;%PATH%

mvn -s maven-settings.xml spring-boot:run -Dspring-boot.run.profiles=demo

pause