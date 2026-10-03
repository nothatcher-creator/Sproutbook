# SproutBook 3.6.0 · Birth planning and family wishes

[Download APK](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.6.0/SproutBook-3.6.0-debug.apk) · [Direct APK](https://sproutbook-woodland.nothatch.chatgpt.site/downloads/SproutBook-3.6.0-debug.apk) · [Complete source](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.6.0/SproutBook-3.6.0-source.zip) · [Checksums](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.6.0/SHA256SUMS-3.6.0.txt) · [Showcase](https://sproutbook-woodland.nothatch.chatgpt.site)

## What changed

**Birth guidance and personal lists.** Pregnancy preparation now includes attended homebirth, freebirth, transfer planning and after-birth care. Read the guidance offline, follow labelled NHS/NICE/CDC source links, edit checklist titles and notes, add your own items, and mark preparations complete. The articles are also searchable in Mom/Dad advice. Content distinguishes qualified attendance from unassisted birth and focuses on informed choices, care access, warning signs and prompt professional assessment. The [source review](https://github.com/nothatcher-creator/Sproutbook/blob/main/android/docs/BIRTH-SOURCES-3.6.md) records sources checked in October 2026 and their local-care limits.

**A homebirth plan for each child.** Save maternity and local emergency contacts, address/access, attendants and backup, support people, consent/privacy and comfort preferences, care information, transfer arrangements, aftercare and priorities. Plans are personal notes to discuss with qualified professionals, not clinical clearance. Existing hospital-bag items and birth notes stay in place.

**Labour focus.** A dedicated native view brings the saved plan, urgent-help guidance, maternity contact, contraction controls and recent contractions together. The view reads the same saved records as the pregnancy journal. Dial buttons open the phone app for review and never call automatically. A timer does not diagnose labour, monitor clinical safety or decide when care can wait.

**Their own wishlist and woodland.** Each child's wishlist keeps gift, book, experience or other ideas, notes, optional HTTP(S) web links and Wanted/Obtained state. Filter, edit, mark or delete ideas while keeping child ownership and caregiver read-only guards. Child profiles now choose Forest, Moss, Amber or Sky accents and Summer, Autumn or Night tree styles. Native Canvas tree scenery, branches and shaded memory leaves have more detail; chapters, timeline, tap/open, tap/place and long-press drag remain available. Framework accessibility actions page through leaves and branch positions.

All family records, guidance and artwork remain native Kotlin/Compose and available offline. Source and wishlist links need an external browser when opened. The app has no Internet permission, account requirement or cloud sync. Existing care, health, calendar, audio, pregnancy and family features are retained.

## Native screens

<p align="center">
  <img src="https://raw.githubusercontent.com/nothatcher-creator/Sproutbook/main/docs/assets/labour-focus-screen-v36.png" width="230" alt="SproutBook 3.6 Labour focus with urgent-help guidance and native contraction controls" />
  <img src="https://raw.githubusercontent.com/nothatcher-creator/Sproutbook/main/docs/assets/homebirth-plan-screen-v36.png" width="230" alt="SproutBook 3.6 personal homebirth plan" />
  <img src="https://raw.githubusercontent.com/nothatcher-creator/Sproutbook/main/docs/assets/memory-autumn-screen-v36.png" width="230" alt="SproutBook 3.6 autumn Memory Tree with placed leaves" />
  <img src="https://raw.githubusercontent.com/nothatcher-creator/Sproutbook/main/docs/assets/wishlist-screen-v36.png" width="230" alt="SproutBook 3.6 child wishlist editor with a sample woodland storybook idea" />
</p>

Emulator captures use sample family data.

## Downloads, identity and data

| Item | Detail |
| --- | --- |
| Version / package | 3.6.0 / 30600 · `com.nothatcher.sproutbook` |
| Android | 8.0+ (min SDK 26); compile/target SDK 37 |
| APK | Debug-signed evaluation pre-release; 15,376,653 bytes (14.7 MiB) |
| Source ZIP | Complete native project, artwork and QA evidence; build outputs and signing keys excluded |
| Database | Room schema 12; explicit additive migration from 11 |
| Backup | Exports format 4; imports native formats 1–4 |
| Artifact verification | APK signature verified; APK/source SHA256s recorded in the release checksum file |

The existing evaluation certificate is retained:
`9c97e149698a89e51ef69e5a6af1649b1b55004f72f8cf57fd7993c5290ec0f3`.
Export and keep a backup before installing over the existing evaluation app.
Avoid uninstalling because it deletes local records. Missing-key signing fails
instead of generating another identity. Production-signed installations require
their original private production identity; that key is unavailable for this
evaluation release. [Installation instructions](https://github.com/nothatcher-creator/Sproutbook/blob/main/docs/INSTALL.md).

The schema 11→12 migration adds Forest/Summer appearance defaults and a child-owned
Wishlist table without rebuilding old tables or deleting records. Older native
backup formats 1–3 remain importable with new-data defaults. **3.5.1 and earlier
apps reject format-4 exports.** Keep the pre-update backup; this release makes no
downgrade-compatibility claim.

## Completed verification

- Final integrated build checks passed after layout polish: debug APK, instrumentation APK, unsigned R8
  release APK/AAB and vital lint, using JDK 17 / Gradle 9.3.1 / SDK 37.0 /
  build tools 36.0.0. Unit results: **111 passed** (109 core + 2 app).
- **32 unique native Android checks passed** on a dedicated API 29 software
  emulator: 17 repository/migration/photo-backup checks and 15 UI/gesture/framework
  accessibility checks. The UI result combines 13 initially passing checks with
  two corrected-flow reruns; failing attempts are retained as evidence.
- Repository checks cover saved list edits/completion, Room close/reopen,
  child/stage changes, caregiver guards, cross-child overwrite rejection,
  migration origins, older backup imports, strict format-4 validation and
  rollback, and photo-backup handling. Native UI checks exercise birth planning,
  Wishlist, appearance, care/navigation and existing calendar/tooth/tree gestures,
  including paged tree accessibility actions.
- A real **3.5.1→3.6.0 install-as-update** with Wi-Fi/data disabled preserved every
  pre-existing fixture table, column value and row, and byte-identical DataStore
  settings. Fixtures included two synthetic children, a feeding, pregnancy
  session, completed bag item, birth note and a memory at anchor 23. New
  Forest/Summer defaults and empty Wishlist were correct; SQLite integrity passed.

Final compact-layout native reruns passed, followed by clean normal and150%
font-scale screen checks. The tree's Summer/Autumn/Night scenes were captured
and inspected with the same six saved branch positions. Final crash buffer was
empty. Manual external-dialer and focused idle-performance checks were blocked
by unavailable UiAutomator hierarchies and are not claimed as passes.

[Native QA](https://github.com/nothatcher-creator/Sproutbook/blob/main/android/docs/QA-3.6.md) · [Continuation checkpoint](https://github.com/nothatcher-creator/Sproutbook/blob/main/docs/CONTINUATION.md).

## Remaining limitations

This is an evaluation pre-release. API 29 software-emulator evidence does not
establish physical-device performance, battery or audio quality. Physical-phone
checks, spoken TalkBack, Android 17 background/notification behavior and minified
release runtime are untested for this update. R8 build success is separate from
runtime verification. Production signing requires the original private key.

Guidance is general education and does not replace a maternity team or emergency
services. Birth-setting suitability, local transfer arrangements and aftercare
need individual professional advice. Source guidance includes UK-specific care
arrangements; the app does not select a universal emergency number or guarantee
that unassisted birth is safe. Cloud sharing, partner accounts, widgets and iOS
remain future work.
