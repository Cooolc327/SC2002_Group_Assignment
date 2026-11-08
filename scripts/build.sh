#!/usr/bin/env bash
set -euo pipefail
restaurant_project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$restaurant_project_root"
restaurant_classes="$restaurant_project_root/build/classes"
mkdir -p "$restaurant_classes"
find "$restaurant_classes" -type f -name '*.class' -delete
restaurant_sources=()
while IFS= read -r -d '' restaurant_source; do
    restaurant_sources+=("$restaurant_source")
done < <(find src -name '*.java' -print0)
javac --release 17 -Xlint:all -Werror -d "$restaurant_classes" "${restaurant_sources[@]}"
printf 'BUILD_STAGE2=PASS classes=%s\n' "$restaurant_classes"
