# SproutBook continuation checkpoint

Updated: 2026-10-02 UTC. This document describes this session's work; older QA
ledgers are historical evidence, not fresh validation.

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
- At recovery, the latest release was the evaluation pre-release `v3.5.0`. Current release is `v3.5.1`. GitHub's
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
