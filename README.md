<p align="center">
  <a href="https://github.com/nothatcher-creator/Sproutbook/releases/download/V1.0/SproutBook-3.4.0-debug.apk">
    <img src="docs/assets/download-android.svg" width="360" alt="Download SproutBook 3.4.0 for Android" />
  </a>
</p>
<p align="center">
  <strong>Android 8.0+ · Version 3.4.0 · 12.5 MiB · Preview build</strong><br />
  <a href="https://raw.githubusercontent.com/nothatcher-creator/Sproutbook/main/SproutBook-3.4.0-debug.apk">Direct APK download</a> ·
  <a href="https://github.com/nothatcher-creator/Sproutbook/releases/tag/V1.0">Release notes</a> ·
  <a href="docs/INSTALL.md">Installation guide</a>
</p>

![SproutBook — a little calmer, a little more connected; a soft grassy woodland illustration](docs/assets/woodland-hero.jpg)

<p align="center">
  <img src="https://img.shields.io/badge/Native_Android-Kotlin_%26_Compose-294C3B?style=flat-square&labelColor=152F25" alt="Native Android, Kotlin and Compose" />
  <img src="https://img.shields.io/badge/Family_records-Offline-78915B?style=flat-square&labelColor=152F25" alt="Family records work offline" />
  <img src="https://img.shields.io/badge/Theme-Woodland-D1B878?style=flat-square&labelColor=152F25" alt="Woodland theme" />
</p>

## A calmer place for your growing family

SproutBook brings the little details of parenting together: the last feed, the next appointment, a new tooth, a favourite meal, and a memory you want to keep. Its native Android interface uses soft woodland paintings, forest greens, warm cream, and simple controls built for busy hands.

From pregnancy through the teen years, each child has their own profile and records. Four main tabs keep everyday care close: **Today, Schedule, Care, and More**.

![A soft grassy woodland divider](docs/assets/meadow-divider.svg)

## Four familiar places

<table>
  <tr>
    <td width="50%"><img src="docs/assets/today-art.jpg" alt="Today artwork: a quiet woodland meadow" /><br /><strong>Today · Your day at a glance</strong><br />Stage-aware shortcuts, recent care, upcoming appointments, low supplies, and your child's memory tree.</td>
    <td width="50%"><img src="docs/assets/schedule-art.jpg" alt="Schedule artwork: a gentle woodland path" /><br /><strong>Schedule · Room for what matters</strong><br />One connected calendar for appointments, birthdays, due dates, and family plans. Hold a day to start an appointment.</td>
  </tr>
  <tr>
    <td><img src="docs/assets/care-art.jpg" alt="Care artwork: a sheltered woodland clearing" /><br /><strong>Care · The everyday essentials</strong><br />Feeding, sleep, sounds, solids, teeth, development, pregnancy tools, and practical parent guidance.</td>
    <td><img src="docs/assets/more-art.jpg" alt="More artwork: warm woodland shelter" /><br /><strong>More · Your family, connected</strong><br />Profiles, supplies, emergency information, caregiver mode, settings, backups, and diagnostics.</td>
  </tr>
</table>

*These are the original tab illustrations bundled in the 3.4 woodland update.*

## Little tools that make a difference

| Caring for today | Keeping the bigger picture |
| --- | --- |
| **Feeding hub** — bottles, breastfeeding, pumping, prepared bottles, and a formula planning estimate. | **Memory tree** — leaves for moments, branch placement, chapters, and a timeline for each child. |
| **Sleep + sound** — sleep sessions and eight native sounds, with pause, resume, volume, and timers. | **Schedule** — appointments, annual birthdays, due dates, and optional reminders. |
| **Solids + meals** — foods tried, favourites, possible reactions, allergens, and weekly meal planning. | **Development** — visual baby teeth, stage-specific milestones, and recorded growth. |
| **Parent support** — searchable Mom/Dad advice and three calm first steps in Help Right Now. | **Pregnancy** — kick sessions, contraction timing, preparation, and appointment questions. |
| **Everyday routines** — diaper and potty logs, milk freezer containers, responsibilities, and shopping. | **Family essentials** — supplies, health journal, an offline emergency card, and read-only grandparent mode. |

### A closer look at Care

<p align="center">
  <img src="docs/assets/teeth-screen-v33.png" width="230" alt="Real SproutBook 3.3 screenshot of the visual baby tooth tracker" />
  <img src="docs/assets/sound-screen-v33.png" width="230" alt="Real SproutBook 3.3 screenshot of the native sound machine" />
  <img src="docs/assets/advice-mom-screen-v33.png" width="230" alt="Real SproutBook 3.3 screenshot of searchable Mom Advice" />
</p>

*Actual emulator screenshots from the 3.3 care update, using synthetic family data. These features remain in 3.4; the screenshots predate the new painted headers and icons.*

![A soft grassy woodland divider](docs/assets/meadow-divider.svg)

## Install in a few steps

1. Tap **Download SproutBook** at the top of this page.
2. Open the APK on your Android phone. If prompted, allow installation from the browser or Files app you used.
3. Install, open SproutBook, and add your child's profile.

For updates, keep your existing app installed and use the same signing identity. Export a backup before updating. See the [installation guide](docs/INSTALL.md) for details and the [SHA-256 checksum](SproutBook-3.4.0-debug.apk.sha256) to verify the download.

## The woodland update

**3.4.0** adds five original painted scenes, 33 custom native icons, softer cards, and a coordinated forest-and-cream visual system. Dark and light themes retain native Android controls and readable labels. The artwork is bundled with the app and works offline.

The background is still by default. Optional gentle fireflies are available by turning off **Reduce motion** in settings. This preview has a known edge case when Android's global animation setting changes while motion is active; leave Reduce motion enabled for a static background.

This is an installable **debug-signed preview**, published as a GitHub pre-release. Final large-text verification, the animation-setting fix, real-device checks, and production signing remain before a general release. See [the full release notes](docs/RELEASE-3.4.0.md).

## Your family's records stay close

Core records are stored locally using Room and DataStore. The app has no account requirement, cloud sync, or Internet permission. Optional reminders use Android notifications; sound uses native Android audio. Backups are exported through the file picker and should be kept private.

[Read the data and privacy notes](docs/PRIVACY.md). Grandparent mode helps prevent accidental edits; it is not a password-protected account or security boundary.

Parenting and health guidance is general education. For an emergency, contact emergency services. SproutBook does not diagnose conditions or replace your family's care team.

## Follow along

[Releases](https://github.com/nothatcher-creator/Sproutbook/releases) · [Report a problem or suggest an idea](https://github.com/nothatcher-creator/Sproutbook/issues) · [Figma design reference](https://www.figma.com/design/SjQl6MaLL4e5YqK10grN2c)

This repository is the APK release home; it does not currently contain the Kotlin source project. The Figma reference includes the visual system and earlier artwork variants. Cloud sharing, partner accounts, widgets, and iOS are future work.

<p align="center"><em>A small place to remember, plan, and grow together.</em></p>
