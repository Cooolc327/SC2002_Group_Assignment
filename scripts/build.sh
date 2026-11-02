#!/usr/bin/env bash
set -euo pipefail
stage1_project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$stage1_project_root"
stage1_classes="$stage1_project_root/build/classes"
mkdir -p "$stage1_classes"
find "$stage1_classes" -type f -name '*.class' -delete
stage1_sources=()
while IFS= read -r -d '' stage1_source; do
    stage1_sources+=("$stage1_source")
done < <(find src -name '*.java' -print0)
javac --release 17 -Xlint:all -Werror -d "$stage1_classes" "${stage1_sources[@]}"
printf 'BUILD_STAGE1=PASS classes=%s\n' "$stage1_classes"
