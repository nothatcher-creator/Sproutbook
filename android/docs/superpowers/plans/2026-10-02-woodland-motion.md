# Woodland Motion Implementation Plan

> **For agentic workers:** Use executing-plans to implement this plan inline, task by task.

**Goal:** Add the supplied animations to the existing native app without changing data or gestures.
**Architecture:** One lifecycle/accessibility/power policy provider; one lazy native Lottie/PNG renderer. Existing controls keep their actions and semantics.
**Tech Stack:** Kotlin, Compose, Lottie Compose 6.7.1, Room/DataStore.
**Spec:** docs/motion-update-spec.md

## Global Constraints
Package com.nothatcher.sproutbook; version 3.5.0; preserve data schema and existing Reduce motion setting; no WebView; offline assets only.

## Review Focus
- System animation scale changed while app is open must stop drawing motion.
- Missing/failed animation must retain a visible static graphic.
- Scrolled-out banners must stop playing.
- Button feedback must not claim a failed save succeeded.
- Contrast and native semantics must remain usable in both themes and at large fonts.

### Task 1: Asset contract
Files: app/src/main/assets/sproutbook-motion; tools/verify_motion_assets.py; docs/motion-pack.
- [x] Write a verifier comparing every manifest ID and theme to Lottie and PNG files, valid dimensions/durations, nonempty vector layers and checksums against uploaded originals.
- [x] Confirm it rejects a missing asset, then run successfully on the packaged originals.
- [x] Add Lottie dependency and version; compile core tests before UI integration.

### Task 2: Native integration
Files: ui/WoodlandMotionPolicy.kt; ui/WoodlandMotionAsset.kt; ui/WoodlandBackground.kt; ui/WoodlandGraphics.kt; ui/Components.kt; navigation/AppRoot.kt.
Interfaces: LocalWoodlandMotionEnabled<Boolean>; WoodlandMotionAsset(id, modifier, play, loop, replay, dark, tint, description).
- [x] Share existing WoodlandMotion.shouldAnimate gates across decorators.
- [x] Implement lazy composition loading with PNG fallback, draw-time progress reads, native frame rate and viewport checks.
- [x] Wire selected nav icons, banner accents, button responses and successful-save snackbars; keep lists static and painted backgrounds.
- [x] Compile debug, instrumentation and unsigned release; run core/app unit tests.

### Task 3: Android validation and delivery
Files: androidTest/MotionAssetTest.kt; androidTest/WoodlandMotionUiTest.kt; docs/QA-3.5.md.
- [x] Parse/render all 92 Lottie files on Android and check both PNG fallbacks.
- [x] Exercise native tab navigation and feeding save with motion enabled; verify Reduce motion and system-scale gates, then existing gesture/sound/read-only regression tests.
- [x] Capture native screenshots and inspect. Test 150% text if emulator permits.
- [x] Review final changes, package APK/source and save deliverables with exact verification limits.
