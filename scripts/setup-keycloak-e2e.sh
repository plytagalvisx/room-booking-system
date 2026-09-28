#!/usr/bin/env bash

set -euo pipefail

: "${E2E_USERNAME:?E2E_USERNAME is required}"
: "${E2E_PASSWORD:?E2E_PASSWORD is required}"

REALM="room-booking"
CLIENT_ID="room-booking-frontend"

KEYCLOAK_ADMIN_USERNAME="${KEYCLOAK_ADMIN_USERNAME:-admin}"
KEYCLOAK_ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin}"

kc() {
    docker compose exec -T keycloak \
        /opt/keycloak/bin/kcadm.sh "$@"
}

echo "Authenticating Keycloak Admin CLI..."

kc config credentials \
    --server http://localhost:8080 \
    --realm master \
    --user "$KEYCLOAK_ADMIN_USERNAME" \
    --password "$KEYCLOAK_ADMIN_PASSWORD"

echo "Checking realm..."

if ! kc get "realms/$REALM" >/dev/null 2>&1
then
    echo "Creating realm $REALM..."

    kc create realms \
        -s "realm=$REALM" \
        -s enabled=true
fi

echo "Checking USER role..."

if ! kc get "roles/USER" \
    -r "$REALM" >/dev/null 2>&1
then
    kc create roles \
        -r "$REALM" \
        -s name=USER
fi

echo "Checking frontend client..."

if ! kc get clients \
    -r "$REALM" \
    -q "clientId=$CLIENT_ID" \
    | grep -q "$CLIENT_ID"
then
    echo "Creating frontend client..."

    kc create clients \
        -r "$REALM" \
        -s "clientId=$CLIENT_ID" \
        -s enabled=true \
        -s publicClient=true \
        -s standardFlowEnabled=true \
        -s directAccessGrantsEnabled=false \
        -s 'redirectUris=["http://localhost:5173/*"]' \
        -s 'webOrigins=["http://localhost:5173"]'
fi

echo "Checking E2E user..."

if ! kc get users \
    -r "$REALM" \
    -q "username=$E2E_USERNAME" \
    | grep -q "$E2E_USERNAME"
then
    echo "Creating E2E user..."

    kc create users \
        -r "$REALM" \
        -s "username=$E2E_USERNAME" \
        -s "email=e2e@example.com" \
        -s "firstName=E2E" \
        -s "lastName=User" \
        -s emailVerified=true \
        -s enabled=true
fi

echo "Setting E2E user password..."

kc set-password \
    -r "$REALM" \
    --username "$E2E_USERNAME" \
    --new-password "$E2E_PASSWORD"

echo "Assigning USER role..."

kc add-roles \
    -r "$REALM" \
    --uusername "$E2E_USERNAME" \
    --rolename USER \
    >/dev/null 2>&1 || true

echo "Keycloak E2E configuration ready."

# In this file we created a dedicated E2E Keycloak setup script.