@echo off
set MVN="C:\Users\Administrator\.m2\wrapper\dists\apache-maven-3.9.12\59fe215c0ad6947fea90184bf7add084544567b927287592651fda3782e0e798\bin\mvn.cmd"
echo Running clean compile...
%MVN% -pl yudao-module-delta -am clean compile -DskipTests -f d:\delta-vanguard\delta-vanguard-admin\pom.xml 2>&1
set COMPILE_RESULT=%ERRORLEVEL%
echo COMPILE_RESULT=%COMPILE_RESULT%
if %COMPILE_RESULT% NEQ 0 exit /b %COMPILE_RESULT%
echo Running tests...
%MVN% -pl yudao-module-delta -am test -f d:\delta-vanguard\delta-vanguard-admin\pom.xml 2>&1
set TEST_RESULT=%ERRORLEVEL%
echo TEST_RESULT=%TEST_RESULT%
exit /b %TEST_RESULT%
