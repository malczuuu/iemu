FROM node:15.11.0-slim as frontbuilder

WORKDIR /home/node
COPY front/ ./
RUN npm i && npm run build


FROM gradle:6.8-jdk11 as builder

USER root
COPY . .
RUN rm -rf src/main/resources/static/*
COPY --from=frontbuilder /home/node/dist/front/ src/main/resources/static/
RUN gradle build -i && chmod +x docker-entrypoint.sh


FROM openjdk:11.0.10-slim

WORKDIR /

EXPOSE 4500 5693/udp

COPY --from=builder /home/gradle/build/libs/*.jar /app.jar
COPY --from=builder /home/gradle/docker-entrypoint.sh /docker-entrypoint.sh
COPY --from=builder /home/gradle/data/ /data/

VOLUME /data

ENTRYPOINT [ "/docker-entrypoint.sh" ]
