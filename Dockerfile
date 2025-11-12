# Stage 1: Build the Spring Boot app
FROM maven:3.9.8-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the app
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Set database environment variables
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://196.221.167.63:5432/mydb
ENV SPRING_DATASOURCE_USERNAME=admin
ENV SPRING_DATASOURCE_PASSWORD=Amenhitham2@

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
