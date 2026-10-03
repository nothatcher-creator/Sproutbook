# SproutBook 3.7 organizer verification

Updated: 2026-10-03 UTC. Native implementation and affected emulator checks have
passed. Artifact freezing, new website verification/publication and GitHub release
publication are still pending; the latest delivered release remains 3.6.0.

## Build and unit tests

The fresh pre-change baseline passed at 18:57 UTC: Gradle `:core:test --rerun`
and `:app:testDebugUnitTest --rerun`, 109 core + 2 app = **111 tests**, zero
failures, errors or skips. The final integrated build passed in **5m 49s**:

- **126 unit tests**: 124 core + 2 app, zero failures, errors or skips.
- Debug APK and Android test APK compiled successfully.
- Unsigned R8 release APK and AAB compiled; vital lint passed. These outputs
  have not been installed or tested at runtime.
- Package `com.nothatcher.sproutbook`, version `3.7.0` / code `30700`,
  min SDK 26, compile/target SDK 37.
- JDK 17, Gradle wrapper 9.3.1, Android SDK 37.0 / Build Tools 36.0.0.
  The workspace helper uses a pinned HTTPS Google Maven mirror after upstream
  rate limiting; normal Android Studio setup is documented in `../README.md`.

The installable debug APK retains the existing evaluation certificate SHA-256:

```text
9c97e149698a89e51ef69e5a6af1649b1b55004f72f8cf57fd7993c5290ec0f3
```

No replacement key was generated. The private production signing identity is
unavailable; this evaluation identity does not establish production-update
compatibility. Verified artifacts are frozen from source checkpoint `354c805ad83bbf51790a7169c6bd25b52c35f25b`: APK 15,365,584 bytes; source ZIP 43,753,394 bytes. ZIP CRC, native project inclusion, signing-key/build-output exclusion and retained APK certificate passed. SHA-256 is recorded in the root `SHA256SUMS-3.7.0.txt` and evidence artifact-metadata.json. Later documentation changes do not regenerate this ZIP.

Evidence: [baseline log](evidence/home-organizer-2026-10-03/organizer-baseline.log),
[final build log](evidence/home-organizer-2026-10-03/organizer-final-build.log),
[JUnit totals](evidence/home-organizer-2026-10-03/unit-results.json).

## Native emulator checks

Executed on a dedicated Android 10 / API 29 software emulator, 360×800 at
160 dpi, with Wi-Fi and mobile data disabled. Synthetic fixtures replace app
records and must never run on a family installation.

**36 unique native tests passed: 35 behavioral checks and one screenshot fixture.**
The final organizer suite passed **20 tests in 307.225 seconds**:

- Seven repository checks: per-child writes, stale profile-save protection,
  empty shortcuts/validation/read-only behavior, private photo ownership and
  retention, backup photo inclusion/remapping, caregiver toggled during blocked
  image IO, and database close/reopen persistence.
- Five explicit migration origins: schemas 1, 9, 10, 11 and 12 to schema 13.
- Eight actual Compose UI flows: section order/visibility/shortcuts/background
  saving and real feature navigation; Preview/Cancel; Restore confirmation and
  Save; child switch and activity recreation; caregiver protections; empty
  Today and ordinary scrolling; stage changes without record/layout loss; and
  a simulated native photo-picker result with draft/Save/Cancel/private-copy
  persistence and child isolation.

The affected regression suite passed **16 tests in 189.796 seconds**: six
Wishlist repository checks, everyday persistence, native backup, five gesture
checks, Memory Tree framework accessibility, child-switch/Grandparent protection,
and the native visual fixture. Calendar long press retains the exact date;
ordinary tooth taps show details, long press edits; memory placement and scroll
protection remain intact. The one visual test is repeated for font variants and
is counted only once in the unique total.

The photo-picker test intercepts `Instrumentation.ActivityResult` and supplies
a real JPEG. It exercises the app callback, decoding, private copy, draft,
Save/Cancel, recreation and child scope. It **does not** exercise Android's
external picker interface. A test-only Java pipe provider blocks image IO so
caregiver mode can be enabled before completion; the import is rejected and
only the new image is removed.

Evidence: [final 20-test suite](evidence/home-organizer-2026-10-03/organizer-native-final.log)
and the `organizer-regression-normal.log` final regression record.

## Native rendering

