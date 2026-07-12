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

RUN apk add --no-cache curl \
	&& addgroup -S spring \
	&& adduser -S -G spring -h /app -s /sbin/nologin spring

WORKDIR /app

# Glob evita amarrar a versao do artefato (ex.: 0.0.1-SNAPSHOT -> proximas versoes).
COPY --from=build /workspace/target/workshop-service-*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker

USER spring:spring

EXPOSE 8080

# HEALTHCHECK do Docker alinhado a liveness probe do K8s (Dev 3).
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
	CMD curl -fsS http://localhost:8080/actuator/health/liveness | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
