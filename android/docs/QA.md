# QA ledger · SproutBook 3.3.0

Verified 30 September 2026. Native Kotlin/Compose, Room schema 10, backup format 2, min SDK 26 and compile/target SDK 37.

## 3.1.0 executed checks

- 59 core tests passed, including v1 backup normalization, required v2 arrays, invalid links/amounts and measurement conversions.
- 44 unique Android tests passed across completed batches. The initial UI batch exposed three test timing/navigation problems; corrected tests and the final 15-test navigation batch passed. This was not one uninterrupted 44-test invocation.
- Both original schema 1 and populated 3.0.0 schema 9 migrated to 10. Existing child notes and a precise legacy measurement survived.
- New repository checks exercised child isolation, once-only bottle feeding, actual consumed amount, idempotent restocking, failed restock rollback, foreign-link deletion, backup v2 restoration, legacy v1 restoration and read-only guards.
- New UI checks exercised diaper and growth add/edit/delete, preparation/use with partial feeding, shopping quantities/unlink/purchase, dirty-draft guards, unknown-unit and precision preservation, caregiver mode and Today links.
- Existing notification, native AudioTrack, calendar long press, teeth long press, tree snapping/dragging, scroll safety, native controls, photo backup and other feature checks passed.
- Dense-family check passed with 4 children, 500 memories, 500 feeds and 300 appointments. It verifies interaction and data handling on the software emulator; it does not establish Galaxy S25 frame rates.
- Debug, instrumentation, R8 unsigned release APK and release AAB compiled; release lint-vital passed. The final debug certificate matches 3.0.0, enabling an evaluation update without uninstalling.
- Final instrumentation crash buffer was empty. A software-emulator System UI ANR dialog was observed during an earlier run and dismissed before rerunning affected flows; it was not an application crash.

Optimized release smoke passed: install-as-update retained the family; Wi-Fi/mobile data disabled; native SAF exported format2 with all17 tables and5 records, validated before replacement, restored successfully, and preserved precise values3.215 and4.5. Release crash buffer was empty. See the release screenshot and QA backup fixture.

Evidence: `evidence/v31/device-summary.json`, final/data/UI logs, core-test XML, build logs and growth screenshot. Review regressions reproduced rounded measurements, changed unknown units and enabled completion for dirty drafts before the fixes. Today now consistently returns to the dashboard.

Remaining polish from the update review: chart axis/date labels, database-side filtering of bottle/shopping history pages, and extended international digit entry. Exact dates/values and older records remain available in the history views. Physical-device audio, Android 17 background/notification behavior, font-scale/TalkBack review, clinical/editorial review and private release signing remain public-release gates.

## Historical 3.0.0 verification

# QA ledger · SproutBook 3.0.0

Verified 30 September 2026. This ledger describes executed checks and their limits.

## Build and environment

Kotlin / Jetpack Compose; Room schema 9; min SDK 26, compile/target SDK 37.
JDK 17, Gradle 9.3.1, AGP 9.1.1, Kotlin 2.2.10 and KSP 2.3.12.
The final debug APK, instrumentation APK, R8-shrunk unsigned release APK and
unsigned release AAB compiled successfully. Release lint-vital passed.
See `evidence/delivery-build.log` and `evidence/build-metadata.txt`.

Device checks used an AOSP Android 10/API 29 x86_64 emulator, 360×800 dp,
software CPU/GPU with animations disabled and no speaker output. This environment
cannot establish Galaxy S25 frame rates, audible sound quality or API 37
permission/background behavior. Test fixtures use dummy family records and clear
app data; run them only on a dedicated test installation.

## Automated results

- **51 core unit tests passed**, zero failures/errors/skips, freshly rerun with
  `:core:test --rerun-tasks`. XML results and `delivery-core-tests.log` are included.
- **33 unique Android instrumentation tests passed**, zero remaining failures.
  Results are the union of completed batches and targeted corrective reruns,
  rather than a claim that one uninterrupted full-suite run passed. The exact
  test-to-log mapping is in `evidence/device-summary.json`.

