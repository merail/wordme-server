#!/bin/bash
set -e

BACKUP_DIR="/opt/wordme/backups"
KEEP_DAYS=30

mkdir -p "$BACKUP_DIR"

TIMESTAMP=$(date +"%Y-%m-%d_%H-%M")

for DB in words ids; do
    BACKUP_FILE="$BACKUP_DIR/${DB}_$TIMESTAMP.db"
    sqlite3 "/opt/wordme/data/$DB.db" ".backup '$BACKUP_FILE'"
    echo "Backup created: $BACKUP_FILE"
done

find "$BACKUP_DIR" -name '*.db' -mtime +$KEEP_DAYS -delete
