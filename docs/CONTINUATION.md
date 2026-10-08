# SproutBook continuation checkpoint

Updated: 2026-10-08 UTC. Native 3.8.0 memory keepsakes and seven tree themes
are implemented and verified; publication is in progress. Older QA
ledgers are historical verification, not fresh testing. Do not regenerate
immutable release artifacts.

## Memory keepsakes and tree themes — 3.8.0

Recovered the native GitHub project at `1790b5bd854c771d165384834cfa23f3af912cf2`
and the existing public Sites source at
`78352f41cb7b2af2207258b634bdc1ae30c08657`; no work was rebuilt from scratch.

- Memory Tree, Timeline and Today leaves open a leaf-shaped keepsake with child,
  chapter, localized date, age, category, optional private photo and full story.
  Explicit Edit opens the existing editor and returns to the viewer after Save
  or Cancel. Grandparent mode has no edit/arrange controls and revokes controls
  when enabled while a viewer/editor is open. Deep-link dismissal survives
  recreation; state is scoped by child and deleted cards disappear.
- Spring, Blossom, Winter and Rainbow join Summer, Autumn and Night. A central
  catalogue drives native validation, backup validation and profile choices.
  Per-child profile previews are drafts until Save. Day/night seasonal artwork
  keeps exact original leaf anchors, category colors and gestures. Room schema
  13 / backup format 5 are unchanged. Apps before 3.8 reject backups containing
  the four new names; they can still restore exports using the original three.
- Final integrated build passed after responsive card buttons: 126 core + 3 app
  units (129 total), no failures, errors or skips; debug/test APK, unsigned R8
  APK/AAB and vital lint, 141 tasks in 7m22s. Version 3.8.0 / code 30800,
  min SDK 26, target SDK 37. Retained
  evaluation certificate verified; no key or credential is in source.
- Independent source review found no Important/Critical findings. The first
  native16 suite passed nine and exposed seven fixture/navigation timing
  failures; original logs are retained. The corrected 541.585-second run passed
  15/16, including all new card/profile checks. Inventory diagnostics showed
  the Saved snackbar covering the Use one tap. The fixture now waits for that
  overlay to clear before its single tap, and the final inventory check passed.
  A captured keyboard entrance race required a test-only readiness gate before
  the single Save tap. The corrected writable flow passed in 118.221 seconds.
  All four final 150% card checks passed in 252.345 seconds. Compact Timeline
  viewing/editing/Save/reopening/Back passed at actual 800×360 bounds and 150%
  text in 125.827 seconds. Card queries are scoped to the viewer; compact setup
  scrolls to the seeded chapter. Earlier fixture/test-clock failures remain in
  the QA ledger. No product changes were made for those test corrections.
  The completed selection covers 16 unique successful methods and six separate
  viewport repeats: ten fresh affected executions, eleven reused unaffected
  normal checks and one reused profile repeat. It is not a fresh full16 batch.
  The gallery case already verified ten unchanged memory records/placements
  across all four new themes in light and dark mode, with eight captures.
- Rendered Chromium151 verified all seven website themes, view/edit/save/cancel,
  keyboard focus/Escape, Add/Reset, feeding/teeth, reduced motion and long text
  at 200% on narrow/mobile/landscape screens. Twelve leaves retain separate
  44px targets at 320px/390px. Actual 3.8 native screenshots join the gallery.
  Journey CSS/motion JS retain the delivered baseline bytes.

Evidence: [native QA](../android/docs/QA-3.8.md),
[website QA](../website/QA-3.8.md), and their memory-leaf evidence directories.
Next: freeze source/APK with checksums, publish the existing public Site and GitHub evaluation release,
then record exact commits, deployment IDs and artifact verification here.
Production signing, installed minified runtime, spoken TalkBack, physical-phone
performance and Android17 background behavior remain outside emulator evidence.


## Home screen organizer delivered — 3.7.0

