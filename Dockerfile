# =========================
# 1️⃣ Build stage
# =========================
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory inside container
WORKDIR /app

# Copy pom.xml and download dependencies (for caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests
FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
