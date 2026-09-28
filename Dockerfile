# This is the base image. This is cool because we can run this instance without having Java installed on our machine
#FROM openjdk:17-jdk-alpine # This is deprecated?
#FROM amazoncorretto:25-alpine

# We copy the target file so that we can run it later with a different name
#COPY target/room-booking-system-0.0.1-SNAPSHOT.jar app-1.0.0.jar

#ENTRYPOINT [ "java", "-jar", "app-1.0.0.jar" ]

# The next thing we need to do is to define the Java application (docker) service in the docker-compose-yml file.

# Or, even better, avoid tying Docker to the exact Maven name:
# FROM amazoncorretto:25-alpine
# COPY target/*.jar app.jar
# ENTRYPOINT ["java", "-jar", "app.jar"]

# Updated Dockerfile for production deployment (We want the Docker to create a JAR file on its own
# (instead depending on an already existing JAR file) from the Maven project and then run it in a separate stage):
# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

COPY src src

RUN ./mvnw clean package -DskipTests


# ---------- Runtime stage ----------
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

