# SproutBook · native Android 3.8.0

A fresh Kotlin / Jetpack Compose parenting application. Package: `com.nothatcher.sproutbook`. There is no WebView, website runtime, Godot dependency, account requirement or Internet permission.

Version `3.8.0` / code `30800` supports Android 8.0+ (min SDK 26), with compile/target SDK 37. [Download the evaluation APK](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.8.0/SproutBook-3.8.0-debug.apk) · [Installation guide](../docs/INSTALL.md) · [Release notes](../docs/RELEASE-3.8.0.md).

## Build and install

Use Android Studio with JDK 17, Android SDK Platform 37.0 and Build Tools 36.0.0. Create `local.properties` containing `sdk.dir=/your/android/sdk`, or set `ANDROID_HOME`.

```bash
./gradlew :core:test :app:testDebugUnitTest :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:assembleRelease :app:bundleRelease
```

Run instrumentation tests only on a dedicated emulator or test installation: their fixtures clear app data. Do not run them on a device containing family records.

The wrapper pins Gradle 9.3.1 and verifies its distribution checksum. The optional `SPROUT_BUILD_ROOT` environment variable moves build outputs outside the checkout. `tools/bootstrap.py` and `tools/emulator.py` document the temporary Linux QA toolchain; normal Android Studio installation is preferred for development.

Debug APKs use the development signing key and are installable for evaluation. Release outputs are unsigned until you supply your own upload keystore through Android Studio's **Generate Signed Bundle / APK** workflow. Keep keystores and passwords outside this repository. Preserve the same signing identity for future updates. Do not distribute a debug key as a production signing identity.

Supply the existing evaluation key from outside the checkout with
`SPROUT_DEBUG_KEYSTORE=/absolute/path/development-signing.keystore` or
`-Psprout.debugKeystore=/absolute/path/development-signing.keystore`.
The existing debug alias/password is the standard Android debug value. If the
key is unavailable, debug signing fails instead of generating another identity.
Certificate SHA-256 must match
`9c97e149698a89e51ef69e5a6af1649b1b55004f72f8cf57fd7993c5290ec0f3`
to update the published 3.4/3.5/3.5.1/3.6 evaluation installation. A production-signed app
requires its original private production identity; the evaluation key cannot
update it. No signing key is committed or included in the new source archive.

On small ephemeral Linux hosts, set `SPROUT_TOOLCHAIN_ROOT` to a filesystem with
at least 8 GiB free before running the optional bootstrap/emulator scripts.
The emulator uses the current user's Android directory (or `ANDROID_AVD_HOME`).

The source archive contains the tracked native project, artwork and QA evidence. Generated build outputs and signing keys are excluded. The release build produces an unsigned R8 APK and Android App Bundle locally; sign a release with your own upload key before distribution.

## Structure

- `core/`: Android-independent calculations, calendar and tree geometry, content catalog, versioned backup validation and unit tests. Business rules can be reused or ported for a future iOS client.
- `app/.../data/`: typed Room entities/DAOs, schema migrations, DataStore preferences and repositories. Repository mutations enforce caregiver mode; related writes use transactions.
- `app/.../features/`: Compose feature screens, grouped by parenting task.
- `FamilyViewModel`: lifecycle-aware StateFlow family context, selected-child repair and readable operation feedback.
- `services/`: native AudioTrack foreground sound service, optional WorkManager appointment reminders, bounded private photo storage and validated backup transport.
- `ui/` and `navigation/`: shared nature palette, accessible controls and four persistent navigation destinations.
- `HomeOrganizerCatalog`, `HomeOrganizerRepository` and `features/home/`: stable Today section/shortcut IDs, child-owned validated presentation settings, draft editor, preview and native backgrounds.

Room is the source of truth. JSON is used only as a versioned, validated interchange format. UUID identities, per-record update timestamps and child ownership leave room for a future sync adapter; cloud sharing and conflict resolution are not implemented.

## Included

