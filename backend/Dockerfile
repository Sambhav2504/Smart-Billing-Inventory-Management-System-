# Use JDK 17
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy everything
COPY . .

# Give execute permission to Maven wrapper
RUN chmod +x mvnw

# Build the app without tests
RUN ./mvnw -DskipTests package

# Expose backend port
EXPOSE 8080

# Run jar
CMD ["java", "-jar", "target/*.jar"]
