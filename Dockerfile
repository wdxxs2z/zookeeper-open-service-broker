FROM maven:3.5-jdk-8-alpine

RUN apk --update add git openssh && \
    rm -rf /var/lib/apt/lists/* && \
    rm /var/cache/apk/*
    
RUN git clone https://github.com/wdxxs2z/zookeeper-open-service-broker
WORKDIR zookeeper-open-service-broker
RUN mvn clean install

RUN cp ./target/*.jar /app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
EXPOSE 8080