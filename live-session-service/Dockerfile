# -------- Build Stage --------
FROM ghcr.io/carlossg/maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn -B -q -e -C dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# -------- Run Stage --------
FROM eclipse-temurin:17-jdk-jammy

# ✅ NEW — needed by TexoraAnalysisService for audio extraction (ffmpeg)
# before sending audio to transcript-microservice. Same install as
# video-service/chat-service, for consistency.
RUN apt-get update && \
    apt-get install -y --no-install-recommends ffmpeg && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8097
ENTRYPOINT ["java", "-jar", "app.jar"]