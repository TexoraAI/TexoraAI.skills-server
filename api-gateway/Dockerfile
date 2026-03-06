# -------- Build Stage --------
FROM ghcr.io/carlossg/maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom first (better caching)
COPY pom.xml .

# Download dependencies
RUN mvn -B -q -e -C dependency:go-offline

# Copy source code
COPY src ./src

# Build jar
RUN mvn clean package -DskipTests


# -------- Run Stage --------
FROM ghcr.io/adoptium/temurin:17-jdk-jammy

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 9000

ENTRYPOINT ["java","-jar","app.jar"]