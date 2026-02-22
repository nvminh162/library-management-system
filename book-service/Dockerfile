# Build
FROM maven:3.9.12-eclipse-temurin-21 AS build
WORKDIR /app

COPY ./common-service ./common-service
COPY ./book-service ./book-service

WORKDIR /app/common-service
# Has JAR
RUN mvn clean install package -DskipTests 
WORKDIR /app/book-service
# No JAR
RUN mvn clean package -DskipTests

# Run
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/book-service/target/*.jar book-service.jar
EXPOSE 9001
ENTRYPOINT ["java", "-jar", "book-service.jar"]
