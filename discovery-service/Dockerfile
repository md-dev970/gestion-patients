# STAGE 1: Build & Test
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
# Use BuildKit cache mount for Maven local repository: --mount=type=cache,target=/root/.m2
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B || true

# Copy source code
COPY src ./src

# Run tests here. If this fails, the image build fails.
# Added network resilience flags for better handling of connection issues
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package \
    -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -Dmaven.wagon.http.retryHandler.count=3 \
    -B \
    --fail-at-end \
    -Dsurefire.printSummary=true

# STAGE 2: Run (Tiny Image)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
# Only copy the JAR from the previous stage
COPY --from=builder /app/target/*.jar app.jar
USER spring:spring
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
