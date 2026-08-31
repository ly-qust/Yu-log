#!/usr/bin/env sh
set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
BACKUP_DIR="${BACKUP_DIR:-$ROOT_DIR/backups}"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"

if [ -f "$ROOT_DIR/.env" ]; then
  set -a
  . "$ROOT_DIR/.env"
  set +a
fi

MYSQL_DATABASE="${MYSQL_DATABASE:-yu_log}"
MYSQL_USER="${MYSQL_USER:-yu_log}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-change_me_mysql_password}"
OUTPUT="$BACKUP_DIR/${MYSQL_DATABASE}-${TIMESTAMP}.sql"

mkdir -p "$BACKUP_DIR"
docker compose -f "$ROOT_DIR/docker-compose.yml" exec -T mysql \
  mysqldump --single-transaction --quick --no-tablespaces \
  -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" > "$OUTPUT"

echo "Database backup written to $OUTPUT"
