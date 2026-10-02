# SproutBook 3.5.1 · Care notes, connected

[Download APK](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.5.1/SproutBook-3.5.1-debug.apk) · [Complete source](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.5.1/SproutBook-3.5.1-source.zip) · [Showcase](https://sproutbook-woodland.nothatch.chatgpt.site)

## What changed

Two check-ups with the same title and date could previously link a health note
to the wrong visit. The health journal now selects the exact appointment in a
native searchable picker, showing full date, time and clinic/place. Search reaches
older visits before limiting results; pages of 50 keep the list manageable.
Cancel keeps the draft link. Clearing a link leaves the appointment intact.

Editing notes previously shifted the later daylight-saving repeated hour back
one hour. Notes-only edits now preserve the original recorded instant, including
seconds and milliseconds. Child switching discards unsaved drafts, and caregiver
mode closes the mutable picker while repository guards continue to block writes.

Recovered the complete checksum-verified native project into `android/` in Git.
The existing Kotlin/Compose, Room/DataStore, native audio, graphics, four tabs,
offline records, explicit migrations and backup format remain in place. The source
download includes the actual Android project, QA evidence, artwork, motion pack,
and unsigned R8 APK/AAB/mapping. Signing keys and credentials are excluded.

The website's overlapping lower tooth arch blocked upper-tooth clicks in a real
browser. Containers now pass pointer events to their tooth buttons. Scroll story,
motion controls, sample demos and feature-request drafts are preserved. Download
metadata and the new native health-picker screenshot now describe 3.5.1.

## Downloads and identity

| Item | Detail |
| --- | --- |
| Version / package | 3.5.1 / 30501 · `com.nothatcher.sproutbook` |
| Android | 8.0+; target SDK 37 |
| APK | Debug-signed evaluation build, 14.4 MiB |
| Source ZIP | Complete native project in `android/`, 31.9 MiB |
| Source checkpoint | `42d2ef64ac98a90a65df32d87c0dd8553e370a34`; manifest verifies all packaged files |
| APK SHA-256 | `bdbef96bdf73dd92d353d043b3e616e49e199915aedf8d003f547c95530c8bca` |
| Source SHA-256 | `5404e40d93fd2fb70238b06bbe0f7f1813519488f856ff7ef16418d033c2bbda` |

The existing evaluation certificate is preserved:
`9c97e149698a89e51ef69e5a6af1649b1b55004f72f8cf57fd7993c5290ec0f3`.
No replacement key was generated. Missing-key signing deliberately fails.
Export a backup and install over the existing evaluation app; avoid uninstalling.
Production-signed installations require their original production key.
[Installation instructions](https://github.com/nothatcher-creator/Sproutbook/blob/main/docs/INSTALL.md) · [Checksums](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.5.1/SHA256SUMS-3.5.1.txt).

## Verification

- Debug APK, instrumentation APK, R8 unsigned release APK/AAB and vital lint passed
  on JDK 17 / Gradle 9.3.1 / SDK 37.0 / build tools 36.0.0.
- 79 unit tests passed at baseline (78 core, 1 app). The final app test reran and
  passed; unchanged core tests were up-to-date. No unit failures/errors/skips.
- 21 unique Android tests passed on a dedicated API 29 software emulator. Coverage
  includes exact links/timestamps, older search/pagination/Back/Cancel/clear,
  child/caregiver guards, reopen/stage/backup persistence, three migrations,
  photos/invalid-import rollback, calendar/tooth/memory gestures, health CRUD,
  actual native sound controls and four-tab navigation. At 150% text, the new
  picker selected the exact visit and light-theme feeding saved 80 mL.
- Install-as-update from rebuilt original-source 3.5.0 preserved every fixture
  table row and DataStore byte, including two children and caregiver mode. Offline
  startup and SQLite integrity checks passed. Published/rebuilt/new certificates
  match; Room schema 11 and backup format 3 are unchanged.
- Four actual rendered Chromium viewport cases passed: forward/reverse SVG frames,
  normal scrolling, idle pause, still layouts, motion-off persistence, live reduced
  motion, all 20 tooth targets/hold/movement cancellation, sample flows and GitHub
  draft feedback. Static/checksum and simulated DOM checks are separate evidence.
- One stable 8-second native picker idle sample with motion disabled rendered
  zero new app frames. This is bounded emulator evidence, not phone performance.

Initial reproduced bug failures and a corrected test-loading timing failure are
retained in the evidence. Only completed final passing results are counted.
[Native QA](https://github.com/nothatcher-creator/Sproutbook/blob/main/android/docs/QA-3.5.1.md) · [Website QA](https://github.com/nothatcher-creator/Sproutbook/blob/main/website/QA.md) ·
[Persistent continuation checkpoint](https://github.com/nothatcher-creator/Sproutbook/blob/main/docs/CONTINUATION.md).

## Remaining limitations

Evaluation pre-release; no physical-device/Galaxy S25, spoken TalkBack, speaker
quality, battery, Android 17 background/notification, or minified-release runtime
claim. The entire historical instrumentation suite was not rerun. A previously
documented finite press-animation replay remains cosmetic. Production signing
still requires the original private production identity.

Browser checks use headless Chromium on Linux with mobile-layout viewports; no
physical-phone browser, Safari/Firefox or supported WebMCP-browser claim. Website
publication is tracked in the continuation document through the existing Sites
project; committing `website/` alone does not publish it.
