@echo off
set JAVA_HOME = C:\Program Files\BellSoft\LibericaJDK-11\

rem Import CA certificates into Java keystore
rem STEP-CA
%JAVA_HOME%\bin\keytool -cacerts -storepass changeit -noprompt -importcert -alias rootca -file step_ca_root_ca.crt
%JAVA_HOME%\bin\keytool -cacerts -storepass changeit -noprompt -importcert -alias intermediateca -file step_ca_intermediate_ca.crt
rem CRYPTO PRO KEYLAB
%JAVA_HOME%\bin\keytool -cacerts -storepass changeit -noprompt -importcert -alias keylabca -file keylab_tls_ca.crt
rem CIFROTECH PESOK
%JAVA_HOME%\bin\keytool -cacerts -storepass changeit -noprompt -importcert -alias ssorpdroot -file _.test.st.crt
%JAVA_HOME%\bin\keytool -cacerts -storepass changeit -noprompt -importcert -alias ssorpdintermediate -file sso.rpd.zk.test.st.crt
rem ZK TEST
%JAVA_HOME%\bin\keytool -cacerts -storepass changeit -noprompt -importcert -alias ssorpdroot -file _.zk.test.st.crt
%JAVA_HOME%\bin\keytool -cacerts -storepass changeit -noprompt -importcert -alias ssorpdintermediate -file _.app-sso01.rpd.zk.test.st.crt
rem OK TEST
%JAVA_HOME%\bin\keytool -cacerts -storepass changeit -noprompt -importcert -alias ssorpdroot -file _.ok.test.st.crt
%JAVA_HOME%\bin\keytool -cacerts -storepass changeit -noprompt -importcert -alias ssorpdintermediate -file _.app-sso01.rpd.ok.test.st.crt
rem Extract security.zip to JAVA_HOME/conf
"C:\Program Files\7-Zip\7z.exe" x security.zip -o%JAVA_HOME%\conf -aoa

echo Completed!