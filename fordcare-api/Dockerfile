# ==========================================================
# FORDCARE INTELLIGENCE
# Secure Multi-Stage Docker Build
# ==========================================================

# ----------------------------------------------------------
# STAGE 1 - BUILD
# ----------------------------------------------------------

FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean package -DskipTests


# ----------------------------------------------------------
# STAGE 2 - RUNTIME
# ----------------------------------------------------------

FROM eclipse-temurin:21-jre-alpine

# Usuário dedicado sem privilégios administrativos
RUN addgroup -S fordcare && \
    adduser -S fordcare -G fordcare

WORKDIR /app

COPY --from=build \
    --chown=fordcare:fordcare \
    /app/target/*.jar \
    app.jar

# Aplicação não executa como root
USER fordcare

# Porta pública da API
EXPOSE 8080

# Porta interna de gerenciamento
EXPOSE 9091

HEALTHCHECK \
    --interval=30s \
    --timeout=5s \
    --start-period=30s \
    --retries=3 \
    CMD wget --no-verbose --tries=1 --spider \
    http://localhost:9091/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]