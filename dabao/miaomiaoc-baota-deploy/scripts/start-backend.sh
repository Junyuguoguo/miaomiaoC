#!/usr/bin/env bash
set -euo pipefail

APP_HOME="$(cd "$(dirname "$0")/.." && pwd)"
JAR_PATH="$APP_HOME/backend/backend-0.0.1-SNAPSHOT.jar"
CONFIG_PATH="$APP_HOME/backend/config/application.yml"
PID_FILE="$APP_HOME/backend/backend.pid"
LOG_DIR="$APP_HOME/logs"
LOG_FILE="$LOG_DIR/backend.log"

mkdir -p "$LOG_DIR"

if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
  echo "Backend is already running, pid=$(cat "$PID_FILE")"
  exit 0
fi

cd "$APP_HOME"
nohup java -Xms256m -Xmx1024m -jar "$JAR_PATH" \
  --spring.config.location="file:$CONFIG_PATH" \
  > "$LOG_FILE" 2>&1 &

echo $! > "$PID_FILE"
echo "Backend started, pid=$(cat "$PID_FILE")"
echo "Log: $LOG_FILE"
