#!/usr/bin/env bash
set -euo pipefail

curl -I http://127.0.0.1:8080/api/auth/verifyToken || true
echo
echo "If the backend is running, any HTTP response proves port 8080 is reachable."
