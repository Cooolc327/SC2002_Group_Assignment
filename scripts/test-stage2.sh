#!/usr/bin/env bash
set -euo pipefail
stage2_project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$stage2_project_root"
stage2_classes="$(mktemp -d "${TMPDIR:-/tmp}/restaurant-stage2-checks.XXXXXX")"
trap 'rm -rf "$stage2_classes"' EXIT
stage2_logs="$stage2_project_root/build/stage2-checks"
mkdir -p "$stage2_logs"
stage2_sources=()
while IFS= read -r -d '' stage2_source; do
    stage2_sources+=("$stage2_source")
done < <(find src test -name '*.java' -print0)
javac --release 17 -Xlint:all -Werror -d "$stage2_classes" "${stage2_sources[@]}"
java -ea -cp "$stage2_classes" unit.BillCalculatorTest | tee "$stage2_logs/bill-calculator.log"
java -ea -cp "$stage2_classes" unit.OrderMenuTest | tee "$stage2_logs/order-menu.log"
java -ea -cp "$stage2_classes" integration.Stage2IntegrationTest \
    "$stage2_project_root/test/replays" "$stage2_logs" | tee "$stage2_logs/integration.log"
printf 'STAGE2_DEVELOPMENT_CHECKS=PASS logs=%s\n' "$stage2_logs"
