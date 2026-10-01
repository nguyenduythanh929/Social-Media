# ---- Stage 1: build the jar ----
FROM maven:3.9-eclipse-temurin-22 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline        # cached layer: deps only re-download when pom.xml changes
COPY src ./src
RUN mvn -B package -DskipTests

# ---- Stage 2: small runtime image ----
FROM eclipse-temurin:22-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
