#!/bin/bash

# Set JAVA_HOME
JAVA_HOME="/opt/bellsoft/libericajdk-11"

# Import CA certificates into Java keystore
$JAVA_HOME/bin/keytool -cacerts -storepass changeit -noprompt -importcert -alias rootca -file root_ca.crt
$JAVA_HOME/bin/keytool -cacerts -storepass changeit -noprompt -importcert -alias intermediateca -file intermediate_ca.crt
$JAVA_HOME/bin/keytool -cacerts -storepass changeit -noprompt -importcert -alias keylabca -file koren.crt

# Extract security.zip to JAVA_HOME/conf
/usr/bin/7z x security.zip -o$JAVA_HOME/conf -aoa

echo "Completed!"