#!/usr/bin/env bash
set -euo pipefail

docker compose down -v
docker compose up -d

echo "Waiting for Postgres to be healthy..."
for ((i = 1; i <= 30; i++)); do
    status="$(docker inspect --format='{{.State.Health.Status}}' uber-db 2>/dev/null || echo "starting")"
    [[ "$status" == "healthy" ]] && { echo "Postgres is healthy and re-seeded."; break; }
    sleep 2
    if [[ "$i" -eq 30 ]]; then
        echo "Postgres did not become healthy in time." >&2
        exit 1
    fi
done

echo "Waiting for backend..."
for ((i = 1; i <= 60; i++)); do
    curl -s -o /dev/null http://localhost:8080/api/auth/login && { echo "Backend is up."; break; }
    sleep 2
done

echo "Waiting for frontend..."
for ((i = 1; i <= 60; i++)); do
    curl -s -o /dev/null http://localhost:4200 && { echo "Frontend is up."; break; }
    sleep 2
done

echo "Everything is up and reseeded. Run E2E with:"
echo "  cd e2e-tests && mvn -Dchrome.binary=/usr/bin/chromium -Dwebdriver.chrome.driver=/usr/bin/chromedriver test"