Request: More → Home screen organizer, reorder/show/hide Today sections, choose
and order quick actions, and built-in or custom photo backgrounds. Each child's
choices are independent. Native Kotlin/Compose implementation continues from
clean delivered HEAD `3e15d6800fabc2faf4ac2ec168c30745b8d00fde`.

- Fresh baseline 2026-10-03 18:57 UTC: `:core:test --rerun` and
  `:app:testDebugUnitTest --rerun` passed, 109 core + 2 app = 111 tests,
  zero failures/skips. Log: ignored `artifacts/organizer-baseline.log`.
- Design: 21 stable section IDs retain stage/data conditions; optional ordered
  shortcuts; original woodland plus morning/meadow/evening/plain/photo choices.
  Organizer has accessible move buttons, Save/Cancel/Preview/Restore defaults.
  Normal Today has no reorder gesture. Grandparent mode stays read only.
- Implemented core validation/backup 5, additive Room 12→13, child-owned
  configuration/photo import, editor and Today renderer. Final integrated build
  passed 126 units (124 core + 2 app), debug/test APK, unsigned R8 APK/AAB and
  vital lint in 5m49s. Retained evaluation certificate verified.
- Final organizer native suite: 20 checks passed in 307.225 seconds: seven
  repository/photo/backup/reopen checks (including caregiver mode switched during
  blocked image IO), five migration origins (1, 9, 10, 11, 12) and eight native
  Compose flows. The photo-picker result is intercepted with a real JPEG; the
  app callback/private copy/Save/Cancel/recreation/child scope were verified,
  while Android's external picker interface was not exercised.
- Affected native regression: 16 checks passed in 189.796 seconds, including
  Wishlist, everyday persistence, backup, calendar/tooth/tree gestures, tree
  framework accessibility, child-switch/caregiver and the visual fixture.
  Total: 36 unique native tests, 35 behavioral + one visual. Repeated captures
  are not additional unique tests.
- Normal and 150% visual fixtures passed (63.173 / 62.405 seconds); 16 screenshots
  captured eight views each, including deeper section/shortcut/photo controls
  and custom-photo Today in both themes. A final test-only frame-harness rerun
  passed one test in 67.831 seconds at actual font scale 1.0; it did not replace
  the 16 evidence screenshots. The dark banner contains its Today/date labels.
- Actual published 3.6.0 APK → new 3.7.0 APK update retained every old fixture
  row, column value and table count, schema 13 integrity `ok`, compatible new
  home defaults and byte-identical DataStore. Actual offline native rendering
  retained the selected Ash/Teen profile, appearance and Grandparent mode.
  Cold `am start -W` timed out on the software emulator; the updated UI later
  rendered. This is not hardware startup or FPS evidence.
- Original seven-UI run passed three and failed four anchor fixture assertions;
  corrected fixtures now use existing arrangeMemory to save their requested
  branch, and the failure log remains in evidence. No app behavior change was
  needed for those assertions. The latest corrected flows and affected existing
  regressions passed; original failures remain available for future sessions.
- Independent data/UI review complete; three UI findings fixed: preview removes
  hidden hit targets, detached photo results show feedback, Add-memory query is
  consumed once per navigation entry. See committed review evidence.
- Local browser verification passed: Chromium 153 and WebKit 26.6, five viewports
  plus two no-JavaScript cases each; Chromium 200% text, two viewports. The
  11-screen gallery passed desktop wrapping/mobile horizontal navigation.
  Journey CSS and motion JS are byte-identical to the delivered Site.
- Existing public Site version 6 is published from exact source commit
  `78352f41cb7b2af2207258b634bdc1ae30c08657`. Live Chromium passed five viewports
  and two no-JavaScript cases. Hosted APK/checksum/source parts were downloaded
  and matched the immutable artifacts. GitHub v3.7.0 evaluation prerelease is
  published; workflow `37156726604` succeeded. All three GitHub assets downloaded
  and matched exact frozen bytes, GitHub digests, source CRC and retained signer.
  Release: https://github.com/nothatcher-creator/Sproutbook/releases/tag/v3.7.0
  Site version ID: `appgprj_6abeb834e1a88191a8c4907b567c3472~appgver_618fcf94ddb48191bc58fa18e4510312`.
  Deployment ID: `appgdep_6ac17810d8dc8191a74b1ede52ab188c`.
  Release target: `ca593130ceecb2f0a0e90518b7d3e5d9a08c3f15`.
