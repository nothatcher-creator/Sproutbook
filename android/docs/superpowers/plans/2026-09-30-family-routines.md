# Family Routines Implementation Plan
> **For agentic workers:** Use superpowers:executing-plans to implement this plan task-by-task.
**Goal:** Ship native routines, potty history and pumping-connected milk inventory in 3.2.0.
**Architecture:** Typed Room tables and child-scoped database flows, guarded repository mutations, existing native Compose editors and navigation. Strict backup transport version3 normalizes v1/v2 backups.
**Tech Stack:** Existing Kotlin/Compose/Room/DataStore, no new dependencies.
**Spec:** docs/superpowers/specs/2026-09-30-family-routines.md

## Global Constraints
- minSDK26 / targetSDK37 / com.nothatcher.sproutbook / version3.2.0 code30200.
- Preserve 3.1.0 records and public development signing identity.
- Native controls, offline persistence, caregiver read only, no unsourced milk expiration/medical claims.

## Review Focus
- Concurrent completion or repeated pump transfer must not double count.
- Parent/child links must reject foreign children on both save and restore.
- Dirty terminal-action drafts must save first.
- Prior backups must retain all old records, and current backups require all tables.
- Switching children or enabling caregiver while an editor is open must guard mutations.

### Task 1: Persistence and transport
Files: core/RoutineRules.kt, core/BackupFormat.kt; data/Routine.kt, RoutineCompletion.kt, PottyLog.kt, MilkContainer.kt, EverydayRepository.kt, AppDatabase.kt; services/BackupManager.kt; tests RoutineRulesTest, EverydayBackupTest, EverydayRepositoryTest, MigrationTest.
Interfaces: RoutineRules.isDue(mask:Int,day:LocalDate):Boolean; repository saveRoutine(Routine), completeRoutine(id:String,child:String,day:Long,done:Boolean), savePotty(PottyLog), saveMilk(MilkContainer), storePump(id:String,child:String), finishMilk(id:String,child:String,used:Boolean). DAOs observe(child,...), all/get/save/delete, routineCompletions.forDay, milkContainers.frozenSummary.
- [x] Add rules/backup tests; run :core:test; expected new behavior failures.
- [x] Implement pure rules and strict v3 format/compatibility; :core:test PASS.
- [x] Add typed entities, Room11 migration, repository and snapshot/restore integration.
- [x] Compile debug; run instrumentation for duplicate transitions, child isolation, backup roundtrip, caregiver guards and schema10->11 migration; PASS.
- [x] Commit persistence gate.

### Task 2: Native product flows
Files: features/routines/RoutineScreen.kt, features/potty/PottyScreen.kt, features/milk/MilkScreen.kt; navigation/AppRoot.kt, care/CareScreen.kt, home/HomeScreen.kt, feeding/FeedingScreen.kt, backup/DiagnosticsScreen.kt.
Interfaces: new composables(vm:FamilyViewModel,state:FamilyState); routes routines/potty/milk. Uses Task1's APIs/flows.
- [x] Write actual Compose flows for adding/editing/deleting, dated undo/history, pump link, caregiver read-only and child switching; run before routes exist to verify missing screen failures.
- [x] Build native lazy pages and bottom-sheet editors; integrate Care, Today and Feeding.
- [x] Compile debug/test APK; run targeted Compose flows; PASS.
- [x] Commit native flows.

### Task 3: QA and deliverables
Files: docs/QA.md, docs/Progress.md, docs/evidence/v32, README.md, app/build.gradle.kts; source/APK artifacts.
- [ ] Run core full suite and Android regression batches including migration/backup/gestures/navigation; inspect every outcome.
- [ ] Build R8 release APK/AAB, unsigned for user production key; confirm debug signing matches baseline.
- [ ] One final whole-update reviewer; fix Important/Critical findings with regression proof.
- [ ] Save source archive and debug APK, update durable original identities. Report actual test evidence and limitations; never claim physical-device QA.
