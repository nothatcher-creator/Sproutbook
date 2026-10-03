# SproutBook 3.7.0 · A personal Today

[Download APK](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.7.0/SproutBook-3.7.0-debug.apk) · [Direct APK](https://sproutbook-woodland.nothatch.chatgpt.site/downloads/SproutBook-3.7.0-debug.apk) · [Complete source](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.7.0/SproutBook-3.7.0-source.zip) · [Checksums](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.7.0/SHA256SUMS-3.7.0.txt) · [Showcase](https://sproutbook-woodland.nothatch.chatgpt.site)

## What changed

**Arrange Today for each child.** Open **More → Home screen organizer** to move any of Today's **21 content sections** up or down and show or hide each one. Your family records stay in place, and hidden tools remain available through the app's other pages. Cards retain their stage and content conditions: choosing Pregnancy, for example, does not make it appear for a teen. The app header and four navigation tabs remain fixed.

**Choose your quick actions.** Select from **23 shortcuts**, put selected shortcuts in your preferred order, or leave the list empty. Add memory opens a fresh memory editor directly. Each child's choices are saved independently, so changing one layout leaves the others in place.

**Pick a background.** Choose **Woodland, Morning, Meadow, Evening, Plain**, or **Your picture**. Android's native photo picker selects one image, which SproutBook copies into private app storage as a resized JPEG. The saved background works offline. The app does not upload the picture, change the original or import your photo library.

**Preview before saving.** Move and toggle controls include readable labels and position information, with at least 48dp touch targets. Preview shows the draft Today page with feature actions disabled. Save layout applies the draft; Cancel keeps the saved layout. Restore defaults asks for confirmation and prepares a draft that still waits for Save. Grandparent mode can view and preview the saved layout while changes remain blocked.

Birth guidance, homebirth plans, Labour focus, per-child wishlists, woodland accents and seasonal Memory Trees from 3.6 remain available, alongside existing care, calendar, health, audio and family tools. The organizer changes presentation rather than deleting family data. Family records and custom backgrounds remain native Kotlin/Compose and local; the app has no Internet permission, account requirement or cloud sync.

## Native screens

<p align="center">
  <img src="https://raw.githubusercontent.com/nothatcher-creator/Sproutbook/main/docs/assets/home-organizer-screen-v37.png" width="230" alt="SproutBook 3.7 organizer with Today section visibility and move controls" />
  <img src="https://raw.githubusercontent.com/nothatcher-creator/Sproutbook/main/docs/assets/home-quick-actions-screen-v37.png" width="230" alt="SproutBook 3.7 chosen quick actions in a personal order" />
  <img src="https://raw.githubusercontent.com/nothatcher-creator/Sproutbook/main/docs/assets/custom-background-screen-v37.png" width="230" alt="SproutBook 3.7 Today page with a custom background photo" />
</p>

Sixteen native captures (eight each at normal and 150% text) were inspected, including move controls, picture selection and saved light/dark backgrounds. Captures use sample family data.

## Downloads, identity and data

| Item | Detail |
| --- | --- |
| Version / package | 3.7.0 / 30700 · `com.nothatcher.sproutbook` |
| Android | 8.0+ (min SDK 26); compile/target SDK 37 |
| APK | Debug-signed evaluation pre-release; 15,365,584 bytes (14.7 MiB) |
| Source ZIP | Tracked native project, artwork and QA evidence; generated builds and signing keys excluded; 43,753,394 bytes (41.7 MiB) |
| Source checkpoint | `354c805ad83bbf51790a7169c6bd25b52c35f25b` |
| Database | Room schema 13; explicit additive migration from 12 |
| Backup | Exports format 5; imports native formats 1–5 |
| Artifact hashes | `SHA256SUMS-3.7.0.txt` beside the APK and complete source ZIP |
| Publication | GitHub v3.7.0 evaluation prerelease and public Sites version 6 are delivered and verified |

The existing evaluation certificate is retained:
`9c97e149698a89e51ef69e5a6af1649b1b55004f72f8cf57fd7993c5290ec0f3`.
Export and keep a backup before installing over the existing evaluation app.
Avoid uninstalling because it deletes local records. Missing-key signing fails
instead of silently generating another identity. Production-signed installations
require their original private production key; that key is unavailable for this
evaluation release. [Installation instructions](https://github.com/nothatcher-creator/Sproutbook/blob/main/docs/INSTALL.md).

The additive 12→13 migration adds section order, hidden sections, chosen quick
actions, background choice and background-photo reference to each child. Existing
profiles start with the original section order, Add memory/Schedule shortcuts and
Woodland background. The organizer saves these presentation fields without
replacing family records, and profile edits preserve the latest organizer choices.

Format-5 backups include the layout and referenced background JPEG. Restore uses
fresh private photo filenames and updates avatar, memory and background references.
Native formats 1–4 remain importable with default Today choices. **3.6.0 and
earlier apps reject format-5 exports.** Retain a pre-update backup; downgrade
compatibility is not claimed. Backups are readable and not encrypted by SproutBook.
[Privacy notes](https://github.com/nothatcher-creator/Sproutbook/blob/main/docs/PRIVACY.md).

## Completed verification

- Fresh pre-change baseline: **111 unit tests passed** (109 core + 2 app).
- Final integrated build: **126 unit tests passed** (124 core + 2 app), debug APK,
  instrumentation APK, unsigned R8 release APK/AAB and vital lint. Build completed
  in 5m49s using JDK 17 / Gradle 9.3.1 / SDK 37.0 / build tools 36.0.0.
- **36 unique native checks passed** on the dedicated offline API 29 software emulator: 7 organizer repository/photo/backup checks, 5 migration origins (1, 9, 10, 11, 12), 8 organizer UI flows, 15 retained behavior regressions and 1 native visual test. The two main runs passed 20 tests in 307.225s and 16 in 189.796s. The visual test also passed separately for normal/150% text; these reruns are not extra unique tests.
- Save/Cancel/Preview/Restore, per-child ordering/shortcuts, empty layouts, stage changes, recreation, caregiver protection, calendar/tooth/tree gestures, native backup/photo remapping and offline persistence passed. Photo-picker callback testing uses a simulated native ActivityResult with a real image; the external picker interface remains untested.
- **Actual 3.6.0→3.7.0 installation with the retained certificate passed.** Every old fixture row/column/count survived, both children retained their records, Room advanced 12→13 with original Today defaults, and DataStore stayed byte-identical. The updated app rendered the selected teen and Grandparent mode offline. Software-emulator startup command timing exceeded its wait budget; no physical-device latency claim is made.
- Sixteen inspected native screenshots cover normal/150% text and both themes. Debug APK signing/package, source ZIP CRC and artifact SHA-256 are checked before publication.

Local real Chromium 153 and WebKit 26.6 passed five viewport sizes and two no-JavaScript cases each. Chromium 200% text passed two viewports. Forward/reverse scrolling, reduced motion, sample demos, request drafts and the 11-screen gallery passed. Live Chromium passed the same five viewports and both no-JavaScript cases after Sites version 6 deployed. Hosted APK, checksums and both source parts were downloaded: SHA-256 matched, and the reassembled ZIP was byte-identical to the frozen source. GitHub workflow [37156726604](https://github.com/nothatcher-creator/Sproutbook/actions/runs/37156726604) passed; all three published release assets were downloaded and matched their frozen bytes and GitHub digests. The downloaded APK retains the evaluation certificate; the source ZIP passed CRC/native-project/key-exclusion checks. [Native QA](https://github.com/nothatcher-creator/Sproutbook/blob/main/android/docs/QA-3.7.md) · [Continuation checkpoint](https://github.com/nothatcher-creator/Sproutbook/blob/main/docs/CONTINUATION.md).

## Remaining limitations

Evaluation pre-release. Existing limits remain: physical-device performance,
battery/audio quality, spoken TalkBack, Android 17 background/notification behavior
and minified-release runtime are untested for this update. Software-emulator
results and successful R8 builds do not establish those results. Production
signing requires the original private key.

Previously selected and unused draft background pictures currently remain in
private app storage; backups include only pictures referenced by saved records.
Removing a background from the layout does not promise immediate file erasure.
If photo work finishes after a child/mode change or editor disposal, the app
reports that the photo was not applied and should be chosen again. Saved layouts
remain unchanged until Save.

Parenting and health guidance remains general education and does not replace a
care team or emergency services. Cloud sharing, partner accounts, widgets and iOS
remain future work.

The source ZIP is an immutable snapshot of the verified source checkpoint; its
QA documentation records the state before publication. This online ledger
records the final publication checks. The ZIP is not regenerated for delivery
metadata edits.
