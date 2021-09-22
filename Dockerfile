FROM openjdk:11
MAINTAINER digambergupta

ARG JAR_NAME

COPY $JAR_NAME app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

EXPOSE 8080
