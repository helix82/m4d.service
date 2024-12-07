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
    && mkdir -p /xml/validation \
    && apt-get clean && rm -rf /var/lib/apt/lists/* \
    && cd $JAVA_HOME/lib/security \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias ssorpdroot -file _.test.st.crt \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias ssorpdintermediate -file sso.rpd.zk.test.st.crt \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias zkssorpdroot -file _.zk.test.st.crt \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias zkssorpdapp -file _.app-sso01.rpd.zk.test.st.crt \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias okssorpdroot -file _.ok.test.st.crt \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias okssorpdapp -file _.app-sso01.rpd.ok.test.st.crt \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias zkssorpdpak -file cik_san.pem \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias zkssorpdpak2 -file cikrf_selfsign_CA.crt \
    && keytool -cacerts -storepass changeit -noprompt -importcert -alias okssorpdpak -file cikcp.ru \
    && rm -rf  $JAVA_HOME/lib/security/_.test.st.crt \
    && rm -rf  $JAVA_HOME/lib/security/sso.rpd.zk.test.st.crt \
    && rm -rf  $JAVA_HOME/lib/security/_.zk.test.st.crt \
    && rm -rf  $JAVA_HOME/lib/security/_.app-sso01.rpd.zk.test.st.crt \
    && rm -rf  $JAVA_HOME/lib/security/_.ok.test.st.crt \
    && rm -rf  $JAVA_HOME/lib/security/_.app-sso01.rpd.ok.test.st.crt \
    && rm -rf  $JAVA_HOME/lib/security/cik_san.pem \
    && rm -rf  $JAVA_HOME/lib/security/cikrf_selfsign_CA.crt \
    && rm -rf  $JAVA_HOME/lib/security/cikcp.ru \
    && unzip -o /app/security.zip -d $JAVA_HOME/conf \
    && rm -rf  /app/security.zip

COPY ./build/ON_EMCHD_1_928_00_01_01_01.xsd /app/xml/validation/

ARG JAR_FILE=m4d-0.0.1-SNAPSHOT.jar
ENV PROJECT_HOME /app
COPY --from=maven /app/target/${JAR_FILE}  $PROJECT_HOME/${JAR_FILE}

WORKDIR $PROJECT_HOME

ENTRYPOINT ["sh", "-c", "java -jar /app/m4d-0.0.1-SNAPSHOT.jar"]
