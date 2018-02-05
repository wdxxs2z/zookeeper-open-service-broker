FROM maven:3.5-jdk-8-alpine

WORKDIR zookeeper-open-service-broker
RUN mvn clean install
COPY target/*.jar /app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
EXPOSE 8080