- Current code checkpoint: `27ecbf9` (organizer implementation). Final source/QA snapshot is `354c805ad83bbf51790a7169c6bd25b52c35f25b`.
  Frozen source ZIP is 43,753,394 bytes, SHA-256
  `a160c25c2c4c0a96221fa2d54b4a9d6388cd4ac776d3c996917f80ee34f3a263`.
  ZIP CRC/native project/no-private-key checks passed. Static hosted APK/source
  checksums and separate DOM simulation passed.
  Frozen APK has 15,365,584 bytes, SHA-256
  `b21820ca110a62fe588672dee4654cf08089b495414a5541c946ee8f518ee484`.
  Immutable artifacts remain in `artifacts/3.7.0/`; do not regenerate them
  for later documentation changes.
- Delivery used exact Git-data objects because this host's Git receive credential
  was rejected. A slow CLI blob transport was completed with the GitHub connector,
  matching blob SHA values; main advanced once without force. The source helper
  preserved every exact local commit. Short-lived Sites credentials stayed in
  memory/hidden stdin. No private key or credential was committed.
- Next: physical Android organizer/photo-picker/rotation/restart/backup checks;
  spoken TalkBack and large-text regression; API 37 service/notification testing;
  installed R8 runtime with original production identity when available; measure
  real-device startup/FPS/battery/audio. Keep stage changes and child switches
  non-destructive. Add backup-safe cleanup for unused private backgrounds only
  after protecting concurrent exports. Use [QA-3.7](../android/docs/QA-3.7.md) and
  evidence before choosing another small feature; organizer implementation is done.
- Limitations: external photo picker UI and cancellation/rotation during active
  image IO; physical devices, spoken TalkBack, API 37 service/notification and
  installed R8 runtime; hardware FPS/startup/battery/native-audio evidence remain
  open. Production signing key unavailable. Previous and unused staged background
  images remain private to avoid racing exports; backup exports referenced files.
  Test fixtures must only use synthetic data on a dedicated emulator.

Evidence: `android/docs/QA-3.7.md` and
`android/docs/evidence/home-organizer-2026-10-03/`. Do not restart recovery or
repeat completed native suites without a relevant change or unresolved concern.
Continue from schema 13 / backup 5; preserve every child's data and the evaluation
signing identity. Publication of 3.7.0 is complete.

## Native 3.6 delivered

User requests: homebirth/freebirth advice and lists, a homebirth plan, Labour
mode, a per-child wishlist, more child customization and a nicer Memory Tree.
The checkout was clean at `b4d2c8b` before this update. Checkpoint `297ba4b`
contains the integrated native implementation. Existing signing identity and the
published woodland journey are preserved.

- Fresh baseline: 78 core + 1 app unit tests passed, 2026-10-02 23:54 UTC.
- Implemented offline sourced homebirth/freebirth/transfer/aftercare guidance,
  editable personal lists and a 12-field per-child homebirth plan. Labour focus
  brings urgent help, saved contacts, native contraction timing/history and plan
  preferences together. It opens a dialer for review and does not call directly.
- Implemented child-owned Wishlist, four woodland accents and three tree styles.
  Static native Canvas polish retains exact anchors, gestures and accessibility.
  Room 12 uses explicit additive migration 11→12; backup 4 exports include new data,
  while native formats 1–3 remain importable. Older apps reject format 4.
- Source digest: `android/docs/BIRTH-SOURCES-3.6.md`. Independent review findings
  (two-digit emergency numbers and incomplete source lists) fixed and verified.
  No DIY clinical procedures or equivalence claim for unassisted birth.
