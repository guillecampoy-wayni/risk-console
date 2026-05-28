#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
THRESHOLD_PERCENT="80"
MODE="block"

usage() {
  cat <<'USAGE'
Usage: scripts/validate-coverage.sh [--threshold 80] [--mode block|warn]

Runs backend tests with JaCoCo and validates line coverage.

Options:
  --threshold N       Minimum line coverage percentage. Default: 80.
  --mode block|warn  block fails when coverage is below threshold; warn only prints a warning. Default: block.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --threshold)
      THRESHOLD_PERCENT="${2:-}"
      shift 2
      ;;
    --mode)
      MODE="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 2
      ;;
  esac
done

if [[ ! "$THRESHOLD_PERCENT" =~ ^[0-9]+([.][0-9]+)?$ ]]; then
  echo "Invalid --threshold value: $THRESHOLD_PERCENT" >&2
  exit 2
fi

if [[ "$MODE" != "block" && "$MODE" != "warn" ]]; then
  echo "Invalid --mode value: $MODE. Expected block or warn." >&2
  exit 2
fi

threshold_ratio="$(awk -v threshold="$THRESHOLD_PERCENT" 'BEGIN { printf "%.4f", threshold / 100 }')"

cd "$BACKEND_DIR"

if [[ "$MODE" == "block" ]]; then
  mvn clean verify "-Djacoco.minimum.coverage=$threshold_ratio"
else
  mvn clean verify "-Djacoco.minimum.coverage=0"
fi

coverage_csv="$BACKEND_DIR/target/site/jacoco/jacoco.csv"
if [[ ! -f "$coverage_csv" ]]; then
  echo "JaCoCo CSV report was not generated: $coverage_csv" >&2
  exit 1
fi

coverage_percent="$(
  awk -F, 'NR > 1 { missed += $8; covered += $9 }
    END {
      total = missed + covered
      if (total == 0) {
        print "0.00"
      } else {
        printf "%.2f", covered * 100 / total
      }
    }' "$coverage_csv"
)"

echo "JaCoCo line coverage: ${coverage_percent}%"
echo "Expected minimum: ${THRESHOLD_PERCENT}%"

comparison="$(awk -v actual="$coverage_percent" -v threshold="$THRESHOLD_PERCENT" 'BEGIN { print ((actual + 0) < (threshold + 0)) ? "below" : "ok" }')"

if [[ "$comparison" == "below" ]]; then
  if [[ "$MODE" == "warn" ]]; then
    echo "WARNING: coverage is below the expected threshold." >&2
    exit 0
  fi

  echo "ERROR: coverage is below the expected threshold." >&2
  exit 1
fi

echo "Coverage threshold met."
