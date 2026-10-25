# The application: API and web UI in one jar.
# Build from the repository root: docker build -f deploy/app.Dockerfile -t atlas .

FROM node:24 AS ui
RUN corepack enable
WORKDIR /build/atlas_ui
COPY atlas_ui/package.json atlas_ui/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY atlas_ui ./
RUN pnpm build

FROM eclipse-temurin:25-jdk AS backend
WORKDIR /build/atlas_backend
COPY atlas_backend ./
# The UI is built above, so Gradle is told to skip it and bundles the result.
COPY --from=ui /build/atlas_ui/dist ../atlas_ui/dist
RUN ./gradlew bootJar -PskipUi --no-daemon

FROM eclipse-temurin:25-jre
RUN useradd --system --create-home atlas
USER atlas
WORKDIR /app
COPY --from=backend /build/atlas_backend/build/libs/atlas_backend.jar ./atlas_backend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "atlas_backend.jar"]
