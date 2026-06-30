@echo off
set MVN="C:\Users\Administrator\.m2\wrapper\dists\apache-maven-3.9.12\59fe215c0ad6947fea90184bf7add084544567b927287592651fda3782e0e798\bin\mvn.cmd"
echo [%DATE% %TIME%] Starting Delta module tests (with deps)...
%MVN% -pl yudao-module-delta -am test -DfailIfNoTests=false -f d:\delta-vanguard\delta-vanguard-admin\pom.xml 2>&1
set RESULT=%ERRORLEVEL%
echo [%DATE% %TIME%] Test exit code: %RESULT%
exit /b %RESULT%
