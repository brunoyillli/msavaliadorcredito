FROM maven:3.8.6-openjdk-17 as build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17
WORKDIR /app
COPY --from=build ./app/target/*.jar ./app.jar

ARG RABBITMQ_SERVER=rabbitmq-host
ARG EUREKA_SERVER=eureka-host

ENTRYPOINT java -jar -Dspring.profiles.active=production app.jar