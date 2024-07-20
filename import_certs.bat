@echo off
set JAVA_HOME = C:\Program Files\BellSoft\LibericaJDK-11\

rem Import CA certificates into Java keystore
%JAVA_HOME%\bin\keytool -cacerts -storepass changeit -noprompt -importcert -alias rootca -file root_ca.crt
%JAVA_HOME%\bin\keytool -cacerts -storepass changeit -noprompt -importcert -alias intermediateca -file intermediate_ca.crt
%JAVA_HOME%\bin\keytool -cacerts -storepass changeit -noprompt -importcert -alias keylabca -file koren.crt

rem Extract security.zip to JAVA_HOME/conf
"C:\Program Files\7-Zip\7z.exe" x security.zip -o%JAVA_HOME%\conf -aoa

echo Completed!