@echo off
set MVN="C:\Users\Administrator\.m2\wrapper\dists\apache-maven-3.9.12\59fe215c0ad6947fea90184bf7add084544567b927287592651fda3782e0e798\bin\mvn.cmd"
set POM=-f d:\delta-vanguard\delta-vanguard-admin\pom.xml

echo [%date% %time%] Step1: install -DskipTests...
%MVN% -pl yudao-module-delta -am install -DskipTests %POM% 2>&1
echo Step1 ExitCode=%ERRORLEVEL%

echo [%date% %time%] Step2: test yudao-module-delta...
%MVN% -pl yudao-module-delta test %POM% 2>&1
echo Step2 ExitCode=%ERRORLEVEL%

echo [%date% %time%] Done.
