#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p build/reports/verification
report_dir=build/reports/verification

printf '%s\n' 'Checking the failing baseline (protection disabled)...'
if ./gradlew runGameTestServer -PprotectionEnabled=false --console=plain > "$report_dir/baseline.log" 2>&1; then
    printf '%s\n' 'ERROR: baseline unexpectedly passed.' >&2
    exit 1
fi
if ! grep -q 'aoestillremovesadjacentordinaryblocks failed! AoE removed protected Waystone' "$report_dir/baseline.log"; then
    printf '%s\n' "ERROR: baseline did not reproduce the AoE bug; inspect $report_dir/baseline.log" >&2
    exit 1
fi

printf '%s\n' 'Building the mod and running protection/compatibility tests...'
if ! ./gradlew build runGameTestServer --console=plain > "$report_dir/protected.log" 2>&1; then
    tail -60 "$report_dir/protected.log"
    exit 1
fi
printf '%s\n' 'Checking startup and mining without Waystones or Balm...'
if ! ./gradlew runGameTestServer -PwithoutWaystones --console=plain > "$report_dir/optional.log" 2>&1; then
    tail -60 "$report_dir/optional.log"
    exit 1
fi
printf '%s\n' 'Checking the Forge and Balm versions from ATM9 Expert 0.7.1...'
if ! ./gradlew runGameTestServer -PforgeVersion=47.4.10 -PbalmVersion=7.3.44 --console=plain > "$report_dir/pack-versions.log" 2>&1; then
    tail -60 "$report_dir/pack-versions.log"
    exit 1
fi
printf '%s\n' "Verification passed. Logs: $report_dir"
