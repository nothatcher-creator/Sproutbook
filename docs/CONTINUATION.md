# SproutBook continuation checkpoint

Updated: 2026-10-02 UTC. This document describes this session's work; older QA
ledgers are historical evidence, not fresh validation.

## Recovery

- Repository: https://github.com/nothatcher-creator/Sproutbook, clean `main` at
  `a56dd6c` before this session. No existing uncommitted work was present.
- Latest published release is the evaluation pre-release `v3.5.0`. GitHub's
  `/releases/latest` returns 404 because all releases are pre-releases; the release
  list and explicit tag endpoint were checked.
- Native Kotlin project recovered into `android/` from
  `SproutBook-3.5.0-source.zip`. The showcase remains separately in `website/`.
- Source SHA-256: `fca20d9542c2019aad613541faea662ea4ca308ef8cd0346d69d1c114a38d9d2`.
- Published APK SHA-256:
  `104cb6e01565f601314326931f08e8a4096a1f8f6b54a0df896fa36a27c47713`.
- Both matched the downloaded `SHA256SUMS-3.5.0.txt` and GitHub asset digests.
  Recovery originals and API metadata are in
  `/workspace/scratch/sproutbook-recovery/v3.5.0/`.
- The source archive contains a public evaluation signing key. It was preserved
  at `/workspace/shared/sproutbook-signing/development-signing.keystore`, outside
  Git. Removed the archive's Git-ignore exception for that file. No replacement
  signing identity has been generated. Published and rebuilt APK certificates and
  the preserved key all match SHA-256
  `9c97e149698a89e51ef69e5a6af1649b1b55004f72f8cf57fd7993c5290ec0f3`.

## Architecture and constraints

Read `android/README.md`, `android/docs/Build.md`, `android/docs/Plan.md`,
`android/docs/Progress.md`, `android/docs/Review.md`, `android/docs/QA.md`, and
especially `android/docs/QA-3.5.md` before further work. `:core` contains pure
Kotlin rules, geometry, backup validation, content, and tests; `:app` contains
Compose feature packages, ViewModels, Room/DataStore repositories, native audio,
WorkManager reminders, and SAF backup/import. Package is
`com.nothatcher.sproutbook`; Room schema 11, backup format 3, min SDK 26, target 37.
Keep Today / Schedule / Care / More, offline records, explicit migrations,
child ownership, caregiver guards, original graphics, and motion gating.

Never run instrumentation fixtures on a family installation: they clear data.
Use a dedicated emulator. Do not commit signing material or credentials.

## Current verification

- Release download checksums: PASS (source and APK), 2026-10-02.
- Native baseline: `:core:test :app:testDebugUnitTest :app:assembleDebug
  :app:assembleDebugAndroidTest` PASS, Gradle exit 0, 82 executed tasks, 8m 9s.
  Core 78 tests and app 1 test, zero failures/errors/skips. Fresh XML and logs are
  under `android/docs/evidence/continuation-2026-10-02/`.
- Toolchain: JDK 17.0.20 Corretto, Gradle 9.3.1 with pinned wrapper checksum,
  SDK 37.0 / build tools 36.0.0. SDK archive hashes passed the supplied bootstrap.
  Toolchain lives in `/workspace/scratch/sprout-toolchain/`; `/tmp/sprout-toolchain`
  links there after the 4.9 GiB temporary mount filled during system-image extraction.
  Build helper: `bash /workspace/scratch/sproutbook-gradle.sh <tasks>`.
  Maven Central's standard endpoints returned HTTP 429. A local Gradle init script
  uses Google's HTTPS Maven Central mirror with the original pinned coordinates.
  Product dependency versions and repositories were not changed.
- BrowserAct has no configured browser/API key. Local Playwright 1.63.0 / Chromium
  153.0.8010.12 provided actual rendered checks. The live baseline reproduced all
  ten upper teeth blocked by the overlapping lower-arch container. A CSS fix passed
  real browser checks on local hosted-source preview at 1440x900, 390x844, 360x640,
  and 667x375: forward/reverse SVG frames, native wheel scrolling, no idle autoplay,
  all chapters, persisted motion-off, live reduced motion, sample demos, and GitHub
  draft feedback. Screenshots/results: `artifacts/browser-baseline/` and
  `artifacts/browser-fixed/`. Browser checks do not establish hardware frame rate.
- Dedicated API 29 software emulator is running at `emulator-5554`, 360x800/dpi160,
  without KVM or audio output. Boot System UI ANRs were dismissed using current
  UI hierarchies before app tests. After a stopped process, stale locks were removed
  only from this test AVD. No physical-device test has happened.
- RED native tests reproduced both health bugs on the original 3.5.0 app:
  same-title/date appointments selected the other visit, and a notes-only edit
  shifted the later New York repeated hour back one hour. Both tests failed as
  expected; `health-red-device.log` and `health-red-summary.json` preserve evidence.
- Implemented a child-constrained, searchable native appointment picker with full
  date/time/place, explicit empty/loading states, lazy pages of 50, and exact IDs.
  Cancel changes no draft link; clear removes only the link. Reused
  `RecordTime.edited` to preserve the original recorded instant. Room schema 11 and
  backup format 3 remain unchanged. Candidate version is 3.5.1 / 30501.
- Read-only independent review found no Critical/Important issue. Corrected its
  minor finding: candidate state resets for a new search, and stale linked-visit
  emissions cannot render the wrong summary. Pagination keeps existing rows while
  the next page loads. Review ran no tests and accessed no signing material.
