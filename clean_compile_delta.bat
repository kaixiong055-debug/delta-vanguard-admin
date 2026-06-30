@echo off
set MVN="C:\Users\Administrator\.m2\wrapper\dists\apache-maven-3.9.12\59fe215c0ad6947fea90184bf7add084544567b927287592651fda3782e0e798\bin\mvn.cmd"
%MVN% -pl yudao-module-delta -am clean compile -DskipTests -f d:\delta-vanguard\delta-vanguard-admin\pom.xml
exit /b %ERRORLEVEL%
