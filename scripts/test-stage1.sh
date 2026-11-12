#!/usr/bin/env bash
set -euo pipefail
stage1_project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$stage1_project_root"
stage1_test_classes="$(mktemp -d "${TMPDIR:-/tmp}/restaurant-stage1-checks.XXXXXX")"
trap 'rm -rf "$stage1_test_classes"' EXIT
stage1_logs="$stage1_project_root/build/stage1-checks"
mkdir -p "$stage1_logs"
stage1_sources=()
while IFS= read -r -d '' stage1_source; do
    stage1_sources+=("$stage1_source")
done < <(find src test -name '*.java' -print0)
javac --release 17 -Xlint:all -Werror -d "$stage1_test_classes" "${stage1_sources[@]}"
java -ea -cp "$stage1_test_classes" integration.MemberACoreTest --production-orders \
    | tee "$stage1_logs/core-production-order.log"
java -ea -cp "$stage1_test_classes" integration.Stage1SmokeTest \
    "$stage1_project_root/test/replays" "$stage1_logs" \
    | tee "$stage1_logs/smoke-summary.log"
printf 'STAGE1_DEVELOPMENT_CHECKS=PASS logs=%s\n' "$stage1_logs"
