#!/usr/bin/env bash
set -euo pipefail

APP_HOME="$(cd "$(dirname "$0")/.." && pwd)"
"$APP_HOME/scripts/stop-backend.sh"
sleep 2
"$APP_HOME/scripts/start-backend.sh"