The visual fixture passed separately at normal text size (**63.173 seconds**)
and 150% text size (**62.405 seconds**). Sixteen native screenshots were captured,
eight for each text size: organizer section overview/controls, shortcut
overview/controls, background choices/photo controls, and custom-photo Today in
light and dark themes. Section, shortcut and photo controls and both themes were
inspected for readable content and usable layout. The dark banner includes the
Today/date labels; no missing-label theme defect was found.

The capture fixture waits for Compose, native Android window and accessibility
idle. These are actual emulator screenshots, not rendered website mockups.
Gallery screenshots use synthetic family data. The final test-only frame
allowance rerun passed one test in **67.831 seconds** at actual font scale 1.0.
Its `snapshotSet` label was `large`; it did not replace the normal/150% evidence
screenshots and is not an additional large-text check.

Evidence: [screens](evidence/home-organizer-2026-10-03/screens/),
`organizer-visual-normal-final.log` and `organizer-visual-large-final.log`.

## Actual 3.6.0 → 3.7.0 install-as-update

The published 3.6.0 evaluation APK was installed on the dedicated emulator with
a validated schema-12 synthetic database and DataStore fixture. The new APK was
installed with `adb install -r`, retaining package, certificate and app data.
After the update:

- SQLite integrity was `ok`; schema became 13.
- Every pre-existing row, column value and table count matched the before image.
  This includes two children, a feed, pregnancy event, two preparation items,
  a memory with its saved branch position, and a Wishlist item.
- All five new home fields received their compatible defaults: original order,
  no hidden sections, Add memory/Schedule, Woodland, and no background photo.
- DataStore bytes were identical, SHA-256
  `6d01ff668f71a0b105ef69a5d968f2f35c9b24c50f2d096b73fad4c1340f6691`.
- The actual offline native screen rendered the previously selected Ash/Teen
  profile with its retained appearance and Grandparent read-only mode.

The cold `am start -W` commands timed out on this software emulator. The old
capture was still loading; it is not evidence of a fully rendered old profile.
The updated native screen subsequently rendered and was inspected. These checks
prove fixture/data preservation and eventual offline rendering, not hardware
startup latency or a frame-rate target.

Evidence: [preservation JSON](evidence/home-organizer-2026-10-03/update-preservation.json)
and [updated native screen](evidence/home-organizer-2026-10-03/update-after.png).

## Review and corrected test failures

Independent data/core review found no blocking record-loss, child-isolation or
backup issue. UI review found three issues, fixed before final verification:
preview retained underlying pointer targets; detached photo results lacked
feedback; and the Add-memory query could reopen on child switch. Preview now
zero-measures the retained editor, detached imports show choose-again feedback,
and the query is consumed once per navigation entry.

The initial seven-case UI run passed three and failed four saved-anchor
assertions because fixture `saveMemory` allocates anchors. The fixtures now call
the existing `arrangeMemory` operation to persist their requested branch.
All eight final UI flows passed; no production behavior change was needed for
those assertions. The original failing log is retained.

Evidence: [independent review](evidence/home-organizer-2026-10-03/review-findings.md)
and [initial UI attempt](evidence/home-organizer-2026-10-03/organizer-flow-native-initial.log).

## Remaining verification and delivery

- Actual external photo-picker UI, cancellation/rotation during active image IO,
  physical Android/iPhone devices and spoken TalkBack remain unverified.
- API 37 notification/service behavior and installed R8 runtime remain
  unverified. Emulator compilation is not coverage of those behaviors.
- No hardware FPS, battery, GPU, thermal, native-audio or reliable startup-latency
  measurements were obtained. Profiling after instrumentation found no process;
  it is not performance evidence.
- Unreferenced staged/previous background files remain private; only referenced
  photos are exported. Cleanup requires a future backup-safe policy.
- Local real Chromium 153 and WebKit 26.6 passed five viewports and two no-JavaScript cases each. Chromium 200% text passed two viewports; all 11 gallery images decoded and desktop wrapping/mobile horizontal navigation passed. Forward/reverse woodland scrolling, parallax, offscreen animation stopping, motion preferences, sample demos and the GitHub request draft passed. Static IDs/assets/file limits and APK/source SHA-256 checks passed, as did the separate sample-data DOM simulation. Live publication checks are next.
- Freeze the final source checkpoint and verified debug APK; validate ZIP CRC,
  native source inclusion, key exclusion, certificate and SHA-256 manifest.
  Publish through the existing Sites deployment and GitHub prerelease workflow,
  then download and verify the published assets and update this delivery ledger.

Source checkpoint, artifact sizes/checksums, new Site version/deployment and
GitHub release run are pending. Do not describe the staged 3.7 links as published
until those actions and their final checks complete.
