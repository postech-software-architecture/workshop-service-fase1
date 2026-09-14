# syntax=docker/dockerfile:1.7

########################################
# Stage 1 — build (Maven Wrapper + JDK 21)
########################################
FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

# Copia primeiro o wrapper e o pom para aproveitar o cache de layers:
# enquanto pom.xml/wrapper nao mudarem, o go-offline vem do cache.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Copia o codigo e empacota. Testes rodam no CI (mvnw verify), nao no build da imagem,
# para nao exigir banco/Testcontainers durante o docker build.
COPY src ./src
RUN ./mvnw -B -q clean package -DskipTests

########################################
# Stage 2 — runtime (JRE 21 slim, nao-root)
########################################
FROM eclipse-temurin:21-jre-alpine AS runtime

ARG OTEL_JAVA_AGENT_VERSION=2.16.0

RUN apk add --no-cache curl \
	&& curl -fsSL "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_JAVA_AGENT_VERSION}/opentelemetry-javaagent.jar" \
		-o /opentelemetry-javaagent.jar \
	&& addgroup -g 1000 -S spring \
	&& adduser -u 1000 -S -G spring -h /app -s /sbin/nologin spring

WORKDIR /app

# Glob evita amarrar a versao do artefato (ex.: 0.0.1-SNAPSHOT -> proximas versoes).
COPY --from=build /workspace/target/workshop-service-*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker
# The agent is always present in the image, but exporters are disabled by default.
# The AWS overlay enables OTLP and points it at the NRDOT collector service.
ENV OTEL_SERVICE_NAME=workshop-service \
	OTEL_SERVICE_VERSION=unknown \
	OTEL_RESOURCE_ATTRIBUTES=deployment.environment=local \
	OTEL_TRACES_EXPORTER=none \
	OTEL_METRICS_EXPORTER=none \
	OTEL_LOGS_EXPORTER=none

USER spring:spring

EXPOSE 8080

# HEALTHCHECK do Docker alinhado a liveness probe do K8s (Dev 3).
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
	CMD curl -fsS http://localhost:8080/actuator/health/liveness | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-javaagent:/opentelemetry-javaagent.jar", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
