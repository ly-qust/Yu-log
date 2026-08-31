#!/usr/bin/env sh
set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
docker compose -f "$ROOT_DIR/docker-compose.yml" up -d --build
docker compose -f "$ROOT_DIR/docker-compose.yml" ps
