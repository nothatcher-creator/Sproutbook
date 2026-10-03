# SproutBook 3.6.0 verification

This update adds sourced birth guidance and lists, a homebirth plan and Labour
mode, per-child Wishlist/appearance, and static native Memory Tree artwork.
Use this ledger and `../../docs/CONTINUATION.md` to resume; older QA is historical.

## Build and unit results

- Fresh pre-change baseline: 78 core + 1 app unit tests passed.
- Final integrated Gradle checks: 109 core + 2 app unit tests passed; debug APK,
  test APK, R8 release APK, release AAB and vital lint passed. JDK17.0.20.12.1,
  Gradle9.3.1, pinned SDK37.0/build tools36.0.0. App3.6.0/code30600,
  package `com.nothatcher.sproutbook`, minSDK26/targetSDK37.
- Published evaluation signing identity retained: SHA256 certificate
  `9c97e149698a89e51ef69e5a6af1649b1b55004f72f8cf57fd7993c5290ec0f3`.
  No replacement key generated or committed. R8 APK/AAB are unsigned outputs,
  not production-signed distribution builds.
- Room schema12 exported; explicit migration11→12 adds child appearance defaults
  and Wishlist. No destructive fallback. Backup4 exports require the new data;
  imports of formats1–3 remain supported and fill appropriate defaults.

## Real install-as-update, offline

Installed3.5.1 →3.6.0 on dedicated API29 software emulator `emulator-5554`,
Wi-Fi/data disabled, with two synthetic child profiles, a saved feeding,
pregnancy session, completed bag item, birth note and a memory at anchor23.
Every pre-existing table, column value and row matched after migration;
DataStore bytes matched exactly. New appearance defaults Forest/Summer and
empty Wishlist were correct. SQLite integrity passed, schema11→12 confirmed.
Native selected Teen profile and read-only state rendered before and after.
Evidence: `evidence/birth-planning-2026-10-03/update-preservation.json`.

The initial UI-root probe returned null and subsequent probes expected the
wrong selected fixture child. Corrected the harness to inspect the retained
Ash/Teen/read-only selection. This was not an app crash or a passing probe.

## Review and test-first evidence

- Missing advice and native entry were reproduced before implementation.
- Six preparation repository tests ran RED: exactly three expected failures
  for cross-child overwrite, invalid unsaved toggle and backup-invalid IDs.
  Shared validation and ownership guard implemented; final6 tests passed.
- Wishlist/backup core RED/GREEN evidence is separate isolated compiler/JUnit
  execution; the full Gradle unit run above verifies the integrated build.
- Independent static review: no critical findings; two-digit local emergency
  numbers were initially rejected (fixed and regression-tested), and source
  attribution was incomplete (fixed with labelled source lists and newborn
  urgent-care link). Birth guidance distinguishes attended homebirth from
  freebirth and contains no DIY clinical procedures. Source digest:
  `BIRTH-SOURCES-3.6.md`.

## Native results and visual checks

API29 native repository/persistence/backup/migration:17 tests passed in22.819s
(6 preparation,6 Wishlist,4 migration origins and1 native photo-backup test).
This includes saved list edits/completion, named Room close/reopen, child/stage
switches, caregiver guards, cross-child overwrite rejection, old backup imports,
strict4 validation/rollback, and preserved older clinical/routine/shopping rows.

Compose birth/Wishlist flows, existing calendar/tooth/tree gestures, native
framework tree accessibility and screen fixture:15 unique checks passed.
The initial15-test run passed13 and failed2 due harness timing (restored scroll
position and waiting for Room's filtered Wishlist collector). Corrected both;
2-test rerun passed in146.359s. Retained failures and rerun logs.
Total:32 unique passing native checks; repetition is not counted as new tests.

After visual polish: seven affected birth/tree gesture/accessibility checks
passed. The screenshot check in that8-test batch failed inside Compose's
TestMonotonicFrameClock with a reentrant measure/layout exception. The identical
isolated fixture passed in122.949s; no crash-buffer entry. Native pixel review
then found stale draw frames and a snackbar race. The capture harness now waits
for Android window/accessibility idle, and observes the saved snackbar before
waiting for its removal. These are harness changes, not hidden passing attempts.

The final full-width Labour control and recreation/persistence check plus normal
screen fixture passed:2 tests in143.529s. The150% variant passed:1 in91.679s.
A final clean normal capture fixture, with useful synthetic plan notes and no
snackbar overlay, passed:1 in107.593s. Final clean150% fixture passed:1 in109.334s. Device font-scale1.5 was recorded;
font-scale1.0 restored afterward. All16 final normal/large screenshots were
pulled and inspected; final crash buffer is empty.
All six saved memory anchors remain0/4/8/12/16/20 across style changes. Summer,
Autumn and Night captures have distinct SHA256s; pixels were inspected directly.
Labour urgency remains first; normal text shows the full-width timer control in
the initial viewport. Large text uses normal scrolling to reach controls.

Separate manual native startup rendered Willow's Pregnancy dashboard and four
navigation tabs. Bounded UiAutomator hierarchy retries returned null/empty, so
external dialer and focused idle gfxinfo/meminfo were not executed. No coordinate
taps/calls were attempted. Evidence and limits: `manual/README.md`.
This does not constitute a performance or external-phone-app pass.

## Packaging and publication

Build: `:core:test :app:testDebugUnitTest :app:assembleDebug
:app:assembleDebugAndroidTest :app:assembleRelease :app:bundleRelease`,
BUILD SUCCESSFUL in5m49s,139 tasks (21 executed,118 up-to-date), exit0.
Final APK15,376,653 bytes; signature verified. APK SHA256:
`5f0a8eea9dc45fe484297bca33b6b223e3c7f477870057307e80cef4285a89ee`.
Source ZIP/checksums, Sites and GitHub publication are pending.

## Limits

API29 is software-rendered and cannot establish physical-phone FPS, battery or
audio quality. Physical GalaxyS25/iPhone website checks, spoken TalkBack,
Android17 behavior and minified runtime are not tested in this update. Labour
mode is a journal/contact view, not a diagnosis or clinical monitoring system;
emergency dial buttons require the phone app and do not call automatically.
