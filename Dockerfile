FROM nexus.essocode.ru/maven:3.9.6-eclipse-temurin-11 AS maven
WORKDIR /app
COPY settings.xml /usr/share/maven/conf/
COPY ./ /app/

RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B
RUN --mount=type=cache,target=/root/.m2 mvn clean package -Dmaven.test.skip=true
FROM nexus.essocode.ru/bellsoft/liberica-openjdk-alpine:11
ARG JAR_FILE=synapi-0.0.1-SNAPSHOT.jar
ENV PROJECT_HOME /app 
RUN mkdir -p /app \
    && mkdir -p /app/files/source \
    && mkdir -p /app/files/dscp
COPY --from=maven /app/target/${JAR_FILE}  $PROJECT_HOME/${JAR_FILE}
COPY root_ca.crt $JAVA_HOME/lib/security
COPY root_ca.crt $JAVA_HOME/lib/security
COPY koren.cer /app
RUN \
    cd $JAVA_HOME/lib/security \
    && keytool -keystore cacerts -storepass changeit -noprompt  -importcert -alias rootca -file root_ca.crt \
    && keytool -keystore cacerts -storepass changeit -noprompt  -importcert -alias intermediateca -file intermediate_ca.crt

RUN \
    cd /app \
    && keytool -importcert -alias keylabca -keystore  /app/store.jks -storepass changeit -file /app/koren.cer

WORKDIR $PROJECT_HOME

ENTRYPOINT ["java", "-jar", "synapi-0.0.1-SNAPSHOT.jar]
