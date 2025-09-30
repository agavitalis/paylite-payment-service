# 1️⃣ Build Stage: Use Maven to build the app
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build

# Copy only pom.xml first for better caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# 2️⃣ Runtime Stage: Use a lightweight JDK image
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /build/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
# Use -Dspring.config.name (env overrides allowed)
ENTRYPOINT ["sh","-c","exec java -Dspring.config.name=${SPRING_CONFIG_NAME:-application} -jar app.jar"]