- Integrated Gradle: 109 core + 2 app unit tests passed; debug/test APK,
  unsigned R8 APK/AAB and vital lint passed. Final full-width Labour button polish
  passed the same full build (5m 49s). Evaluation certificate remains unchanged.
- Dedicated offline API 29 emulator: real 3.5.1→3.6.0 update preserved every old
  fixture row/value and byte-identical DataStore. 17 repository/migration/backup
  plus 15 unique UI/gesture/framework-accessibility checks passed before final
  visual polish. Corrected two harness timing failures; original logs retained.
- Final affected rerun: seven birth/tree gesture/accessibility checks passed.
  Screenshot fixture initially failed inside Compose test-frame layout, then
  passed unchanged in isolation. Pixel inspection found stale screenshot frames;
  capture harness now waits for native Android window/accessibility idle.
- Final full-width Labour persistence/recreation check and normal fixture passed;
  clean normal/150% fixtures passed with 16 inspected screenshots. Three distinct
  tree scenes retain saved anchors. Final crash buffer empty. Manual startup
  rendered; external dialer/idle profiling blocked by missing UiAutomator roots.
- Frozen source checkpoint `3a027bb29fe4265c793e1a7f48e20f1ba4fc426a`;
  ZIP 39,871,429 bytes/1135 entries, CRC passed, no keys/build outputs.
  APK 15,376,653 bytes, retained certificate and SHA verified. Both digests are
  in `SHA256SUMS-3.6.0.txt`; artifacts are never regenerated for doc updates.
- Published GitHub evaluation prerelease v3.6.0 at 2026-10-03 16:46:11 UTC.
  Release workflow 37138059905 succeeded from metadata commit 473aebe. Actual
  downloaded assets matched checksum file/GitHub digests; ZIP CRC and APK
  certificate verified again. Native artifact publication evidence is committed.
- Published existing public Sites version 5 at 2026-10-03 16:44:38 UTC, source
  `1f393520be147bcff40f718c459331a29e302115`, version
  `appgprj_6abeb834e1a88191a8c4907b567c3472~appgver_a9c1cf7803cc8191a32d984aff5a43dd`,
  deployment `appgdep_6ac130de87848191bbb45d97523f9273`, status succeeded.
  A fresh short-lived credential fixed an expired-token source push.
  Local Chromium/WebKit five viewports each, four no-JavaScript cases and
  two 200% text cases passed. Actual live Chromium five viewports and both
  no-JavaScript cases passed after publication. Website CSS/motion controller
  unchanged. Site checkout clean at published source; source QA snapshot
  predates publication; GitHub final ledger is authoritative.
- Physical-phone FPS/battery/audio, spoken TalkBack, Android 17 behavior, minified
  runtime and original private production signing remain unverified/unavailable.

