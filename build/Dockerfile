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
    && unzip -o /app/security.zip -d $JAVA_HOME/conf \
    && rm -rf  /app/security.zip

COPY ./build/ON_EMCHD_1_928_00_01_01_01.xsd /app/xml/validation/

ARG JAR_FILE=m4d-0.0.1-SNAPSHOT.jar
ENV PROJECT_HOME /app
COPY --from=maven /app/target/${JAR_FILE}  $PROJECT_HOME/${JAR_FILE}

WORKDIR $PROJECT_HOME

ENTRYPOINT ["sh", "-c", "java -jar /app/m4d-0.0.1-SNAPSHOT.jar"]
