#!/usr/bin/env bash
set -euo pipefail
# Dedicated QA emulator only: this script clears the installed app's data.
# Run in the shell that owns the emulator's adb server.
if [ -n "${SPROUT_BUILD_ROOT:-}" ]; then
 APK_ROOT="$SPROUT_BUILD_ROOT/app/outputs/apk"
else
 APK_ROOT="$(pwd)/app/build/outputs/apk"
fi
ARTIFACTS="${QA_OUTPUT:-$(pwd)/qa}"
mkdir -p "$ARTIFACTS"
run_tests() {
 local label="$1" duration="$2" classes="$3"
 set +e
 timeout "$duration" adb shell am instrument -w -r -e class "$classes" com.nothatcher.sproutbook.test/androidx.test.runner.AndroidJUnitRunner >"$ARTIFACTS/$label.log" 2>&1
 local code=$?
 set -e
 printf '%s: command exit %s\n' "$label" "$code"
 # Preserve evidence even if a suite fails; instrumentation's own summary is authoritative.
}
timeout 180 adb install --no-incremental -r "$APK_ROOT/debug/app-debug.apk"
timeout 120 adb install --no-incremental -r "$APK_ROOT/androidTest/debug/app-debug-androidTest.apk"
timeout 20 adb shell pm clear com.nothatcher.sproutbook
timeout 20 adb logcat -c
run_tests navigation-tests 240 com.nothatcher.sproutbook.NavigationTest
run_tests data-tests 600 com.nothatcher.sproutbook.NewCareRepositoryTest,com.nothatcher.sproutbook.FoundationTest,com.nothatcher.sproutbook.MigrationTest,com.nothatcher.sproutbook.RepositoryTest,com.nothatcher.sproutbook.BackupTest,com.nothatcher.sproutbook.HealthLinkRepositoryTest,com.nothatcher.sproutbook.ReviewRegressionTest,com.nothatcher.sproutbook.CrudAndHistoryTest,com.nothatcher.sproutbook.SoundTest,com.nothatcher.sproutbook.NotificationTest
run_tests everyday-data 600 com.nothatcher.sproutbook.EverydayRepositoryTest,com.nothatcher.sproutbook.LinkedPumpIntegrityTest,com.nothatcher.sproutbook.EverydayPersistenceTest
run_tests ui-tests 2400 com.nothatcher.sproutbook.NewHomeFlowTest,com.nothatcher.sproutbook.NewCareFlowTest,com.nothatcher.sproutbook.FeatureFlowTest,com.nothatcher.sproutbook.HealthLinkFlowTest,com.nothatcher.sproutbook.GestureTest,com.nothatcher.sproutbook.NavigationSweepTest,com.nothatcher.sproutbook.CareFlowTest,com.nothatcher.sproutbook.NativeControlsTest,com.nothatcher.sproutbook.CareUpgradeTest,com.nothatcher.sproutbook.TreeAccessibilityTest
run_tests everyday-ui 1800 com.nothatcher.sproutbook.EverydayFlowTest,com.nothatcher.sproutbook.EverydayHomeFlowTest,com.nothatcher.sproutbook.EverydaySecondaryFlowTest,com.nothatcher.sproutbook.RepeatedHourFlowTest
timeout 30 adb logcat -b crash -d >"$ARTIFACTS/crash.log"

run_tests density-tests 360 com.nothatcher.sproutbook.DensityTest
python3 "$(dirname "$0")/summarize-device-tests.py" --check "$ARTIFACTS/navigation-tests.log" "$ARTIFACTS/data-tests.log" "$ARTIFACTS/everyday-data.log" "$ARTIFACTS/ui-tests.log" "$ARTIFACTS/everyday-ui.log" "$ARTIFACTS/density-tests.log" >"$ARTIFACTS/device-summary.json"
