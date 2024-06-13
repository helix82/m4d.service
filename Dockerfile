FROM nexus.essocode.ru/maven:3.9.6-eclipse-temurin-11 AS maven
#FROM maven:3.9.6-eclipse-temurin-11 AS maven
WORKDIR /app
COPY pom.xml /app
RUN mkdir -p /root/.m2/repository
COPY settings.xml /usr/share/maven/conf/
COPY ./ /app/
RUN --mount=type=cache,target=/root/.m2/repository mvn dependency:go-offline -B
RUN --mount=type=cache,target=/root/.m2/repository mvn clean package -Dmaven.test.skip=true
FROM nexus.essocode.ru/bellsoft/liberica-openjdk-alpine:11
#FROM bellsoft/liberica-openjdk-alpine:11
ARG JAR_FILE=synapi-0.0.1-SNAPSHOT.jar
ENV PROJECT_HOME /app 
RUN mkdir -p /app 
COPY --from=maven /app/target/${JAR_FILE}  $PROJECT_HOME/${JAR_FILE}
COPY root_ca.crt $JAVA_HOME/lib/security
COPY intermediate_ca.crt $JAVA_HOME/lib/security
RUN \
    cd $JAVA_HOME/lib/security \
    && keytool -keystore cacerts -storepass changeit -noprompt  -importcert -alias rootca -file root_ca.crt \
    && keytool -keystore cacerts -storepass changeit -noprompt  -importcert -alias intermediateca -file intermediate_ca.crt
WORKDIR $PROJECT_HOME

ENTRYPOINT ["java", "-jar", "synapi-0.0.1-SNAPSHOT.jar]