Daily routines/responsibilities with dated completion and undo; potty visits; individual milk freezer containers linked to saved pumps; native diaper logs and daily counts; recorded weight/height trends; prepared bottles linked to feeding; shopping linked to low supplies; stage-aware family profiles and Today dashboard; per-child memory tree with chapters, snapping and timeline; connected calendar/appointments; bottle, nursing and pumping logs; formula planning; sleep timers/manual logs and eight native sounds with pause/resume and timers; solids, reactions, allergens and weekly meals; visual teeth and milestones; pregnancy sessions and preparation; searchable parent advice and immediate help; inventory, emergency card, health journal, caregiver read-only mode; optional appointment notifications; backup/import and diagnostics.

All personal records work offline. Guidance is general education, with primary-source links that require a browser only when opened. The app does not diagnose illness, labour, food allergies or developmental conditions, or calculate medication doses. See `docs/Guidance.md` and [the 3.6 birth-source review](docs/BIRTH-SOURCES-3.6.md) for content review notes.

## Leaves worth keeping · 3.8.0

Tree, timeline and Today leaf taps open a separate native keepsake card with a leaf outline, scrollable story, optional private photo, date, child and chapter. **Edit memory** opens the existing editor. Reading does not save or mutate records, and Grandparent mode has no edit action.

The profile previews seven per-child tree themes: Summer, Autumn, Night, Spring, Blossom, Winter and Rainbow. Their static scenery keeps the original anchors, arranging gestures and memory-category colors. A shared core catalogue drives choices, repository validation and backup validation.

Room schema 13 and backup format 5 remain unchanged. Older 3.7 apps do not recognize the four newly named themes in exports; restore those backups with 3.8 or newer. See [QA-3.8](docs/QA-3.8.md) for fresh verification and [release notes](../docs/RELEASE-3.8.0.md).

## A personal Today · 3.7.0

More → Home screen organizer lets each child arrange all 21 optional Today content sections, select and order any of 23 quick shortcuts, and choose Woodland/Morning/Meadow/Evening/Plain or a personal picture. The app header and four navigation tabs remain fixed. Hidden sections retain their records; existing stage and content conditions still govern whether a card appears. Empty quick-action selections and fully hidden content are valid. Add memory opens the new-memory editor directly, with the request consumed once per navigation entry.

Organizer changes remain a draft until Save layout. Cancel leaves saved settings unchanged; Restore defaults requires confirmation and changes the draft until saved. Preview renders the draft using the native Today page with feature actions disabled. The editor retains its draft but has zero layout size and cleared semantics during preview, preventing underlying controls from receiving taps. Grandparent mode can view/preview the saved layout but cannot edit or save. Move controls have 48dp targets and accessible names that include the section/action title and current position.

Room schema 13 adds five child columns through explicit migration 12→13: `homeSections`, `homeHiddenSections`, `homeQuickActions`, `homeBackground` and nullable `homeBackgroundPhoto`. Defaults preserve the original section order, show the original cards, choose Add memory/Schedule shortcuts and use Woodland. Organizer writes update only these presentation columns plus the record timestamp; stale profile saves preserve the latest organizer settings. Stable catalogue IDs and strict validation reject unknown or duplicate choices. No organizer operation deletes family records.

The native Android photo picker supplies one selected image. `PhotoStore` bounds the read, decodes/resizes and writes a fresh private JPEG on the IO dispatcher. A photo becomes the saved background only through Save layout. Previous and unused staged files currently remain in private storage so an export already referencing a file is not broken. If photo IO finishes after the editor is disposed, changes child, or becomes read-only, the result is not applied to that draft; the app reports that the picture should be chosen again instead of silently losing it. See [privacy notes](../docs/PRIVACY.md).

Backup format 5 includes the five settings and any referenced background JPEG. Restore stages photos under fresh filenames and remaps avatar, memory-photo and background-photo references. Native formats 1–4 remain importable with default Today choices; **3.6.0 and earlier apps reject format 5**. Retain a pre-update backup; downgrade compatibility is not claimed.

The final integrated build passed with 126 unit tests (124 core + 2 app), debug/test APKs, unsigned R8 APK/AAB and vital lint. A dedicated offline API 29 emulator passed 36 unique native checks: 35 behavioral tests and one visual fixture, including organizer flows, private photo/backup persistence, five migration origins and affected existing gestures/caregiver protections. Normal/150% fixtures produced 16 native screenshots. An actual 3.6.0→3.7.0 update preserved every old fixture row/value/count and byte-identical DataStore, then rendered the retained selected child and Grandparent mode offline. External picker UI, physical/TalkBack/API 37/R8 runtime and hardware performance remain unverified. Verified immutable artifacts, GitHub prerelease and Sites version 6 are delivered; [QA-3.7](docs/QA-3.7.md) and [release notes](../docs/RELEASE-3.7.0.md) track delivery.

