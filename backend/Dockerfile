# ── Stage 1: Build ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Cache the Maven dependency layer separately for faster rebuilds
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -q 2>/dev/null || \
    (apk add --no-cache maven && mvn dependency:go-offline -q)

COPY src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests --no-transfer-progress

# ── Stage 2: Runtime ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: run as non-root user
RUN apk add --no-cache wget && \
    addgroup -S forgemind && adduser -S forgemind -G forgemind
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# Create workspace directory
RUN mkdir -p /tmp/forgemind/workspaces && \
    chown -R forgemind:forgemind /app /tmp/forgemind

USER forgemind

EXPOSE 8080

# Use exec form to make the JVM the PID 1 process (receives OS signals correctly)
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
