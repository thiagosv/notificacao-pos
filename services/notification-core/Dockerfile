# Build stage
FROM maven:4.0.0-rc-4-eclipse-temurin-21 AS build
WORKDIR /build
# Copia o monorepo inteiro
COPY . .

RUN mvn clean package -DskipTests -pl services/notification-core -am

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built artifact
COPY --from=build /build/services/notification-core/target/notification-core-*.jar app.jar

# Add a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]

