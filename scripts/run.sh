#!/usr/bin/env bash
set -euo pipefail
stage1_project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
bash "$stage1_project_root/scripts/build.sh"
exec java -cp "$stage1_project_root/build/classes" Main "$@"
