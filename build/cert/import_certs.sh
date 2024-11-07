#!/bin/bash

# Set JAVA_HOME
JAVA_HOME="/opt/bellsoft/libericajdk-11"

# Import CA certificates into Java keystore
# STEP-CA
$JAVA_HOME/bin/keytool -cacerts -storepass changeit -noprompt -importcert -alias rootca -file step_ca_root_ca.crt
$JAVA_HOME/bin/keytool -cacerts -storepass changeit -noprompt -importcert -alias intermediateca -file step_ca_intermediate_ca.crt
# CRYPTO PRO KEYLAB
$JAVA_HOME/bin/keytool -cacerts -storepass changeit -noprompt -importcert -alias keylabca -file keylab_tls_ca.crt
# CIFROTECH PESOK
$JAVA_HOME/bin/keytool -cacerts -storepass changeit -noprompt -importcert -alias keylabca -file _.test.st.crt
$JAVA_HOME/bin/keytool -cacerts -storepass changeit -noprompt -importcert -alias keylabca -file sso.rpd.zk.test.st.crt
# ZK TEST
$JAVA_HOME/bin/keytool -cacerts -storepass changeit -noprompt -importcert -alias keylabca -file _.zk.test.st.crt
$JAVA_HOME/bin/keytool -cacerts -storepass changeit -noprompt -importcert -alias keylabca -file _.app-sso01.rpd.zk.test.st.crt
# OK TEST
$JAVA_HOME/bin/keytool -cacerts -storepass changeit -noprompt -importcert -alias keylabca -file _.ok.test.st.crt
$JAVA_HOME/bin/keytool -cacerts -storepass changeit -noprompt -importcert -alias keylabca -file _.app-sso01.rpd.ok.test.st.crt

# Extract security.zip to JAVA_HOME/conf
/usr/bin/7z x security.zip -o$JAVA_HOME/conf -aoa

echo "Completed!"