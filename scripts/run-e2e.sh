#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"
BACKEND_LOG="$ROOT_DIR/target/e2e-backend.log"

# Require credentials without hardcoding them.
: "${E2E_USERNAME:?E2E_USERNAME is required}"
: "${E2E_PASSWORD:?E2E_PASSWORD is required}"

cd "$ROOT_DIR"

echo "Starting PostgreSQL and Keycloak..."
docker compose up -d java_db keycloak

echo "Waiting for PostgreSQL..."

until docker compose exec -T java_db \
    pg_isready -U postgres -d postgres \
    >/dev/null 2>&1
do
    sleep 1
done

echo "PostgreSQL is ready."

# Create the E2E database if it does not already exist.
# Using "docker compose exec -T java_db" instead of "docker exec java_db" makes the script independent of generated container names.
if ! docker compose exec -T java_db \
    psql -U postgres -tAc \
    "SELECT 1 FROM pg_database WHERE datname='store_e2e'" \
    | grep -q 1
then
    echo "Creating store_e2e database..."

    docker compose exec -T java_db \
        createdb -U postgres store_e2e
fi

echo "Waiting for Keycloak..."

until curl -fsS \
    "http://localhost:8081/realms/master/.well-known/openid-configuration" \
    >/dev/null
do
    sleep 1
done

echo "Keycloak is ready."


# Insert a dedicated E2E Keycloak setup script: which uses Keycloak's official Admin CLI approach: authenticate, create realms/clients/users, set passwords, and assign roles.
echo "Configuring Keycloak for E2E..."

bash "$ROOT_DIR/scripts/setup-keycloak-e2e.sh"


echo "Waiting for room-booking realm..."

until curl -fsS \
    "http://localhost:8081/realms/room-booking/.well-known/openid-configuration" \
    >/dev/null
do
    sleep 1
done

echo "room-booking realm is ready."


# Do not accidentally run E2E against some other backend.
if lsof -nP -iTCP:8080 -sTCP:LISTEN \
    >/dev/null 2>&1
then
    echo ""
    echo "ERROR: Port 8080 is already in use."
    echo "Stop your local Spring backend before running E2E."
    exit 1
fi

mkdir -p "$ROOT_DIR/target"

echo "Starting Spring Boot with the e2e profile..."

./mvnw spring-boot:run \
    -Dspring-boot.run.profiles=e2e \
    >"$BACKEND_LOG" 2>&1 &

BACKEND_PID=$!

cleanup() {
    echo ""
    echo "Stopping E2E backend..."

    kill "$BACKEND_PID" 2>/dev/null || true
    wait "$BACKEND_PID" 2>/dev/null || true
}

trap cleanup EXIT INT TERM

echo "Waiting for Spring Boot..."

until curl -fsS \
    "http://localhost:8080/api/rooms" \
    >/dev/null
do
    if ! kill -0 "$BACKEND_PID" 2>/dev/null
    then
        echo ""
        echo "Spring Boot failed to start."
        echo "Backend log:"
        tail -n 100 "$BACKEND_LOG"
        exit 1
    fi

    sleep 1
done

echo "Spring Boot is ready."
echo "Running Playwright..."

cd "$FRONTEND_DIR"

npm run test:e2e

# In this file we created an E2E runner script