- Final candidate debug/test build passed after the review correction (82 tasks,
  17 executed / 65 up-to-date, 2m 5s). App unit test reran and passed; unchanged core
  tests were up-to-date after the 78-test baseline. Initial 3.5.1 R8 release APK, AAB,
  and vital lint build passed (59 tasks, 5m 38s); outputs are unsigned. Final R8
  APK/AAB, vital lint and corrected test APK rebuild passed after review: 114 tasks,
  19 executed / 95 up-to-date, 3m 32s.
- An initial pre-update fixture run was interrupted by an install and reported
  `Process crashed`; it is NOT counted as a pass. Its snapshot failed the intended
  two-child fixture assertion. The clean, sequential retry against 3.5.0 passed
  (1 test, 76.737s). See
  `v350-pre-update-child-guard.log`; do not use it as passing evidence.
- Install-as-update PASS: rebuilt original-source 3.5.0 → 3.5.1 with two children, teen stage, selected
  child, a feeding record and caregiver preference retained. Wi-Fi/data disabled;
  actual native UI showed Ash, teen dashboard and read-only mode. SQLite integrity
  checks passed; every family table's rows and DataStore bytes matched exactly
  before/after. `update-preservation.json` and `update-offline-ui.xml` preserve
  evidence. Test snapshots in ignored `artifacts/update-persistence/` are synthetic.
- Missing-key check PASS: overriding the evaluation key with a nonexistent path
  caused `validateSigningDebug` to fail; no replacement file appeared. Expected
  failure is in `signing-missing-key.log`. Final APK uses the preserved certificate.
- All 6 new native health tests PASS across completed runs. Initial run had 5 passes
  and one test timing failure (unloaded lazy row); added a loading wait/list scroll,
  and the corrected duplicate-selection rerun passed. `health-final-summary.json`
  records final outcomes; initial failure logs remain. Native UI also verified
  pagination, hardware Back, no-match Cancel, clear-without-delete, caregiver closing
  the picker, and discard on child switch. Repository tests checked literal `%`/`_`
  search, two-child ownership, reopen, stage changes, exact link/time and actual
  BackupManager export/review/restore. Final APK certificate/manifest and the complete
  original motion asset contract passed separate checks.
- Existing affected regressions PASS: 14 completed tests, 210.517s, zero failures,
  errors or skips. Foundation/reopen (1), migrations (3), backup/photos/rollback (1),
  gestures (5), health CRUD (1), child/caregiver (1), native sound controls (1), and
  four-tab navigation (1). `native-regression-summary.json` lists exact methods.
- Large text PASS: two completed tests at font scale 1.5, 80.197s, no skips. Light
  theme feeding saved 80 mL; the dark-theme native health picker selected the exact
  repeated-title visit. Normal/large picker and light feeding screenshots were
  captured from native Android and visually inspected. The final ledger has
  21 unique passing methods (`final-device-summary.json`).
- Bounded performance capture: one stable 8-second health-picker idle sample at
  150% text, Reduce motion enabled / Android animation scales 0, 0 app frames after
  gfxinfo reset. PSS 95,834 → 94,928 KiB and 1 Activity in both snapshots. Raw
  frames/memory/UI evidence is in `performance/`. This is not smoothness, battery or
  leak evidence. First post-instrumentation UI query returned no XML; discarded,
  then retried with fresh valid trees. Final crash buffer empty. Font scale restored
  to 1.0 after QA.
- Existing public Sites project recovered:
  `appgprj_6abeb834e1a88191a8c4907b567c3472`, saved version 2. Hosted source checkout
  is `/workspace/sproutbook-showcase`, starting commit
  `1c9796e93dbcebea98ada4052f31e1137dd7d22e`; `.openai/hosting.json` specifies `dist`.
  The hosting skill's bundled workflow script was unavailable via skills or disk;
  equivalent Git operations use hidden stdin credentials and never write tokens to
  files or shell arguments. Website fix is verified locally and not yet published.

## Next steps

All four rendered-browser cases passed again against final local 3.5.1 website
metadata, native screenshot and feature-request context. Evidence:
`docs/evidence/browser-2026-10-02/v351-browser.log` and `results.json`. Packaging
and existing public Sites deployment are now the remaining delivery steps.

Verified local artifacts in `artifacts/3.5.1/`: APK 15,097,626 bytes, source ZIP
33,454,252 bytes. Source manifest checkpoint is `42d2ef64ac98a90a65df32d87c0dd8553e370a34`.
ZIP CRC and every manifest file hash passed; no signing/credential files are
packaged. Source halves reassemble byte-for-byte and each meets the hosting file
limit. APK SHA-256:
`bdbef96bdf73dd92d353d043b3e616e49e199915aedf8d003f547c95530c8bca`;
source SHA-256:
`5404e40d93fd2fb70238b06bbe0f7f1813519488f856ff7ef16418d033c2bbda`.
New release static/version/checksum checks and DOM-simulated regressions passed
again (`static-v351.log`, `dom-v351.log`). Existing hosted 3.5.0 downloads remain.
Publication is pending; do not regenerate these frozen artifacts while updating
publication documentation.

1. Package the verified 3.5.1 APK/source/checksums, update download metadata and
   screenshots consistently, publish an evaluation pre-release and the existing
   Sites project, then checkpoint terminal release/deployment details here.

Hardware audio quality, battery use, spoken TalkBack, Android 17 background behavior,
and production signing were already open gates at 3.5.0. Do not claim them passed.
