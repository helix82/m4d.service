#FROM nexus.essocode.ru:5000/maven:3.9.6-eclipse-temurin-11 AS maven
FROM maven:3.9.6-eclipse-temurin-11 AS maven

WORKDIR /app
COPY ./ /app/
RUN mkdir -p /root/.m2/repository
# COPY settings.xml /root/.m2/
# RUN --mount=type=cache,target=/root/.m2/repository mvn dependency:go-offline -B
# RUN --mount=type=cache,target=/root/.m2/repository mvn clean package -Dmaven.test.skip=true

#FROM nexus.essocode.ru:5000/liberica-openjdk-alpine:11
FROM bellsoft/liberica-openjdk-alpine:11
ARG JAR_FILE=synapi-0.0.1-SNAPSHOT.jar
ENV PROJECT_HOME /app 
RUN mkdir -p /app 
COPY --from=maven /app/target/${JAR_FILE}  $PROJECT_HOME/${JAR_FILE}
WORKDIR $PROJECT_HOME

ENTRYPOINT ["java", "-jar", "synapi-0.0.1-SNAPSHOT.jar]
