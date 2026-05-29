#!/usr/bin/env bash
set -euo pipefail

if ! command -v packwiz >/dev/null 2>&1; then
  echo "packwiz not found in PATH."
  echo "Install packwiz and try again."
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [ ! -f index.toml ]; then
  echo "index.toml not found."
  exit 1
fi

packwiz refresh
packwiz validate

echo "packwiz update complete."
