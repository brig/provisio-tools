FROM eclipse-temurin:17.0.19_10-jre-ubi9-minimal

ARG MAVEN_VERSION

COPY target/provisio-tools-${MAVEN_VERSION}-runner.jar /root/.provisio/provisio.jar
