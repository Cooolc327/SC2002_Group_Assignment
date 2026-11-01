#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
member_a_build_dir="$(mktemp -d "${TMPDIR:-/tmp}/restaurant-rush-a.XXXXXX")"
trap 'rm -rf "$member_a_build_dir"' EXIT
sources=()
while IFS= read -r -d '' source_file; do
    sources+=("$source_file")
done < <(find src test -name '*.java' -print0)
javac --release 17 -Xlint:all -Werror -d "$member_a_build_dir" "${sources[@]}"
java -ea -cp "$member_a_build_dir" integration.MemberACoreTest
