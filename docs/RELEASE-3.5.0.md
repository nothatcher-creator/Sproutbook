# SproutBook 3.5.0 · Woodland in motion

A native Android parenting app, with a softer woodland interface and the supplied animated asset pack integrated throughout care, navigation, and feedback.

[Download APK](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.5.0/SproutBook-3.5.0-debug.apk) · [Source project](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.5.0/SproutBook-3.5.0-source.zip) · [Interactive showcase](https://sproutbook-woodland.nothatch.chatgpt.site)

## What's new

- 46 woodland motion assets in both light and dark themes, with matching static fallbacks. Selected navigation icons, banner details, button responses, and save feedback use native Lottie Compose.
- Motion follows Reduce motion, the Android animator setting, lifecycle, battery saver, focus, keyboard visibility, and viewport eligibility. Assets are bundled locally; no Internet permission was added.
- Painted woodland scenes, the native tooth tracker, sound machine, expanded parent advice, and all existing care tools remain connected to each child's profile.
- Light-theme logging at 150% font size was verified on the emulator. Existing development-signing identity is preserved for updates from the 3.4 debug APK.
- The showcase website now has a scroll-controlled woodland journey. Scrolling forward advances the scene; scrolling back reverses it. Motion can be turned off, and the website follows the device's reduced-motion preference.

## Downloads and identity

| Item | Detail |
| --- | --- |
| Version | 3.5.0 / 30500 |
| Package | `com.nothatcher.sproutbook` |
| Android | 8.0+; target SDK 37 |
| APK | Debug-signed evaluation build, 14.1 MiB |
| Source ZIP | Kotlin/Compose project, original editable artwork, motion assets, QA evidence, unsigned release APK/AAB, and R8 mapping |
| APK SHA-256 | `104cb6e01565f601314326931f08e8a4096a1f8f6b54a0df896fa36a27c47713` |
| Source SHA-256 | `fca20d9542c2019aad613541faea662ea4ca308ef8cd0346d69d1c114a38d9d2` |

Export a backup before updating. Keep the existing app installed and install this APK over it. See [installation instructions](INSTALL.md).

## Verification and remaining work

The native update passed 79 unit tests and 12 focused Android checks on an API 29 software emulator. Checks covered every supplied asset, motion gating, the four tabs, calendar/tooth/memory gestures, native sound service controls, multiple children, caregiver protections, and light-theme feeding save at 150% font size. Debug, R8 release APK, release AAB, and release vital lint completed. Full evidence is inside the source ZIP's `docs/QA-3.5.md`.

This is an evaluation pre-release. Galaxy S25 hardware smoothness, battery use, spoken TalkBack output, and minified-release runtime still require device QA. Release outputs are unsigned and require a private production upload key. A finite button animation can replay after motion eligibility returns; this is cosmetic and never repeats its action.

Website checks cover static assets, links, versions, download hashes, and DOM-simulated interactions including scroll reversal and reduced-motion behavior. Rendered-browser/mobile visual QA was unavailable in the static-site preview environment; no browser frame-rate claim is made.

## Data and support

Room schema 11 and backup format 3 are unchanged. Existing Reduce motion preferences stay intact; it is enabled by default for new app installations. Family records remain local. Grandparent mode prevents accidental edits and is not authentication. Backups contain readable personal information: keep them private.

[Report a problem or suggest a feature](https://github.com/nothatcher-creator/Sproutbook/issues). Use sample data rather than family records. Parenting guidance is general education; contact emergency services in an emergency.
