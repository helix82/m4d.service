FROM nexus.essocode.ru/adoptopenjdk/maven-openjdk11 AS maven
WORKDIR /app
COPY settings.xml /usr/share/maven/conf/
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B
COPY ./ ./
RUN --mount=type=cache,target=/root/.m2 mvn clean install -Dmaven.test.skip=true
FROM nexus.essocode.ru/bellsoft/liberica-openjdk-alpine-musl:11
RUN apk add --no-cache fontconfig ttf-dejavu
ARG JAR_FILE=synapi-0.0.1-SNAPSHOT.jar
ENV PROJECT_HOME /app 
COPY --from=maven /app/target/${JAR_FILE}  $PROJECT_HOME/${JAR_FILE}
# ACME TLS ROOT AND INTERMEDIATE CERTIFICATES, CRYPTO PRO KEYLAB VM ROOT CERTIFICATE
COPY ["root_ca.crt", "intermediate_ca.crt", "koren.crt", "$JAVA_HOME/lib/security"]
COPY security.zip /app
RUN \
    cd $JAVA_HOME/lib/security \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias rootca -file root_ca.crt \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias intermediateca -file intermediate_ca.crt \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias keylabca -file koren.crt \
    && unzip -o /app/security.zip -d $JAVA_HOME/conf \
    && rm -rf  /app/security.zip \
    && mkdir -p /app \
    && mkdir -p /app/files/source \
    && mkdir -p /app/files/dscp

WORKDIR $PROJECT_HOME

ENTRYPOINT ["java", "-jar", "synapi-0.0.1-SNAPSHOT.jar]
