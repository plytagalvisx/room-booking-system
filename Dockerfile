# This is the base image. This is cool because we can run this instance without having Java installed on our machine
#FROM openjdk:17-jdk-alpine # This is deprecated?
FROM amazoncorretto:25-alpine

# We copy the target file so that we can run it later with a different name
COPY target/room-booking-system-0.0.1-SNAPSHOT.jar app-1.0.0.jar

ENTRYPOINT [ "java", "-jar", "app-1.0.0.jar" ]

# The next thing we need to do is to define the Java application (docker) service in the docker-compose-yml file.

# Or, even better, avoid tying Docker to the exact Maven name:
# FROM amazoncorretto:25-alpine
# COPY target/*.jar app.jar
# ENTRYPOINT ["java", "-jar", "app.jar"]