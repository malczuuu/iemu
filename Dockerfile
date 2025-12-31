FROM node:24.12.0-slim AS webappbuilder

WORKDIR /home/node
COPY webapp/ ./
RUN npm i && npm run build


FROM gradle:9.2.1-jdk17 AS builder

USER root
WORKDIR /home/gradle/app

COPY . .

RUN rm -rf src/main/resources/static/*
COPY --from=webappbuilder /home/node/dist/iemu-webapp/browser src/main/resources/static/

RUN gradle install --no-daemon


FROM eclipse-temurin:17-alpine

WORKDIR /iemu

EXPOSE 4500 5693/udp

COPY --from=builder /home/gradle/app/build/install/iemu/ /iemu/
COPY --from=builder /home/gradle/app/data/ /iemu/data/

VOLUME /data

ENV DEFAULT_JVM_OPTS="\
-XX:+UseContainerSupport \
-XX:MaxRAMPercentage=75.0 \
-XX:+ExitOnOutOfMemoryError \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/tmp"

ENV JAVA_OPTS=""

ENV APP_ARGS=""

ENTRYPOINT ["/bin/sh", "-c", "/iemu/bin/iemu $APP_ARGS"]
