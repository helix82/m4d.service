FROM nexus.essocode.ru/adoptopenjdk/maven-openjdk11 AS maven
WORKDIR /app
COPY ./build/settings.xml /usr/share/maven/conf/
COPY ../pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B
COPY ../ ./
RUN --mount=type=cache,target=/root/.m2 mvn clean install -Dmaven.test.skip=true
FROM nexus.essocode.ru/bellsoft/liberica-openjdk-debian:11
COPY ./build/cert/* $JAVA_HOME/lib/security
COPY ./build/security.zip /app/
ARG NEXUS_RAW_USERNAME
ARG NEXUS_RAW_PASSWORD
RUN apt-get update && \
    apt-get install -y \
    wget \
    unzip \
    tar \
    gnupg2 \
    fontconfig \
    && wget --no-check-certificate --user $NEXUS_RAW_USERNAME --password $NEXUS_RAW_PASSWORD https://nexus.essocode.ru/repository/ocode-raw/csp/linux-amd64_deb.tgz -O /tmp/linux-amd64_deb.tgz \
    && ls -lh /tmp/linux-amd64_deb.tgz \
    && mkdir -p /tmp/csp \
    && mkdir -p /xml/validation \
    && tar -xvzf /tmp/linux-amd64_deb.tgz -C /tmp \
    && /tmp/linux-amd64_deb/install.sh \
    && rm -rf /tmp/linux-amd64_deb.tgz /tmp/linux-amd64_deb \
    && apt-get clean && rm -rf /var/lib/apt/lists/* \
    && cd $JAVA_HOME/lib/security \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias stepcarootca -file step_ca_root_ca.crt \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias stepcaintermediateca -file step_ca_intermediate_ca.crt \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias keylabtlsca -file keylab_tls_ca.crt \
    && rm -rf  $JAVA_HOME/lib/security/step_ca_root_ca.crt \
    && rm -rf  $JAVA_HOME/lib/security/step_ca_intermediate_ca.crt \
    && rm -rf  $JAVA_HOME/lib/security/keylab_tls_ca.crt \
    && unzip -o /app/security.zip -d $JAVA_HOME/conf \
    && rm -rf  /app/security.zip \
    && mkdir -p /app \
    && mkdir -p /app/files/source \
    && mkdir -p /app/files/dscp
COPY ./build/ON_EMCHD_1_928_00_01_01_01.xsd /app/xml/validation/

ARG JAR_FILE=m4d-0.0.1-SNAPSHOT.jar
ENV PROJECT_HOME /app
COPY --from=maven /app/target/${JAR_FILE}  $PROJECT_HOME/${JAR_FILE}

WORKDIR $PROJECT_HOME

ENTRYPOINT ["sh", "-c", "java -jar /app/m4d-0.0.1-SNAPSHOT.jar"]
