# SproutBook 3.6.0 verification in progress

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
  Shared validation and ownership guard implemented; final execution pending.
- Wishlist/backup core RED/GREEN evidence is separate isolated compiler/JUnit
  execution; the full Gradle unit run above verifies the integrated build.
- Independent static review: no critical findings; two-digit local emergency
  numbers were initially rejected (fixed and regression-tested), and source
  attribution was incomplete (fixed with labelled source lists and newborn
  urgent-care link). Birth guidance distinguishes attended homebirth from
  freebirth and contains no DIY clinical procedures. Source digest:
  `BIRTH-SOURCES-3.6.md`.

## Pending native checks

Repository/persistence/backup/migration instrumentation is running. New Compose
flows, caregiver transition, child switch, gesture regressions, framework tree
accessibility, screenshots at100%/150% font and crash-buffer check are pending.
APK/source/checksums have not been published for3.6.0 yet.

## Limits

API29 is software-rendered and cannot establish physical-phone FPS, battery or
audio quality. Physical GalaxyS25/iPhone website checks, spoken TalkBack,
Android17 behavior and minified runtime are not tested in this update. Labour
mode is a journal/contact view, not a diagnosis or clinical monitoring system;
emergency dial buttons require the phone app and do not call automatically.
