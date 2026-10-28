# Stage 1: Build application using Maven and JDK 25
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy Maven wrapper and pom.xml first to cache dependencies
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
RUN chmod +x ./mvnw

# Copy source code and build executable jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Minimal runtime image
FROM eclipse-temurin:25-jre
WORKDIR /app

# Create a non-privileged user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy packaged jar from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
