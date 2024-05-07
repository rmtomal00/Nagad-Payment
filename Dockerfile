FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ./target/Nagad-Payment-0.0.2-SNAPSHOT.jar Nagad_payment.jar
ENTRYPOINT ["java","-jar", "Nagad_payment.jar"]