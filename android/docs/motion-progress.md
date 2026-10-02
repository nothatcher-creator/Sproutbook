# Woodland motion progress
Source copied to isolated sproutbook-native-v35; prior 3.4 deliverables preserved.
Asset contract RED: missing icon-today rejected; GREEN: all 184 compact exports validated against uploaded archive, dimensions/durations valid and no external image assets.
Native integration written: shared policy observers, lazy local Lottie with bounded PNG cache, native frame-rate quantization, viewport gate, selected tab loops, themed header accents, firefly overlay, finite action feedback and actual successful-save confirmation.
Ruling: Preserve the existing Reduce motion preference/default rather than forcing movement onto every family. The user can opt in from Profile/settings.
Ruling: Bundle all exports for both themes (~800 KB payload) but only animate visible selected accents; list rows stay static, and interactive Memory Tree remains unchanged.
Build recovery: official toolchain restored; stale GRADLE_OPTS proxy overridden in local build runner and proxy CA imported into local JDK truststore. These environment-only settings are not shipped in app source/configuration.
Verification and source/APK packaging pending.

Builds complete: debug + instrumentation, R8 unsigned release APK and AAB, vital release lint. Core78 and app1 unit tests passed. Debug signing certificate matches3.4; manifest has no INTERNET permission. Android MotionAssetTest passed: all92 JSONs rendered and all92 PNGs decoded in67.571s on API29 software emulator. UI/runtime checks in progress. Reviewer important accessibility finding fixed; minor cosmetic replay behavior deferred/documented in QA notes.

Android final motion suite:2/2 passed after test-clock navigation transition fix. Regression8/8 and large-text1/1 passed; total focused Android12/12, no crash-buffer entries. Native dark Today/Care and light large feeding editor visually inspected. Release/debug rebuilt successfully with DejaVu licence bundled; only packaging changed after runtime tests. Final APK SHA256104cb6e01565f601314326931f08e8a4096a1f8f6b54a0df896fa36a27c47713.

Final: asset verification unchanged, debug signature correct, both-theme screenshots inspected, motion12/12 Android and79/79 unit checks green. Idle sample results recorded in QA-3.5.md; hardware battery/smoothness left for device QA. No data migration or interaction logic changed.
