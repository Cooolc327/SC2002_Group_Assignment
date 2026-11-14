#!/usr/bin/env bash
set -euo pipefail
stage3_project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$stage3_project_root"
stage3_classes="$(mktemp -d "${TMPDIR:-/tmp}/restaurant-stage3-checks.XXXXXX")"
trap 'rm -rf "$stage3_classes"' EXIT
stage3_logs="$stage3_project_root/build/stage3-checks"
mkdir -p "$stage3_logs"
stage3_sources=()
while IFS= read -r -d '' stage3_source; do
    stage3_sources+=("$stage3_source")
done < <(find src test -name '*.java' -print0)
javac --release 17 -Xlint:all -Werror -d "$stage3_classes" "${stage3_sources[@]}"
java -ea -cp "$stage3_classes" unit.ComboMealTest | tee "$stage3_logs/combo-meal.log"
java -ea -cp "$stage3_classes" unit.BillCalculatorTest | tee "$stage3_logs/bill-calculator.log"
java -ea -cp "$stage3_classes" integration.Stage3IntegrationTest \
    "$stage3_project_root/test/replays" "$stage3_logs" "$@" | tee "$stage3_logs/integration.log"
printf 'STAGE3_DEVELOPMENT_CHECKS=PASS logs=%s\n' "$stage3_logs"
