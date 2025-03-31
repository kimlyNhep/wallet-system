# Build stage (Maven)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage (JDK 17)
FROM maven:3.9-eclipse-temurin-21
WORKDIR /app
COPY --from=build /app/target/*.jar /app/user-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "user-service.jar"]