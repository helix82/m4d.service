FROM nexus.essocode.ru/adoptopenjdk/maven-openjdk11 AS maven
WORKDIR /app
COPY settings.xml /usr/share/maven/conf/
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B
COPY ./ ./
RUN --mount=type=cache,target=/root/.m2 mvn clean install -Dmaven.test.skip=true
FROM nexus.essocode.ru/bellsoft/liberica-openjdk-debian:11
ARG USERNAME
ARG PASSWORD
RUN apt-get update && \
    apt-get install -y \
    wget \
    unzip \
    tar \
    gnupg2 \
    fontconfig \
    && rm -rf /var/lib/apt/lists/* \
    && wget --no-check-certificate --user $USERNAME --password $PASSWORD https://nexus.essocode.ru/repository/ocode-raw/csp/linux-amd64_deb.tgz -O /tmp/linux-amd64_deb.tgz \
    && ls -lh /tmp/linux-amd64_deb.tgz \
    && mkdir -p /tmp/csp \
    && mkdir -p /xml/validation \
    && tar -xvzf /tmp/linux-amd64_deb.tgz -C /tmp \
    && /tmp/linux-amd64_deb/install.sh \
    && rm -rf /tmp/linux-amd64_deb.tgz /tmp/linux-amd64_deb \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

ARG JAR_FILE=synapi-0.0.1-SNAPSHOT.jar
ENV PROJECT_HOME /app 
COPY --from=maven /app/target/${JAR_FILE}  $PROJECT_HOME/${JAR_FILE}
# ACME TLS ROOT AND INTERMEDIATE CERTIFICATES, CRYPTO PRO KEYLAB VM ROOT CERTIFICATE
COPY ["root_ca.crt", "intermediate_ca.crt", "koren.crt", "$JAVA_HOME/lib/security"]
COPY security.zip /app
COPY ON_EMCHD_1_928_00_01_01_01.xsd /app/xml/validation
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