| Area | Executed coverage |
| --- | --- |
| Foundation | Onboarding navigation; multiple children; database reopen; schema 1→9 migration retaining records; independent child data |
| Data integrity | Atomic milestone/leaf writes; caregiver guards at repository/UI; text and pump limits; collision ownership; history pagination/filtering; typed record create/update/delete |
| Backup | Export/read/restore including a photo and tree position; invalid backup rejected without changing records; core validation of versions, dates, IDs, orphan rows, numeric bounds and photo paths |
| Navigation | All 12 Care destinations; profile/settings; backup and diagnostics |
| Feeding | Bottle add/edit/delete; left/right/both nursing and pump amounts; formula reset on child switch; 201st feeding reachable |
| Native gestures | Calendar long press keeps the selected date; tooth tap only views and long press edits; scrolling across a tooth does not activate it; tree tap-to-snap and long-press drag; scrolling does not open a leaf |
| Care flows | Memory timeline add/edit/delete; food and weekly meal save; inventory save/adjust/delete; pregnancy kick/contraction persistence; completed milestone creates a leaf; health add/edit/delete; emergency card edit and clipboard |
| Sleep/audio | Start/stop sleep saves; UI selects brown noise, 15-minute timer and volume, starts AudioTrack service and stops it; separate service test starts/stops all four sounds |
| Notifications | Real WorkManager execution requires opt-in and a current appointment; stale work is suppressed |
| Accessibility | Native framework inspection of full tree and Arrange mode; every leaf/branch remains accessible in grouped actions; branch 32 placement persists |
| Density | Four children, 500 memories, 500 feeds, 300 appointments; 32-leaf tree chapters; next/previous chapters; Today remains reachable |

Initial runs found two test-harness problems: an uncomposed lazy-list row and
back navigation bypassing a native dialog. Corrected tests were rerun and passed.
A separate real UI issue was reproduced: sleep success snackbars could cover the
sound button during rapid logging. Sleep's timer/history already show success;
removing these two redundant success banners fixed the existing UI regression.
Errors still produce readable feedback. The unchanged regression then passed,
with a real `play` command recorded by the native service.

A later native accessibility inspection exposed a full-canopy crash that
Compose-only gesture tests had missed: 32 custom actions on one Canvas exceeded
the framework provider limit. Leaf and branch actions now appear in groups of
16, with next/previous groups and cancel-selection actions. The new regression
failed on the old APK and passed on the fixed APK, including native node queries
and moving a leaf to branch 32. Its red result and original crash are preserved.

## Manual and release checks

- With Wi-Fi and mobile data disabled, force-stop/relaunch returned to the saved
  family dashboard. A copied database passed SQLite integrity checking and
  retained four children, 500 memories, 500 feeds and 300 appointments. See
  `offline-persistence.json` and the offline screenshot.
- Actual Today, full-canopy Memory Tree and monthly Schedule screenshots were
  visually inspected; they are included in `evidence/`.
- After each screen settled, 15-second idle samples on Today and the full tree
  recorded **zero app frames**. The percentile fields in a zero-frame gfxinfo
  dump are meaningless and are not used as latency claims. Single debug-build
  PSS samples were 63,611 KB on Today and 77,660 KB on the tree, with one Activity
  and zero WebViews. These samples establish neither a leak-free result nor
  hardware frame rates. No continuous redraw was observed in those intervals.
- The software emulator showed a System UI boot ANR; it was dismissed before the
  successful idle samples. Captures taken while the dialog was present or after
  the pre-fix tree crash were discarded.

The unsigned shrunk release is temporarily signed with the public development
key only for emulator smoke testing. That QA signing is not a production key.
The native file-picker export → validated review → restore → re-export flow
passed on that shrunk release. All 1,304 records, photos and exported preferences
were identical after the round trip; the final crash buffer was empty. The
initial attempt was interrupted by Android's package-update stop, with an empty
crash buffer. See `release-backup-summary.json` and the review/restore screenshots.
These are bounded emulator checks, not a production performance certification.

## Remaining public-release gates

- Physical-device listening, volume/output behavior, interruptions, screen-off
  longevity and battery use; modern Android foreground-service restrictions.
- Android 17/API 37 notification permission and background behavior on supported
  hardware; Galaxy S25 startup, scrolling, tree interaction and memory profiling.
- Full TalkBack/focus and large-font review, keyboards/photo/document providers,
  rotation/process recreation and all supported Android configurations.
- Clinical/editorial review of advice and emergency guidance, signed release
  identity, public privacy policy/contact details and Play Console declarations.
- Three deferred maintenance issues documented in `Review.md`.

The evaluated APK is a native development build for device testing. No assertion
is made that every button on every device configuration has been exhaustively
verified, or that the app is approved for public store release.

## 3.3.0 executed update checks · 1 October 2026

The final native debug APK, Android-test APK, R8 release APK and release bundle
built successfully. Seventy-five core tests plus one app unit test passed.
Twenty-five unique emulator tests passed across completed batches and a corrective
navigation rerun; no incomplete tests or remaining failures are counted as passes.
The exact mapping is in `evidence/v33/v33-final-summary.json`, and the update
scope, review corrections, original failures, screenshots and limits are in
`v33-ledger.md` and `v33-review.md`. Hardware audio quality, long sessions, battery
and Galaxy S25 performance remain unverified by this update.
