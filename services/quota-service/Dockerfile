# Build stage
FROM maven:4.0.0-rc-4-eclipse-temurin-21 AS build
WORKDIR /build
# Copia o monorepo inteiro
COPY . .
# Build completo para resolver o parent
RUN mvn clean install -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built artifact
COPY --from=build /build/services/quota-service/target/quota-service-*.jar app.jar

# Add a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose port
EXPOSE 8083

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]