## Earlier birth planning and family wishes · 3.6.0

Pregnancy preparation now includes separate attended-homebirth, freebirth, transfer and after-birth guides, backed by labelled NHS/NICE/CDC sources. Guidance and editable personal checklists are bundled offline. Each child's list keeps added items, notes and completion state; source links open externally. The same birth-planning articles are searchable in Mom/Dad advice. The content distinguishes qualified attendance from unassisted birth, supports access to care and changing plans, and gives no DIY clinical procedures.

The per-child homebirth plan stores maternity/emergency contacts, address and access, attendants/backup, support people, communication and comfort preferences, care information, transfer, aftercare and priorities. Labour focus shows the saved plan, care contact, urgent-help guidance, contraction controls and recent contractions. Dial actions open the phone app for user review and never call automatically. Labour focus is a journal/contact view, not clinical monitoring or a diagnosis.

Wishlist stores titles, notes, optional validated HTTP(S) links and Wanted/Obtained state separately for each child, with filters, editing, deletion and caregiver write guards. Child profiles choose Forest/Moss/Amber/Sky accents and Summer/Autumn/Night tree styles. The Memory Tree uses richer static Canvas scenery and shaded leaves; original 32-anchor geometry, tap/open, tap/place, long-press drag, chapters and timeline remain. Framework accessibility custom actions page through leaves and branch positions; spoken TalkBack remains untested.

Room schema 12 uses an explicit additive 11→12 migration: default Forest/Summer child appearance and a new child-owned Wishlist table. Backup format 4 includes appearance and wishlists; imports of native formats 1–3 supply compatible defaults. **Older apps reject format 4; downgrade compatibility is not claimed.** No destructive fallback or replacement signing identity is used.

Completed integrated verification includes 111 unit tests (109 core + 2 app), 17 native repository/migration/photo-backup tests and 15 unique native UI/gesture/accessibility checks on an API 29 software emulator. The final full build passed after layout polish. A real offline 3.5.1→3.6.0 install preserved all old fixture columns/rows and byte-identical DataStore settings. Final compact-layout reruns and normal/150% screenshot variants passed; current results and limits are recorded in [QA-3.6](docs/QA-3.6.md).

## Earlier woodland motion 3.5 update

The supplied Woodland Motion Pack is integrated through native Lottie Compose 6.7.1. All 46 assets are bundled in light/dark JSON and PNG forms (184 small local files); videos, GIFs and sprite sheets are excluded from the APK. Selected tab icons and visible banner accents loop gently, main pages show faint fireflies, buttons have finite press responses, and completed record saves show a confirmation sprout. List icons and empty-state illustrations stay still. The interactive Memory Tree and all native gestures remain intact.

**Reduce motion defaults on and existing preferences are preserved.** In Your family/settings, turn it off to enable motion. Animations stop for battery saver, disabled system animators, background lifecycle, keyboard entry, loss of window focus and offscreen placement. Progress is read in drawing and quantized to the pack's 24fps; screens do not run a continuous page recomposition loop. Failed/pending animation loading keeps the matching PNG visible. Motion state is exposed only through a custom test property, never an irrelevant spoken accessibility label.

The 3.5 update kept Room schema 11 and backup format 3 unchanged. See `docs/QA-3.5.md` for that build's verification and limitations, and `docs/motion-pack/` for the supplied manifest, usage notes and DejaVu font licence. Unused decorative banners are reserved for later art placements; bundling them does not start background animations.

## Earlier Woodland 3.4 graphical update

