# Phase ledger — docs/Plan.md
2026-09-29 recovery: workspace maintenance removed the previous local rebuild and
its toolchain. No v3 checkpoint was present in saved files. The conversation retains
implementation and test evidence (phases 1–10 built, 42 core tests), but those results
are historical and do not validate reconstructed files. Reconstruct and rerun all gates.
Ruling: restore the fresh native architecture from recorded implementation; do not
patch old app code. Save source checkpoints after each phase to prevent recurrence.
Known device finding to retain: bottom tabs must be disabled before NavHost is ready.

Recovered Phase 1: debug APK built; 5 core tests pass. Onboarding navigation is disabled until NavHost is ready. Device tests await emulator boot.

Recovered Phase 2: debug and instrumentation APKs compiled; 13 core tests pass.
Memory branches use persistent chapter/anchor positions, bounded photo copies and EXIF
orientation correction. Real emulator onboarding screenshot inspected; corrected dark
system bars and a default purple navigation color found during that inspection.

Recovered Phase 3: debug built; 17 core tests pass. Monthly dates, Monday week boundaries
and leap-day birthdays verified. Long press pre-fills appointment date; child birthday and
due-date events derive from profiles.
Device foundation run: 3/3 PASS on Android 10 AOSP software emulator. Includes onboarding
navigation regression, independent children after database reopen, and schema 1 migration.

Recovered Phase 4: debug APK built; 23 core tests pass. Bottle, breast and pump logs
share a typed table, with central tested unit conversions and bounded planning estimates.
Device interaction checks for these new screens remain scheduled for phase 12.

Phase 5 validation in progress: overlap/midnight sleep totals and four bounded PCM
signals were tested red before implementation. AudioTrack foreground service and
screen-off playback code are present; device playback test added, not yet run.
Phase 5 gate: debug APK builds; all 27 core tests PASS, including interval union,
overlap boundaries and energy/level/seam checks for all four sound signals.

Phase 6 gate: debug APK builds; 29 core tests PASS. Weekly meal identities are isolated
by child/date/slot; food comparisons ignore case and surrounding whitespace.
Device audio check exposed a MODE_STATIC initialization-state mistake. Corrected the
check to allow STATE_NO_STATIC_DATA before write, then require STATE_INITIALIZED after
write. Rebuild/device regression is running; no audible-hardware claim is made.

Phase 7 gate: debug APK built; 31 core tests PASS. Tooth range/stage/date rules and
stable milestone-leaf identity covered. Teeth have native long-press-only mutation.
Audio regression: all four AudioTrack sound kinds start and stop on Android 10 emulator,
1/1 instrumentation test PASS after initialization fix (qa/sound-tests.log). This verifies
native playback state, not audible output quality on hardware.

Phase 8 gate: debug APK built; 33 core tests PASS. Due-date gestation arithmetic and
start-to-start contraction intervals covered. Session updates read fresh rows under the
repository write lock, preventing lost rapid kick taps. Checklists and notes are typed
records; active timers survive process restart.

Phase 9 gate: debug APK built; 35 core tests PASS. Nine help topics have exactly three
first steps and emergency guidance appears first. Advice search supports audience,
category and case-insensitive body/title matching. Clinical review remains a public
release gate; source-backed general guidance stays available without network access.

Phase 10 gate: debug APK built; 38 core tests PASS. Stock math is bounded and rejects
non-finite input; phone actions use sanitized dial strings. Emergency cards have one
row per child. Health records link only to the same child's appointments and survive
appointment deletion with the optional link cleared. Read-only checks sit in the
repository as well as UI controls.

Phase 11 gate: debug APK built; 48 core tests PASS. Ten new tests cover backup defaults,
invalid types/enums, orphan records, duplicate identities/tree positions, unsafe photo
paths, missing tables, future versions, and past-reminder suppression. Full restore is
transactional with newly staged photo filenames. Reminders use WorkManager, optional
Android notification permission and stale-record checks. Notification taps select the
correct child and open Schedule. Android backup/photo round-trip test added for phase 12.

Phase 12 correction pass: six Important review findings addressed. Formula state is
keyed to child, local text/pump limits match backup, import checks historical dates
and full-width tooth indices, milestone leaf collisions respect child ownership,
and four history hubs support loading older records. 51 core tests passed on the
API 37 toolchain. Kotlin sources formatted consistently; tree branches refined.
Final debug, release and device QA results are tracked in QA.md.

Phase 12 evaluation gate: debug and R8 release APK/AAB built; 51 core unit tests
and 33 unique completed Android tests passed. Native accessibility and rapid
sleep/audio regressions were reproduced and fixed. Dense family, offline restart,
idle-render snapshots and shrunk-release native backup/import round trip verified.
Artifacts and exact QA boundaries are recorded in QA.md; hardware, clinical and
public-store release gates remain separate.


## Native 3.1.0 feature update

Added diaper logging, recorded growth, prepared bottles and linked shopping. Room10 and backup2 safely extend the v3.0.0 foundation. New feature editors, read-only mode, dashboard links and integrity cases are covered by tests. 59 core and 44 unique Android checks passed across completed batches; debug and unsigned release outputs compiled. See QA.md for evidence and remaining public-release gates.


## Native 3.2.0 everyday tools update

Routines, dated completions, potty visits and freezer containers use four additional typed Room tables (schema11). Backup3 accepts complete v1/v2 files while preserving their records. Care, Today and Feeding link the tools; caregiver guards remain in both UI and repositories. The first data gate passed 64 core and nine Android tests; the follow-up six-test data gate verified pump edit protection and close/reopen persistence. Final UI/regression/release evidence is recorded separately in QA.md.
