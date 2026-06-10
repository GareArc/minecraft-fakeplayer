#!/usr/bin/env bash
# First-time dev environment setup for fakeplayer.
# Usage:
#   ./scripts/setup.sh          # full setup
#   ./scripts/setup.sh --check  # verify only, no changes

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CHECK_ONLY=false

[[ "${1:-}" == "--check" ]] && CHECK_ONLY=true

# ── Colours ───────────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
ok()   { echo -e "${GREEN}  ✔${NC}  $*"; }
warn() { echo -e "${YELLOW}  ⚠${NC}  $*"; }
fail() { echo -e "${RED}  ✘${NC}  $*"; ERRORS=$((ERRORS+1)); }
info() { echo -e "${CYAN}  →${NC}  $*"; }

ERRORS=0

echo ""
echo "  fakeplayer dev environment setup"
echo "  ─────────────────────────────────────────"
echo ""

# ── 1. Java 21 ────────────────────────────────────────────────────────────────
echo "  Checking prerequisites…"
if command -v java &>/dev/null; then
  JAVA_VER=$(java -fullversion 2>&1 | grep -oP '(?<=version ")[^"]+' | head -1)
  JAVA_MAJOR=$(echo "$JAVA_VER" | cut -d. -f1)
  if [[ "$JAVA_MAJOR" -ge 21 ]]; then
    ok "Java $JAVA_VER (>= 21 required)"
  else
    fail "Java $JAVA_VER found — Java 21+ is required. Install from https://adoptium.net/"
  fi
else
  fail "Java not found. Install Java 21+ from https://adoptium.net/"
fi

# ── 2. Gradle wrapper ─────────────────────────────────────────────────────────
if [[ -x "$ROOT/gradlew" ]]; then
  ok "Gradle wrapper (gradlew) is present and executable"
else
  warn "gradlew not executable — fixing…"
  if ! $CHECK_ONLY; then
    chmod +x "$ROOT/gradlew"
    ok "Fixed gradlew permissions"
  fi
fi

# ── 3. lib/ JARs ──────────────────────────────────────────────────────────────
LIB_DIR="$ROOT/lib"
OPENINV="$LIB_DIR/OpenInv.jar"
PAPI="$LIB_DIR/PlaceholderAPI-2.11.6.jar"

if [[ -f "$OPENINV" ]]; then
  ok "lib/OpenInv.jar present"
else
  warn "lib/OpenInv.jar missing — optional integration will not compile against it"
  warn "  Get it from: https://www.spigotmc.org/resources/open-inv.54402/"
fi

if [[ -f "$PAPI" ]]; then
  ok "lib/PlaceholderAPI-2.11.6.jar present"
else
  warn "lib/PlaceholderAPI-2.11.6.jar missing — optional integration will not compile against it"
  warn "  Get it from: https://www.spigotmc.org/resources/placeholderapi.6245/"
fi

# ── 4. Git hooks ──────────────────────────────────────────────────────────────
if $CHECK_ONLY; then
  HOOK="$ROOT/.git/hooks/pre-commit"
  if [[ -x "$HOOK" ]]; then
    ok "pre-commit hook installed"
  else
    warn "pre-commit hook not installed (run setup.sh without --check to install)"
  fi
else
  echo ""
  echo "  Installing git hooks…"
  HOOKS_SRC="$ROOT/scripts/hooks"
  HOOKS_DST="$ROOT/.git/hooks"
  mkdir -p "$HOOKS_DST"
  for src in "$HOOKS_SRC"/*; do
    name="$(basename "$src")"
    dst="$HOOKS_DST/$name"
    cp "$src" "$dst"
    chmod +x "$dst"
    ok "Installed hook: $name"
  done
fi

# ── 5. lib/ directory ─────────────────────────────────────────────────────────
if ! $CHECK_ONLY; then
  mkdir -p "$LIB_DIR"
  if [[ ! -f "$LIB_DIR/.gitkeep" ]]; then
    touch "$LIB_DIR/.gitkeep"
  fi
fi

# ── Summary ───────────────────────────────────────────────────────────────────
echo ""
if [[ $ERRORS -eq 0 ]]; then
  ok "Environment looks good!"
  echo ""
  if ! $CHECK_ONLY; then
    info "Run  make build  to compile the plugin"
    info "Run  make help   to see all available targets"
  fi
else
  echo -e "  ${RED}$ERRORS issue(s) found. Fix them before building.${NC}"
  exit 1
fi
echo ""
