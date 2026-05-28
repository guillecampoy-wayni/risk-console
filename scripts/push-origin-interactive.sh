#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
THRESHOLD_PERCENT="80"
COVERAGE_MODE="block"
RUN_FRONTEND_BUILD="auto"
REMOTE="origin"
BRANCH=""

usage() {
  cat <<'USAGE'
Usage: scripts/push-origin-interactive.sh [options]

Interactive local validation, staged review, commit and push to origin.

Options:
  --threshold N             Minimum backend line coverage percentage. Default: 80.
  --coverage-mode block|warn
                            block prevents push below threshold; warn allows push after warning. Default: block.
  --frontend auto|always|skip
                            auto builds frontend only when tracked/staged frontend files changed. Default: auto.
  --remote NAME             Git remote. Default: origin.
  --branch NAME             Branch to push. Default: current branch.
USAGE
}

confirm() {
  local prompt="$1"
  local answer
  read -r -p "$prompt [y/N] " answer
  [[ "$answer" == "y" || "$answer" == "Y" || "$answer" == "yes" || "$answer" == "YES" ]]
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --threshold)
      THRESHOLD_PERCENT="${2:-}"
      shift 2
      ;;
    --coverage-mode)
      COVERAGE_MODE="${2:-}"
      shift 2
      ;;
    --frontend)
      RUN_FRONTEND_BUILD="${2:-}"
      shift 2
      ;;
    --remote)
      REMOTE="${2:-}"
      shift 2
      ;;
    --branch)
      BRANCH="${2:-}"
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

if [[ "$COVERAGE_MODE" != "block" && "$COVERAGE_MODE" != "warn" ]]; then
  echo "Invalid --coverage-mode value: $COVERAGE_MODE. Expected block or warn." >&2
  exit 2
fi

if [[ "$RUN_FRONTEND_BUILD" != "auto" && "$RUN_FRONTEND_BUILD" != "always" && "$RUN_FRONTEND_BUILD" != "skip" ]]; then
  echo "Invalid --frontend value: $RUN_FRONTEND_BUILD. Expected auto, always or skip." >&2
  exit 2
fi

cd "$ROOT_DIR"

if [[ -z "$BRANCH" ]]; then
  BRANCH="$(git branch --show-current)"
fi

if [[ -z "$BRANCH" ]]; then
  echo "Cannot detect current branch. Pass --branch explicitly." >&2
  exit 1
fi

if ! git remote get-url "$REMOTE" >/dev/null 2>&1; then
  echo "Remote '$REMOTE' does not exist." >&2
  exit 1
fi

echo "Current branch: $BRANCH"
echo "Remote: $REMOTE"
echo
git status --short
echo

if ! confirm "Run backend validation with JaCoCo now?"; then
  echo "Backend validation is mandatory before push." >&2
  exit 1
fi

"$ROOT_DIR/scripts/validate-coverage.sh" --threshold "$THRESHOLD_PERCENT" --mode "$COVERAGE_MODE"

should_build_frontend="false"
if [[ "$RUN_FRONTEND_BUILD" == "always" ]]; then
  should_build_frontend="true"
elif [[ "$RUN_FRONTEND_BUILD" == "auto" ]]; then
  if git diff --name-only HEAD -- frontend | grep -q .; then
    should_build_frontend="true"
  fi
fi

if [[ "$should_build_frontend" == "true" ]]; then
  if confirm "Run frontend build now?"; then
    cd "$ROOT_DIR/frontend"
    npm run build
    cd "$ROOT_DIR"
  else
    echo "Frontend build skipped by user." >&2
  fi
fi

echo
git status --short
echo

if confirm "Review and stage changes interactively with git add -p?"; then
  git add -p
fi

if git diff --cached --quiet; then
  echo "No staged changes. Nothing to commit or push." >&2
  exit 1
fi

echo
git diff --cached --stat
echo

if confirm "Open staged diff before commit?"; then
  git diff --cached
fi

if confirm "Create commit with git commit -v?"; then
  git commit -v
else
  echo "Commit cancelled. Nothing was pushed." >&2
  exit 1
fi

echo
git status --short
echo

if confirm "Push branch '$BRANCH' to '$REMOTE'?"; then
  git push "$REMOTE" "$BRANCH"
else
  echo "Push cancelled after commit." >&2
  exit 1
fi
