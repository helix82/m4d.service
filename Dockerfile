FROM nexus.essocode.ru/maven:3.9.6-eclipse-temurin-11 AS maven
WORKDIR /app
RUN mkdir -p /root/.m2/repository
COPY settings.xml /usr/share/maven/conf/
COPY ./ /app/

RUN --mount=type=cache,target=/root/.m2/repository mvn install:install-file -Dfile=/app/libs/JCPRevCheck.jar -DgroupId=ru.cryptopro.jcp -DartifactId=jcp-jcpRevCheck -Dversion=5.0.42119-A -Dpackaging=jar
RUN --mount=type=cache,target=/root/.m2/repository mvn install:install-file -Dfile=/app/libs/JCPRevTools.jar -DgroupId=ru.cryptopro.jcp -DartifactId=jcp-jcpRevTools -Dversion=5.0.42119-A -Dpackaging=jar
RUN --mount=type=cache,target=/root/.m2/repository mvn install:install-file -Dfile=/app/libs/ASN1P.jar -DgroupId=ru.cryptopro.jcp -DartifactId=jcp-asn1p -Dversion=5.0.42119-A -Dpackaging=jar
RUN --mount=type=cache,target=/root/.m2/repository mvn install:install-file -Dfile=/app/libs/asn1rt.jar -DgroupId=ru.cryptopro.jcp -DartifactId=jcp-asn1rt -Dversion=5.0.42119-A -Dpackaging=jar
RUN --mount=type=cache,target=/root/.m2/repository mvn install:install-file -Dfile=/app/libs/CAdES.jar -DgroupId=ru.cryptopro.jcp -DartifactId=jcp-cades -Dversion=5.0.42119-A -Dpackaging=jar
RUN --mount=type=cache,target=/root/.m2/repository mvn install:install-file -Dfile=/app/libs/forms_rt.jar -DgroupId=ru.cryptopro.jcp -DartifactId=jcp-forms_rt -Dversion=5.0.42119-A -Dpackaging=jar
RUN --mount=type=cache,target=/root/.m2/repository mvn install:install-file -Dfile=/app/libs/J6Oscar.jar -DgroupId=ru.cryptopro.jcp -DartifactId=jcp-J6Oscar -Dversion=5.0.42119-A -Dpackaging=jar
RUN --mount=type=cache,target=/root/.m2/repository mvn install:install-file -Dfile=/app/libs/JCP.jar -DgroupId=ru.cryptopro.jcp -DartifactId=jcp-jcp -Dversion=5.0.42119-A -Dpackaging=jar
RUN --mount=type=cache,target=/root/.m2/repository mvn install:install-file -Dfile=/app/libs/JCPRequest.jar -DgroupId=ru.cryptopro.jcp -DartifactId=jcp-request -Dversion=5.0.42119-A -Dpackaging=jar
RUN --mount=type=cache,target=/root/.m2/repository mvn install:install-file -Dfile=/app/libs/JCPxml.jar -DgroupId=ru.cryptopro.jcp -DartifactId=jcp-xml -Dversion=5.0.42119-A -Dpackaging=jar
RUN --mount=type=cache,target=/root/.m2/repository mvn install:install-file -Dfile=/app/libs/JCryptoP.jar -DgroupId=ru.cryptopro.jcp -DartifactId=jcp-jcrypto -Dversion=5.0.42119-A -Dpackaging=jar
RUN --mount=type=cache,target=/root/.m2/repository mvn install:install-file -Dfile=/app/libs/J6CF.jar -DgroupId=ru.cryptopro.jcp -DartifactId=J6CF -Dversion=5.0.42119-A -Dpackaging=jar
RUN --mount=type=cache,target=/root/.m2/repository mvn install:install-file -Dfile=/app/libs/XMLDSigRI.jar -DgroupId=ru.cryptopro.jcp -DartifactId=XMLDSigRI -Dversion=5.0.42119-A -Dpackaging=jar
RUN --mount=type=cache,target=/root/.m2/repository mvn install:install-file -Dfile=/app/libs/AdES-core.jar -DgroupId=ru.cryptopro.jcp -DartifactId=AdES-core -Dversion=5.0.42119-A -Dpackaging=jar
# RUN --mount=type=cache,target=/root/.m2/repository mvn install:install-file -Dfile=/app/libs/JCSP.jar -DgroupId=ru.cryptopro.jcp -DartifactId=jcp-jscp -Dversion=5.0.42119-A -Dpackaging=jar
# RUN --mount=type=cache,target=/root/.m2/repository mvn install:install-file -Dfile=/app/libs/JCPinst.jar -DgroupId=ru.cryptopro.jcp -DartifactId=jcp-jcpinst -Dversion=5.0.42119-A -Dpackaging=jar

RUN --mount=type=cache,target=/root/.m2/repository mvn dependency:go-offline -B
RUN --mount=type=cache,target=/root/.m2/repository mvn clean package -Dmaven.test.skip=true
FROM nexus.essocode.ru/bellsoft/liberica-openjdk-alpine:11
ARG JAR_FILE=synapi-0.0.1-SNAPSHOT.jar
ENV PROJECT_HOME /app 
RUN mkdir -p /app \
    && mkdir -p /app/files/source \
    && mkdir -p /app/files/dscp
COPY --from=maven /app/target/${JAR_FILE}  $PROJECT_HOME/${JAR_FILE}
COPY root_ca.crt $JAVA_HOME/lib/security
COPY intermediate_ca.crt $JAVA_HOME/lib/security
RUN \
    cd $JAVA_HOME/lib/security \
    && keytool -keystore cacerts -storepass changeit -noprompt  -importcert -alias rootca -file root_ca.crt \
    && keytool -keystore cacerts -storepass changeit -noprompt  -importcert -alias intermediateca -file intermediate_ca.crt
WORKDIR $PROJECT_HOME

ENTRYPOINT ["java", "-jar", "synapi-0.0.1-SNAPSHOT.jar]