Original painted woodland backgrounds and four tab illustrations, 33 editable SVG/native vector icons, softer cards, native controls and scalable typography. All graphics are bundled offline. `design-assets/` contains original paintings, icon paths and the complete prompt set used with the built-in image tool. [Editable Figma design](https://www.figma.com/design/SjQl6MaLL4e5YqK10grN2c) contains the visual system and four tab examples. Its Starter API quota blocks additional edits; regenerated native paintings retain the same visual direction.

The earlier 3.4 update introduced a small Canvas light overlay. Version 3.5 replaces that overlay with the supplied native animation pack and shared motion policy. Room schema 11 and backup format 3 remain compatible with 3.3.

See `docs/v34-ledger.md` for that version's validation.

## Earlier 3.3 care update

The sleep hub has Sleep journal and Sound machine tabs. White, Brown, Fan, Rain,
Ocean, Hush, Stream and Breeze are generated sound textures played by a native
foreground AudioTrack service. Pause freezes the timer; switching sounds preserves
it. The notification has Pause/Resume and Stop. Sound continues with the screen off.

Little teeth shows tooth-shaped upper/lower arches, stage totals and an eruption
journal. Tap views details; long press opens the stage/date editor. Records remain
separate for each child. Mom/Dad advice has 42 expandable, searchable short reads;
switching audience clears the previous search. Decorative woodland graphics are
native, static vectors. Higgsfield generation was blocked by the account plan, so
no Higgsfield asset is bundled.

See `docs/v33-ledger.md` for this version's actual validation and hardware limits.

## Everyday tools

Routines support weekday selections, optional times, shared responsibility labels, pause and dated completion history. Today shows the remaining scheduled routines. A stage change keeps old records.

The potty journal records neutral observations with time and notes. Its toddler shortcut appears on Today; it is available for other stages through Care.

Milk freezer inventory counts Frozen containers and orders them by recorded date. A saved pump can create one linked container using its full logged amount. That stored amount is a historical snapshot, not a live copy of later pump edits. Marking a container Used or Discarded retains history and changes stock without fabricating a feeding. Manually log feeds separately. No storage expiry or safety calculation is provided.

## Data and backups

Room schema 13 retains exported schema history and explicit migrations; destructive migration fallback is not enabled. Android system/cloud backup is disabled; use the in-app export. Backups contain sensitive family information and photos as readable JSON/Base64, so store and share them carefully.

Exports use SproutBook backup format version 5. Import supports versions 1–5, upgrading older native backups with appropriate appearance, Wishlist and Today defaults. Referenced background photos are included and remapped to fresh private filenames on restore. 3.6 and earlier cannot restore format-5 exports. 3.7 accepts the original three tree styles, but cannot restore an export containing Spring, Blossom, Winter or Rainbow; use 3.8 or newer for those themes. Retain a pre-update backup before installing. Imports are limited to 32 MiB including at most 20 MiB of photos and 50,000 records. It validates all tables, ownership, identities, dates, amounts and photos before asking to replace the current family data. Restore is transactional and imports photos under fresh filenames. Notifications remain off after restore. Old HTML/WebView exports are not compatible with this format.

Grandparent mode prevents accidental changes; it is a convenience mode, not account authentication or a security boundary. Anyone using the unlocked app can turn it off in settings.

## Release checklist and evidence

Read [QA-3.8](docs/QA-3.8.md) and [the 3.8 release notes](../docs/RELEASE-3.8.0.md) for this evaluation build; `docs/QA.md`, `docs/Review.md`, `docs/Progress.md` and `docs/Plan.md` retain earlier evidence and plans. The QA ledger distinguishes actual executed checks from remaining hardware/store gates. Before a production/store release, complete real-device audio and background tests, Android 17/API 37 notification/foreground-service checks, spoken accessibility review, clinical/editorial review, production signing, privacy policy and Play Console declarations. No claim of Play approval is made.

Branding is centralized in `res/values/strings.xml`, `res/drawable/ic_sprout.xml` and `ui/Theme.kt`. Future enhancements in the project plan are not presented as working UI buttons.

## Earlier health journal maintenance · 3.5.1

Link health notes to the exact appointment through a searchable native picker.
Full date, time and place distinguish repeat visits; older results can be loaded
without a fixed history cutoff. Cancel keeps the draft link, and choosing no linked
appointment clears only that link. The picker shows only the selected child's
visits and closes when caregiver mode or child context changes. Notes-only edits
preserve the original timestamp, including the later daylight-saving overlap hour.
Room schema 11 and backup format 3 remain unchanged. Fresh evidence and remaining
limits are recorded in `../docs/CONTINUATION.md` and `docs/QA-3.5.1.md`.
