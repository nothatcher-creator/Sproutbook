# SproutBook 3.3.0 · validation ledger

Verified 1 October 2026. Native Kotlin/Compose; package com.nothatcher.sproutbook,
version code 30300, min SDK 26, target SDK 37. Room schema 11 and backup format 3
are unchanged. The complete offline data model and earlier features are retained.

## Delivered care update

- Twenty tooth-shaped baby teeth in upper/lower arches. Tap views; long press
  opens a native stage/date editor. Eruption dates and stages persist per child.
  Status symbols and totals supplement color. Read-only mode omits mutation actions.
- Eight native AudioTrack loops: White, Brown, Fan, Rain, Ocean, Hush, Stream,
  Breeze. Play, pause, resume, stop, volume, 15/30/60/90-minute or continuous timers.
  Pausing freezes the monotonic countdown; paused switching does not take audio focus.
  Pending sound choices survive switching between sleep/sound tabs.
- Forty-two expandable and searchable articles across Mom (24) and Dad (18), with
  primary-source links for medical education. Audience switching clears old filters.
- Cached native woodland illustrations, shaped teeth and sound glyphs. No idle
  animation, web surface, remote asset dependency or new database storage format.

Higgsfield: a private visual project was created, but image generation was rejected
with “Requires basic plan or higher.” No image was generated or incorporated.
All shipped artwork is native Compose drawing.

## Executed verification

The final production build passed :core:test, :app:testDebugUnitTest,
:app:assembleDebug, :app:assembleDebugAndroidTest, :app:assembleRelease and
:app:bundleRelease, including R8 and release vital lint. Seventy-five core tests
and one app unit test passed with no failures, errors or skips. Debug APK signature,
version and package were checked. The project includes unsigned release APK/AAB
and the matching R8 map; public distribution requires the owner's release identity.

Twelve targeted emulator tests passed: native tooth gestures and scroll safety,
calendar date retention, tree snapping/dragging, all eight AudioTrack play/stop
paths, pause/resume/switching, other audio focus ownership, sound tab persistence,
advice searching/expansion, tooth date recreation and read-only permissions.
Thirteen broader regression checks passed across the clean batch and a targeted
navigation rerun: 25 unique completed emulator tests, zero remaining failures.
This is the union of completed batches, not one uninterrupted full-suite run.
The test-to-log mapping is in evidence/v33/v33-final-summary.json.

The density check exercised four children, 500 feeds, 500 memories and 300
appointments, with bounded 32-leaf tree chapters. It is a functional data-density
check, not a frame-time or Galaxy S25 performance benchmark. Backup validation,
photo round trip, child separation, offline clipboard, health CRUD, feeding CRUD
and milestone-to-memory integration were included in the executed regression batch.

## Review and QA corrections

One independent read-only review found two important and three minor issues,
all addressed with targeted checks. See v33-review.md. Red-to-green evidence
covers new sound support, advice catalog, tooth hitbox separation, audio focus
interruption and lost sound selections.

An AOSP System UI ANR overlay on the software emulator initially stole focus,
blocking hardware input and clipboard. It was dismissed; QA focus guards prevent
capturing behind system windows. No application crash was present in the clean
crash buffer. Clean screenshots were visually inspected. A later QA-only fix
scrolls Care to its heading before asserting it, because navigation correctly
restores that screen's lazy-list scroll position. Original failing logs remain.

## Limits

API 29 software emulator audio runs with host output disabled: tests verify native
AudioTrack state and UI/service integration, not physical speaker quality. Timer
expiry math is unit tested; real fifteen-to-ninety-minute sessions were not waited
out. No claim of physical screen-off longevity, battery performance, Galaxy S25
smoothness, full TalkBack/large-font coverage or Play approval is made. Remaining
hardware, editorial, privacy and release-signing gates are in QA.md.
