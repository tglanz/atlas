# The application: API and web UI in one jar.
# Build from the repository root: docker build -f deploy/app.Dockerfile -t atlas .

FROM eclipse-temurin:25-jdk AS backend
WORKDIR /build/atlas_backend
COPY atlas_backend ./
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jre
RUN useradd --system --create-home atlas
USER atlas
WORKDIR /app
COPY --from=backend /build/atlas_backend/build/libs/atlas_backend.jar ./atlas_backend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "atlas_backend.jar"]
