# SproutBook 3.5.1 verification

Fresh session evidence: 2026-10-02 UTC. Historical QA files describe earlier
releases and are not counted in this update. Logs and runner summaries are in
`evidence/continuation-2026-10-02/`; the repository checkpoint is
`../../docs/CONTINUATION.md`.

## Scope

Health notes previously resolved appointment display labels back to the first
matching visit. Two check-ups on the same day could link the wrong appointment.
The editor also reconstructed a notes-only edit's timestamp, shifting the later
daylight-saving repeated hour back one hour. Both failures were reproduced on
the recovered original 3.5.0 app before behavior changed.

The native picker now selects exact IDs, shows full date/time/place, searches
title or place before applying its limit, and loads pages of 50. It queries only
the current child's visits. Empty/loading/unavailable states give feedback;
Cancel preserves the draft and clearing removes only its link. RecordTime.edited
preserves the original instant when its displayed date/time did not change.
Caregiver mode closes the mutable picker; child switching discards the draft.

No medical guidance, permissions, Room schema (11), backup format (3), database
migration, native audio implementation, or motion asset changed.

## Build and signing

- Baseline: JDK 17.0.20 Corretto / Gradle 9.3.1 / Android SDK 37.0 / build tools
  36.0.0. Wrapper and downloaded SDK archive checksums verified.
- Baseline tasks `:core:test :app:testDebugUnitTest :app:assembleDebug
  :app:assembleDebugAndroidTest` passed: 82 tasks, 8m 9s. Core 78 and app 1 unit
  tests passed, zero failures/errors/skips. Fresh XML is archived with evidence.
- Final debug/test build after review passed: 82 tasks, 17 executed and 65
  up-to-date, 2m 5s. App's 1 unit test reran successfully; unchanged core tests were
  up-to-date after their successful baseline run.
- Initial candidate R8 APK, release AAB and vital lint passed: 59 tasks, 5m 38s.
  Final R8 APK/AAB, vital lint and corrected test APK rebuild passed after review:
  114 tasks, 19 executed / 95 up-to-date, 3m 32s. Release outputs are unsigned.
- Version 3.5.1 / 30501; package `com.nothatcher.sproutbook`; min SDK 26, target 37.
- Preserved evaluation certificate SHA-256:
  `9c97e149698a89e51ef69e5a6af1649b1b55004f72f8cf57fd7993c5290ec0f3`.
  No new key generated. No key or credential is committed or packaged.
- Missing-key test passed: `validateSigningDebug` failed for an absent explicitly
  supplied key and did not create that file. The expected failure log is retained.
- Standard Maven Central endpoints returned HTTP 429 on this host; a local init
  script used Google's HTTPS Maven Central mirror for the original pinned
  coordinates. Product dependencies/repositories are unchanged.

## Native Android checks

Dedicated API 29 Android 10 AOSP x86_64 software emulator, 360x800 / dpi160,
1536 MiB guest RAM, two cores, no KVM, no audio output. Instrumentation fixtures
replace sample family data and must never run on a family installation.

| Check | Result / exact evidence |
| --- | --- |
| Original health bugs | Two expected failures, `health-red-device.log`; wrong duplicate visit and timestamp shifted by 3,600,000 ms. |
| 3.5.0 update fixture | One completed test passed, 76.737s, `v350-update-fixture.log`. |
| Install as update, offline startup | Passed rebuilt original-source 3.5.0 → 3.5.1 with two children, teen stage, selected Ash, one feeding record and caregiver preference. Published/rebuilt/candidate APK certificates match. Every family table row and DataStore byte matched; both SQLite integrity checks passed. `update-preservation.json`, `update-offline-ui.xml`. |
| New Room/repository and health UI tests | All 6 unique tests pass across completed runs. Initial run: 5 passed, 1 failed, 164.655s; duplicate-selection test attempted an unloaded lazy row. Added loading wait/lazy-list scroll; corrected rerun passed (1 test, 52.784s). `health-final-summary.json` records the final result and both logs retain the earlier failure. |
| Existing affected regressions | All 14 passed, 210.517s, `native-regression-summary.json`: Foundation (1), migrations (3), backup/photos/invalid rollback (1), gestures (5), health CRUD (1), child/caregiver (1), native sound pause/resume/switch/stop (1), four-tab navigation (1). |
| 150% text size | Two completed checks passed, 80.197s, no skips: light-theme feeding saved 80 mL and the new picker selected the correct duplicate visit. `large-text-summary.json`; screenshots in `../../docs/evidence/native-2026-10-02/`. |
| Final instrumentation ledger | 21 unique passing methods across completed runs; duplicate selection additionally ran at 150% text size. `final-device-summary.json`. |
| Bounded performance evidence | One stable 8-second native picker idle sample at 150% text: 0 app frames after gfxinfo reset. PSS 95,834 → 94,928 KiB; 1 Activity in both snapshots. Reduce motion enabled, all Android animation scales 0. Raw gfxinfo/meminfo and current UI tree/screenshot are in `performance/`. No smoothness, battery or leak inference. The first UI dump had no XML root; discarded and retried with fresh, valid app trees. |

A first pre-update fixture was interrupted by an install and reported
`Process crashed`; it is not a passing check. Its incomplete snapshot did not
meet the intended two-child fixture assertion. The sequential retry and exact
before/after comparison above passed. Boot System UI ANRs were dismissed from
current UI trees before app tests; results behind a system overlay are rejected.
The final Android crash buffer was empty (`final-crash.log`).

## Review and browser verification

Read-only independent review found no Critical/Important issues. Its minor
finding was corrected before the final debug build: new searches reset candidate
state, and an old visit emission cannot display another selected ID's summary.
Pagination keeps valid existing rows while more results load. Review itself ran
no tests and accessed no signing material.

Real Playwright 1.63.0 / headless Chromium 153.0.8010.12 verification of the
showcase found that the full lower-arch container intercepted every upper tooth
button. CSS now lets only the buttons intercept pointers. Four viewport runs
(1440x900, 390x844, 360x640, 667x375) passed scroll-controlled forward/reverse
SVG frames, normal scrolling, still layouts, persisted motion off, live reduced
motion, sample logging/reset, all 20 teeth, hold/movement cancellation, relocking,
stage selection and feature-request draft feedback. No issue was submitted.
See `../../website/QA.md` for exact browser/static/DOM scope and screenshots.
The existing public Sites deployment succeeded (saved version 3), and all four
viewport cases passed again on the actual live URL. The GitHub 3.5.1 evaluation
release workflow succeeded; all published asset sizes/digests match verified files.
Exact IDs and links are recorded in the continuation checkpoint.

## Remaining limits

No physical-device or Galaxy S25 check. No spoken TalkBack, speaker/audio quality,
hardware frame-rate, battery measurement, Android 17 background/notification,
or minified-release runtime claim. The focused update does not repeat the entire
historical instrumentation suite. The previously documented finite press
animation replay remains cosmetic; it never repeats the underlying action.

This is an evaluation pre-release. Production distribution requires the original
private production signing identity; unsigned release outputs are build evidence.
