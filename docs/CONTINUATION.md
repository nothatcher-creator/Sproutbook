# SproutBook continuation checkpoint

Updated: 2026-10-02 UTC. This document describes this session's work; older QA
ledgers are historical evidence, not fresh validation.

## Recovery

- Repository: https://github.com/nothatcher-creator/Sproutbook, clean `main` at
  `a56dd6c` before this session. No existing uncommitted work was present.
- Latest published release is the evaluation pre-release `v3.5.0`. GitHub's
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
  without KVM or audio output. A boot System UI ANR was dismissed before app tests.
  Native regression and performance checks are pending; no physical-device test.
- Existing public Sites project recovered:
  `appgprj_6abeb834e1a88191a8c4907b567c3472`, saved version 2. Hosted source checkout
  is `/workspace/sproutbook-showcase`, starting commit
  `1c9796e93dbcebea98ada4052f31e1137dd7d22e`; `.openai/hosting.json` specifies `dist`.
  The hosting skill's bundled workflow script was unavailable via skills or disk;
  equivalent Git operations use hidden stdin credentials and never write tokens to
  files or shell arguments. Website fix is verified locally and not yet published.

## Next steps

1. Recover native health regressions: duplicate appointment labels select the first
   visit, and notes-only edits reconstruct the earlier DST overlap instant. New
   `HealthLinkFlowTest` is being compiled against unchanged app behavior.
2. Complete the health editor improvement: identity-based searchable visit picker
   with full date/time/place, lazy results, caregiver protection, and preservation
   of the original recorded instant via existing `RecordTime.edited`.
3. Finish all-tooth hit/hold/scroll checks, publish the verified showcase fix through
   the existing Sites integration, and record the terminal deployment result.
4. Compile, test affected persistence/child/caregiver/gesture flows, document exact
   results and limits, and checkpoint source and verified artifacts.

Hardware audio quality, battery use, spoken TalkBack, Android 17 background behavior,
and production signing were already open gates at 3.5.0. Do not claim them passed.
