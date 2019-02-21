FROM node:10.15.0-alpine as frontbuilder

WORKDIR /home/node
COPY front/ ./
RUN npm i && npm run build


FROM gradle:5.1-jdk8-alpine as builder

USER root
COPY . .
RUN rm -rf src/main/resources/static/*
COPY --from=frontbuilder /home/node/dist/front/ src/main/resources/static/
RUN gradle build -i && chmod +x docker-entrypoint.sh


FROM openjdk:8u181-jre-alpine3.8

WORKDIR /

EXPOSE 4500 5693/udp

COPY --from=builder /home/gradle/build/libs/*.jar /app.jar
COPY --from=builder /home/gradle/docker-entrypoint.sh /docker-entrypoint.sh
COPY --from=builder /home/gradle/data/config.yml /data/config.yml

VOLUME /data

ENTRYPOINT [ "/docker-entrypoint.sh" ]
