# syntax=docker/dockerfile:1.6
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS=""

EXPOSE 8761

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
