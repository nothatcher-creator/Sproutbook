# Home organizer evidence · 2026-10-03 UTC

All family records and pictures in these captures are synthetic emulator fixtures.
126 unit tests passed. The final 20-test native run and retained 16-test regression
run give 36 unique native checks (35 behavior checks and one visual test).
Normal and 150% captures each cover eight native views; visual reruns are not
additional unique tests. The settled capture-harness rerun passed at font scale
1.0 (its snapshotSet filename label was arbitrary); the stored large screenshots
are the separately verified 1.5 captures, not that later rerun.

The initial UI failures are preserved: fixtures incorrectly assumed saveMemory
saved the requested branch anchor. The fixtures now use arrangeMemory. Production
code was not changed for those assertions.

Actual evaluation-signed 3.6.0→3.7.0 install over existing synthetic private data
passed. update-before.json and update-after.json record every old row. The check
script compared all old columns, row counts and DataStore bytes and checked new
schema-13 defaults. Wi-Fi/data were disabled. update-after.png shows the selected
Ash/Teen profile, retained Sky appearance and Grandparent read-only mode. The
before screenshot is a loading frame; it does not establish old rendered UI.
The software-emulator am-start wait timed out; this is not hardware latency or
60fps evidence. No physical-device, external-picker UI or spoken TalkBack claim.

Build/log paths and exact test commands are in ../../QA-3.7.md. Tests recreate
synthetic data: never run fixture instrumentation against a family's installation.