Evidence: `android/docs/evidence/birth-planning-2026-10-03/`.
Build logs and staged artifacts: ignored `artifacts/3.6.0/`.
Downloads: [APK](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.6.0/SproutBook-3.6.0-debug.apk),
[complete source](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.6.0/SproutBook-3.6.0-source.zip),
[release/checksums](https://github.com/nothatcher-creator/Sproutbook/releases/tag/v3.6.0).
Website: https://sproutbook-woodland.nothatch.chatgpt.site

Next: physical Android regression with font scaling/TalkBack, real external
phone/browser actions, Android 17 notification/background/native sound behavior
and measured hardware performance. Do not repeat recovery or rebuild frozen
artifacts. A possible future complete improvement is offline birth-plan PDF
export for sharing with the maternity team; it is not implemented in 3.6.
Continue from schema 12/backup 4, QA-3.6 and the actual code, preserving all child
records and original evaluation signing identity. Instrumentation fixtures
replace synthetic records and must never run on a family installation.

## Woodland journey follow-up

Website-only enhancement, 2026-10-02: sunrise canopy → memory oak → appointment
noticeboard → feeding picnic → evening nursery → growing sapling → interactive
clearing and native screenshots → nighttime download. Four depth layers,
scroll-reversible plants/animals/cards/phones, three matching optimized generated
assets, original painted scenes and motion graphics. The APK and all native
source/signing/release artifacts remain 3.5.1 and were not rebuilt for this task.

- Actual local Chromium 153 and WebKit 26.6: all five viewport cases per engine
  passed (1440×900, 390×844, 360×640, 320×700, 667×375), including preserved sample
  demos, reverse poses, wheel, preferences, asset loading and script errors.
- Four no-JavaScript static cases and two 200% text Chromium cases passed.
- Static asset/version/download SHA-256 verification and app.js DOM simulation
  passed separately. These are not native tests. Physical phone browsers,
  screen-reader speech, GPU/frame rate and battery performance remain unmeasured.
- Details, reproducible verifiers, performance limits and committed evidence:
  `website/QA-JOURNEY.md`, `website/STORY-JOURNEY.md`,
  `website/docs/evidence/woodland-journey-2026-10-02/`.
- Source Site: `/workspace/sproutbook-showcase`, existing public deployment.
  Opening fetched the exact source branch with no conflicts or pre-existing
  uncommitted changes. Bundled skill workflow scripts were unavailable in the
  cloud skill package; the existing hidden-stdin Sites Git helper was reused.
- Publication: SUCCESS, 2026-10-02 23:21:55 UTC. Existing public Site version 4,
  source `b48193de6014bac30e6978f5d989ec395b476185`, saved version
  `appgprj_6abeb834e1a88191a8c4907b567c3472~appgver_d1a14664ec30819183f1e47622559cb6`,
  deployment `appgdep_6ac03c8948288191a90f2a3bb7e7b143`, native status `succeeded`.
  URL https://sproutbook-woodland.nothatch.chatgpt.site
- Actual live Chromium: all five viewport regressions and both no-JavaScript
  cases passed after publication. Local WebKit and 200% checks remain separately
  identified. Live scripts/styles returned HTTP 200 and must revalidate caches.
- Site source checkout remains clean at the published source commit. Its saved
  QA snapshot predates publication; final GitHub QA/continuation records are the
  authoritative delivery ledger. GitHub website source uses public release
  download URLs; hosted Site keeps verified local APK/checksum downloads. The
  actual artwork, markup, CSS and controller are mirrored. Frozen release ZIPs
  and checksums were not regenerated.

Next: physical Android Chrome/iPhone Safari scroll smoothness,
reduced-motion/assistive-technology review and optional GPU/battery measurement. Do not claim a hardware FPS result from
headless callback timing.

## Recovery

- Repository: https://github.com/nothatcher-creator/Sproutbook, clean `main` at
  `a56dd6c` before this session. No existing uncommitted work was present.
- At recovery, the latest release was the evaluation pre-release `v3.5.0`. Current release is `v3.6.0`. GitHub's
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
`com.nothatcher.sproutbook`; current Room schema 12, backup format 4, min SDK26, target37.
Keep Today / Schedule / Care / More, offline records, explicit migrations,
child ownership, caregiver guards, original graphics, and motion gating.

Never run instrumentation fixtures on a family installation: they clear data.
Use a dedicated emulator. Do not commit signing material or credentials.

## Historical baseline and 3.5.1 verification

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
  backup format 3 remain unchanged. Released version is 3.5.1 / 30501.
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
  `appgprj_6abeb834e1a88191a8c4907b567c3472`, originally saved version 2, now published version 3. Hosted source checkout
  is `/workspace/sproutbook-showcase`, starting commit
  `1c9796e93dbcebea98ada4052f31e1137dd7d22e`; `.openai/hosting.json` specifies `dist`.
  The hosting skill's bundled workflow script was unavailable via skills or disk;
  equivalent Git operations use hidden stdin credentials and never write tokens to
  files or shell arguments. Website update published successfully; four live rendered-browser cases also passed.

## Delivered release and website

- Evaluation pre-release: https://github.com/nothatcher-creator/Sproutbook/releases/tag/v3.5.1
- APK: https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.5.1/SproutBook-3.5.1-debug.apk
- Complete source: https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.5.1/SproutBook-3.5.1-source.zip
- Local frozen artifacts: `artifacts/3.5.1/`; APK 15,097,626 bytes, source ZIP
  33,454,252 bytes. Source manifest checkpoint:
  `42d2ef64ac98a90a65df32d87c0dd8553e370a34`.
- APK SHA-256: `bdbef96bdf73dd92d353d043b3e616e49e199915aedf8d003f547c95530c8bca`.
- Source SHA-256: `5404e40d93fd2fb70238b06bbe0f7f1813519488f856ff7ef16418d033c2bbda`.
- ZIP CRC and every manifest file hash passed. No key or credential packaged.
  Both hosted source halves reassemble exactly and meet the hosting file limit.
  All three published GitHub asset sizes/digests, including the checksum file,
  match verified local files. Do not regenerate this release's frozen artifacts.
- GitHub main contains the full recovered native source and exact local commits.
  HTTPS Git receive returned 401 on this host. The authenticated Git Data API
  uploaded matching blobs/trees/commits and performed one non-force fast-forward;
  no remote changes were overwritten. Helper/cached SHA list are in
  `/workspace/scratch/sproutbook-api-push.py` and `sproutbook-api-upload-state.json`.
  A large flat tree timed out; building direct child trees resolved it.
- Direct source-asset upload returned `Bad Content-Length`; the draft was not
  published. The existing release workflow then downloaded the verified Sites
  artifacts, checked SHA-256/ZIP/native source/no-key conditions, and published all
  three assets successfully. Run:
  https://github.com/nothatcher-creator/Sproutbook/actions/runs/37070151376
  The workflow is now explicit `workflow_dispatch`, versioned, and refuses to
  overwrite existing releases. Publication log is archived with browser evidence.
- Existing public Sites deployment **succeeded** on 2026-10-02 at 21:58:53 UTC:
  https://sproutbook-woodland.nothatch.chatgpt.site
  Saved version 3: `appgprj_6abeb834e1a88191a8c4907b567c3472~appgver_656629d908b8819195c5862fde354c9f`.
  Deployment: `appgdep_6ac029092a148191b1bf736d6c858bee`.
  Pushed source: `5ee5c4e2bfb31b1972abd554142b4dd9b8b652b5`.
  Public audience and older 3.5.0 downloads preserved. The archive was generated
  from that exact committed `.openai/hosting.json` and `dist` source.
- All four final local **and live** Chromium viewport cases passed with 3.5.1
  metadata, native screenshot and feature-request context. Live evidence:
  `docs/evidence/browser-2026-10-02/live-v351-browser.log`,
  `live-v351-results.json`, and `live-downloads-v351.png`.
  Static/checksum and simulated DOM checks are separate passing evidence.
  Website demos are temporary sample state, separate from native family records.

## Continuing from this checkpoint

1. Start with `android/docs/QA-3.5.1.md`, not only the historical 3.5 ledger.
   Clone main; the native project is now present. Use the preserved external
   evaluation key if available, never an automatically generated replacement.
2. On an actual Android 17/API 37 device, prioritize background notification/audio,
   spoken TalkBack, motion eligibility, battery and smoothness. Also check the R8
   runtime with the proper signing identity. None of these physical/runtime gates
   passed here; the API 29 software emulator is functional evidence only.
3. The finite press animation can replay when motion eligibility returns. Review
   that cosmetic issue and audit another genuine feature gap before choosing the
   next small update. Keep schema/migrations/backup and per-child records intact.
4. Compile → test affected flows → fix → update this checkpoint and the QA ledger.
   Bump semantic version before publishing new artifacts. Preserve both hosting
   sources and use Sites for website publication; GitHub `website/` alone is a mirror.

No current delivery blocker remains. Production signing, hardware audio quality,
battery use, spoken TalkBack, Android 17 background behavior and minified runtime
remain open gates. Do not describe them as passed.
