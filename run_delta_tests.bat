@echo off
set MVN="C:\Users\Administrator\.m2\wrapper\dists\apache-maven-3.9.12\59fe215c0ad6947fea90184bf7add084544567b927287592651fda3782e0e798\bin\mvn.cmd"
%MVN% -pl yudao-module-delta test -f d:\delta-vanguard\delta-vanguard-admin\pom.xml -o 2>&1 > d:\delta-vanguard\delta-vanguard-admin\test_output2.txt
echo EXIT=%ERRORLEVEL%
findstr /C:"Tests run:" /C:"BUILD" /C:"FAILURE" d:\delta-vanguard\delta-vanguard-admin\test_output2.txt
