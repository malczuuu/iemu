FROM node:15.11.0-slim AS WebAppBuilder

WORKDIR /home/node
COPY webapp/ ./
RUN npm i && npm run build


FROM gradle:9.2.1-jdk25 AS JarBuilder

USER root
COPY . .
RUN rm -rf src/main/resources/static/*
COPY --from=WebAppBuilder /home/node/dist/front/ src/main/resources/static/
RUN gradle build -i


FROM eclipse-temurin:25-alpine

WORKDIR /

EXPOSE 4500 5693/udp

COPY --from=JarBuilder /home/gradle/build/libs/*.jar /app.jar
COPY --from=JarBuilder /home/gradle/data/ /data/

VOLUME /data

ENV JAVA_OPTS_DEFAULT="\
-XX:+UseContainerSupport \
-XX:MaxRAMPercentage=75.0 \
-XX:+ExitOnOutOfMemoryError \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/tmp"

ENV JAVA_OPTS_EXTRA=""

ENV APP_ARGS=""

CMD ["/bin/sh", "-c", "java $JAVA_OPTS_DEFAULT $JAVA_OPTS_EXTRA -jar app.jar $APP_ARGS"]
