# Build
FROM maven:3.9.12-eclipse-temurin-21 AS build
WORKDIR /app
COPY ./api-gateway ./api-gateway
WORKDIR /app/api-gateway
RUN mvn clean package -DskipTests

# Run
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/api-gateway/target/*.jar api-gateway.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]