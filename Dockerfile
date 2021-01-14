FROM openjdk:13-jdk-alpine
EXPOSE 8080
WORKDIR /opt/biketeam
ARG JAR_FILE=target/biketeam.jar
COPY ${JAR_FILE} biketeam.jar
ENTRYPOINT ["java", "-jar", "biketeam.jar", "--spring.config.location=classpath:/application.properties,./application-custom.properties"]