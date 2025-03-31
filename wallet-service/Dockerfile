# Build stage (Maven with JDK 21)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage (JDK 21)
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar /app/wallet-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "wallet-service.jar"]