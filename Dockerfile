FROM node:15.11.0-slim AS WebAppBuilder

WORKDIR /home/node
COPY webapp/ ./
RUN npm i && npm run build


FROM gradle:9.2.1-jdk25 AS AppBuilder

USER root
WORKDIR /home/gradle/app

COPY . .

RUN rm -rf src/main/resources/static/*
COPY --from=WebAppBuilder /home/node/dist/front/ src/main/resources/static/

RUN gradle install --no-daemon


FROM eclipse-temurin:25-alpine

WORKDIR /iemu

EXPOSE 4500 5693/udp

COPY --from=AppBuilder /home/gradle/app/build/install/iemu/ /iemu/
COPY --from=AppBuilder /home/gradle/app/data/ /iemu/data/

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
