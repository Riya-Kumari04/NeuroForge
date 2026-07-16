# Stage 1: Build stage using Maven and Java 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# 1. Copy only the pom.xml to download dependencies (improves build caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Lightweight Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 3. Create a non-root user for better security in production
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 4. Copy the executable jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# 5. Expose the port (matches the SERVER_PORT env variable we'll pass)
EXPOSE 8080

# 6. Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]