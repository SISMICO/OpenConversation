# syntax=docker/dockerfile:1

# --- Build stage ---
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Gradle wrapper and build scripts first to leverage layer caching
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

# Download Gradle dependencies separately from source code changes
RUN ./gradlew dependencies --no-daemon

# Copy source code and build the executable JAR
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine

# Create a non-root user with deterministic IDs for volume ownership
ARG APP_UID=1000
ARG APP_GID=1000
RUN addgroup -g ${APP_GID} -S appgroup && adduser -u ${APP_UID} -S appuser -G appgroup

WORKDIR /app

# Install ffmpeg for audio format conversion before Whisper transcription
RUN apk add --no-cache ffmpeg

# Copy the generated JAR from the build stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Ensure the non-root user owns the application files
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget --server-response --quiet --output-document=/dev/null http://localhost:8081/actuator/health 2>&1 | grep -q 'HTTP/1.1 200' || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
