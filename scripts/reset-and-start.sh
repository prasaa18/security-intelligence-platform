#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is required but was not found." >&2
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "Docker Compose v2 is required but was not found." >&2
  exit 1
fi

printf '%s\n' "Resetting Security Intelligence Platform from the repository defaults."
printf '%s\n' "WARNING: this deletes the MongoDB volume and all stored scan data."

cp .env.example .env
set -a
source .env
set +a

docker compose down --volumes --remove-orphans
docker compose build --no-cache
docker compose up -d

printf '%s\n' "Waiting for containers to become healthy..."
for attempt in $(seq 1 30); do
  if docker compose ps --status running | grep -q security-intel-backend; then
    if curl --fail --silent --show-error \
      --user "${APP_ADMIN_USERNAME:-admin}:${APP_ADMIN_PASSWORD:-StrongPass123!}" \
      http://127.0.0.1:4200/api/integrations/scans/github-actions/health >/dev/null; then
      break
    fi
  fi
  if [[ "$attempt" == "30" ]]; then
    docker compose ps
    docker compose logs --tail 100 backend mongodb
    exit 1
  fi
  sleep 2
done

MONGO_USER="$(sed -n 's/^MONGO_INITDB_ROOT_USERNAME=//p' .env)"
MONGO_PASSWORD="$(sed -n 's/^MONGO_INITDB_ROOT_PASSWORD=//p' .env)"
docker exec security-intel-mongodb mongosh \
  --quiet \
  -u "$MONGO_USER" \
  -p "$MONGO_PASSWORD" \
  --authenticationDatabase admin \
  --eval 'db.adminCommand({ping: 1})' >/dev/null

docker compose ps
printf '%s\n' "Platform is ready at http://127.0.0.1:4200"
printf '%s\n' "Admin login: admin / StrongPass123!"
