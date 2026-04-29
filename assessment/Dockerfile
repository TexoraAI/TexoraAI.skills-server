# -------- Build Stage --------
FROM ghcr.io/carlossg/maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn -B -q -e -C dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# -------- Run Stage --------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# ✅ Install Python, Node.js, MySQL client, bash for code execution
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3 \
    python3-pip \
    curl \
    bash \
    default-mysql-client \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# ✅ Create workspace dir for bash persistent student sessions
RUN mkdir -p /root/lms_workspaces && chmod 777 /root/lms_workspaces

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]