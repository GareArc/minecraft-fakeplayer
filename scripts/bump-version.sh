#!/usr/bin/env bash
# Bump the plugin version in gradle.properties.
# Usage: ./scripts/bump-version.sh [patch|minor|major]

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROPS="$ROOT/gradle.properties"
PART="${1:-patch}"

current=$(grep '^revision' "$PROPS" | cut -d= -f2)
IFS='.' read -r major minor patch <<< "$current"

case "$PART" in
  patch) patch=$((patch+1)) ;;
  minor) minor=$((minor+1)); patch=0 ;;
  major) major=$((major+1)); minor=0; patch=0 ;;
  *) echo "Usage: $0 [patch|minor|major]" >&2; exit 1 ;;
esac

next="$major.$minor.$patch"
sed -i "s/^revision=.*/revision=$next/" "$PROPS"
echo "Version bumped: $current → $next"
