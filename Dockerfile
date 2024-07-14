FROM nexus.essocode.ru/adoptopenjdk/maven-openjdk11 AS maven
WORKDIR /app
COPY settings.xml /usr/share/maven/conf/
COPY ./ /app/
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B
RUN --mount=type=cache,target=/root/.m2 mvn clean package -Dmaven.test.skip=true
FROM nexus.essocode.ru/bellsoft/liberica-openjdk-alpine-musl:11
RUN apk add --no-cache fontconfig ttf-dejavu
ARG JAR_FILE=synapi-0.0.1-SNAPSHOT.jar
ENV PROJECT_HOME /app 
RUN mkdir -p /app \
    && mkdir -p /app/files/source \
    && mkdir -p /app/files/dscp
COPY --from=maven /app/target/${JAR_FILE}  $PROJECT_HOME/${JAR_FILE}
# ACME TLS ROOT AND INTERMEDIATE CERTIFICATES
COPY root_ca.crt $JAVA_HOME/lib/security
COPY intermediate_ca.crt $JAVA_HOME/lib/security
# CRYPTO PRO KEYLAB VM ROOT CERTIFICATE
COPY koren.cer $JAVA_HOME/lib/security
RUN \
    cd $JAVA_HOME/lib/security \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias rootca -file root_ca.crt \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias intermediateca -file intermediate_ca.crt \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias keylabca -file koren.cer \
    && keytool -keystore  /app/store.jks -storepass changeit -noprompt -importcert -alias keylabca -file koren.cer

COPY security.zip /app
RUN  unzip -o /app/security.zip -d $JAVA_HOME/conf \
&& rm -rf  /app/security.zip

WORKDIR $PROJECT_HOME

ENTRYPOINT ["java", "-jar", "synapi-0.0.1-SNAPSHOT.jar]
