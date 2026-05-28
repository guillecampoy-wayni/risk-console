#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
zip -r ../pomelo-risk-console-starter.zip . -x "*/node_modules/*" "*/target/*" "*.git*"
