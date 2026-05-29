#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DIST="$ROOT/dist"

mkdir -p "$DIST"

"$ROOT/packwiz-update.sh"

cd "$ROOT"
zip -r "$DIST/TCToRPG-Client.zip" .

echo "Release package created: $DIST/TCToRPG-Client.zip"
