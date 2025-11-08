# Use OpenJDK 17
FROM eclipse-temurin:17-jdk-alpine

# Create app directory
WORKDIR /app

# Copy Maven wrapper & project files
COPY . .

# Build the app (skip tests to save time)
RUN ./mvnw -DskipTests package

# Expose port (same as Spring Boot default)
EXPOSE 8080

# Run jar file
CMD ["java", "-jar", "target/*.jar"]
