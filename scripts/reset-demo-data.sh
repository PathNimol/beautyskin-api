#!/usr/bin/env bash
# Reset PostgreSQL and load fresh BeautySkin demo data (shops, products, demo users).
#
# Usage:
#   ./scripts/reset-demo-data.sh           # confirm, then reset + seed
#   ./scripts/reset-demo-data.sh --yes     # no confirmation prompt
#   ./scripts/reset-demo-data.sh --yes --keep-api   # do not stop app on :8080
#
# Demo logins after seed:
#   admin@beautyskin.com / admin123
#   owner@beautyskin.com / owner123
#   staff@beautyskin.com / staff123
#   buyer@beautyskin.com / buyer123
#
# Requires: psql (or Docker container beautyskin-postgres), Java 21, ./gradlew

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

AUTO_YES=false
KEEP_API=false
for arg in "$@"; do
  case "$arg" in
    --yes|-y) AUTO_YES=true ;;
    --keep-api) KEEP_API=true ;;
    -h|--help)
      sed -n '2,20p' "$0"
      exit 0
      ;;
    *)
      echo "Unknown option: $arg (try --help)" >&2
      exit 1
      ;;
  esac
done

# Load .env if present (DB_* and optional overrides)
if [[ -f "$ROOT/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$ROOT/.env"
  set +a
fi

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5434}"
DB_NAME="${DB_NAME:-beautyskin-db}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-151003}"

LOG_FILE="${TMPDIR:-/tmp}/beautyskin-demo-seed.log"
: >"$LOG_FILE"

info() { echo "==> $*"; }
die() { echo "ERROR: $*" >&2; exit 1; }

run_psql() {
  if command -v docker >/dev/null 2>&1 && docker ps --format '{{.Names}}' 2>/dev/null | grep -qx 'beautyskin-postgres'; then
    docker exec -i beautyskin-postgres psql -v ON_ERROR_STOP=1 -U "$DB_USER" -d "$DB_NAME" "$@"
  else
    export PGPASSWORD="$DB_PASSWORD"
    psql -v ON_ERROR_STOP=1 -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" "$@"
  fi
}

check_psql() {
  if command -v docker >/dev/null 2>&1 && docker ps --format '{{.Names}}' 2>/dev/null | grep -qx 'beautyskin-postgres'; then
    return 0
  fi
  command -v psql >/dev/null 2>&1 || die "psql not found. Start Postgres (docker compose up -d) or install PostgreSQL client."
}

stop_api_on_8080() {
  if [[ "$KEEP_API" == true ]]; then
    info "Keeping any process on port 8080 (--keep-api)"
    return
  fi
  local pid=""
  if command -v lsof >/dev/null 2>&1; then
    pid="$(lsof -ti :8080 2>/dev/null | head -1 || true)"
  elif command -v netstat >/dev/null 2>&1; then
    pid="$(netstat -ano 2>/dev/null | grep ':8080' | grep LISTENING | awk '{print $5}' | head -1 || true)"
  fi
  if [[ -n "${pid:-}" ]] && [[ "$pid" != "0" ]]; then
    info "Stopping process on port 8080 (PID $pid) so Gradle can seed..."
    taskkill //F //PID "$pid" >/dev/null 2>&1 || kill "$pid" 2>/dev/null || true
    sleep 2
  fi
}

wipe_database() {
  info "Wiping database \"$DB_NAME\" (DROP SCHEMA public CASCADE)..."
  run_psql <<'SQL'
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO public;
SQL
  info "Database schema cleared."
}

wait_for_seed_log() {
  local max_wait=300
  local i=0
  while (( i < max_wait )); do
    if grep -q "Account seed complete" "$LOG_FILE" 2>/dev/null; then
      return 0
    fi
    if grep -qi "FlywayMigrateException\|Script V4__\|Script V5__\|APPLICATION FAILED TO START\|BUILD FAILED" "$LOG_FILE" 2>/dev/null; then
      return 1
    fi
    sleep 2
    (( i += 2 )) || true
  done
  return 1
}

on_seed_failure() {
  echo "" >&2
  echo "Seed failed. Common causes:" >&2
  echo "  - Flyway migration error (check log above)" >&2
  echo "  - Port 8080 still in use (stop IntelliJ API, re-run with --yes)" >&2
  echo "" >&2
  echo "Log: $LOG_FILE" >&2
  tail -n 40 "$LOG_FILE" 2>/dev/null >&2 || true
}

run_app_seed() {
  info "Starting API once to apply Flyway, catalog SQL, and demo accounts (log: $LOG_FILE)..."
  local gradle_cmd=("$ROOT/gradlew" -p "$ROOT" bootRun --no-daemon)
  if [[ ! -x "$ROOT/gradlew" ]]; then
    die "gradlew not found or not executable in $ROOT"
  fi

  (
    export DB_HOST DB_PORT DB_NAME DB_USER DB_PASSWORD
    "${gradle_cmd[@]}" 2>&1 | tee -a "$LOG_FILE"
  ) &
  local gradle_pid=$!

  if wait_for_seed_log; then
    info "Demo seed finished (see log for details)."
  else
    kill "$gradle_pid" 2>/dev/null || true
    wait "$gradle_pid" 2>/dev/null || true
    on_seed_failure
    die "Seed did not complete."
  fi

  info "Stopping temporary Gradle bootRun (PID $gradle_pid)..."
  kill "$gradle_pid" 2>/dev/null || true
  sleep 3
  if command -v pkill >/dev/null 2>&1; then
    pkill -f "BeautySkinApiApplication" 2>/dev/null || true
  fi
}

print_summary() {
  cat <<EOF

----------------------------------------
Demo data reset complete
----------------------------------------
Database : $DB_NAME @ $DB_HOST:$DB_PORT

Logins (password shown once per role):
  Customer  buyer@beautyskin.com   / buyer123
  Staff     staff@beautyskin.com   / staff123
  Owner     owner@beautyskin.com   / owner123
  Admin     admin@beautyskin.com   / admin123

Start the API again (IntelliJ or: ./gradlew bootRun)
UI: http://localhost:4028

Seed log: $LOG_FILE
----------------------------------------
EOF
}

main() {
  check_psql

  if [[ "$AUTO_YES" != true ]]; then
    echo "This will DELETE ALL DATA in database \"$DB_NAME\" and reload demo seed."
    read -r -p "Continue? [y/N] " ans
    [[ "${ans,,}" == "y" || "${ans,,}" == "yes" ]] || die "Aborted."
  fi

  stop_api_on_8080
  wipe_database
  run_app_seed
  print_summary
}

main "$@"
