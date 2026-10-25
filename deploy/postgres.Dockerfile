# A throwaway PostgreSQL for trying the app against Postgres. Not for production.
# docker build -f deploy/postgres.Dockerfile -t atlas-postgres deploy
# docker run --rm -p 5432:5432 atlas-postgres
#
# Then run the app with:
#   SPRING_PROFILES_ACTIVE=postgres
#   ATLAS_DB_POSTGRES_HOST=localhost
#   ATLAS_DB_POSTGRES_PORT=5432
#   ATLAS_DB_POSTGRES_DATABASE=atlas
#   ATLAS_DB_POSTGRES_USER=atlas
#   ATLAS_DB_POSTGRES_PASSWORD=atlas
#   ATLAS_DB_POSTGRES_SSLMODE=disable

FROM postgres:18
ENV POSTGRES_DB=atlas \
    POSTGRES_USER=atlas \
    POSTGRES_PASSWORD=atlas
EXPOSE 5432
