#!/usr/bin/env bash
# Wipes the PostgreSQL database the app points at, so the migrations can run again from scratch.
# DESTROYS ALL DATA in the schema "public". Reads the same variables as the app:
#   ATLAS_DB_POSTGRES_HOST, _PORT (default 5432), _DATABASE, _USER, _PASSWORD,
#   and _SSLMODE (default require)
# Usage: deploy/reset-db.sh --yes
# Needs Docker, which runs psql; no local PostgreSQL client is needed.
set -euo pipefail

: "${ATLAS_DB_POSTGRES_HOST:?ATLAS_DB_POSTGRES_HOST is not set}"
: "${ATLAS_DB_POSTGRES_DATABASE:?ATLAS_DB_POSTGRES_DATABASE is not set}"
: "${ATLAS_DB_POSTGRES_USER:?ATLAS_DB_POSTGRES_USER is not set}"
: "${ATLAS_DB_POSTGRES_PASSWORD:?ATLAS_DB_POSTGRES_PASSWORD is not set}"
host="$ATLAS_DB_POSTGRES_HOST"
port="${ATLAS_DB_POSTGRES_PORT:-5432}"
database="$ATLAS_DB_POSTGRES_DATABASE"

echo "This will delete everything in schema \"public\" of database \"$database\" on $host:$port."
if [[ "${1:-}" != "--yes" ]]; then
  echo "Run again with --yes to do it." >&2
  exit 1
fi

# A database on this machine is only reachable from the container through the host network.
network=()
if [[ "$host" == "localhost" || "$host" == "127.0.0.1" ]]; then
  network=(--network host)
fi

docker run --rm "${network[@]}" \
  -e PGHOST="$host" -e PGPORT="$port" -e PGDATABASE="$database" \
  -e PGUSER="$ATLAS_DB_POSTGRES_USER" -e PGPASSWORD="$ATLAS_DB_POSTGRES_PASSWORD" \
  -e PGSSLMODE="${ATLAS_DB_POSTGRES_SSLMODE:-require}" \
  postgres:18 psql -v ON_ERROR_STOP=1 \
  -c 'DROP SCHEMA public CASCADE' \
  -c 'CREATE SCHEMA public'

echo "Wiped. Restart the app so the migrations run again."
