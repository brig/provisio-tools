FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0

ARG VERSION
ARG USER

COPY target/provisio-tools-${VERSION}-runner /root/.provisio/provisio
