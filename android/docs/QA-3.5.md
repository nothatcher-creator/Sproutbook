# SproutBook 3.5.0 verification

## Build and identity
- Kotlin/Compose debug APK, instrumentation APK, R8 release APK and release Android App Bundle compiled on JDK17, Gradle9.3.1, Android SDK37.0 / build tools36.0.0.
- Core unit tests: 78 passed, zero failures/errors. App unit tests: 1 passed, zero failures/errors.
- Release vital lint passed during bundle build. Release outputs are unsigned; use a private upload key before production distribution.
- Package com.nothatcher.sproutbook; versionName3.5.0; versionCode30500; minSDK26 / targetSDK37.
- Debug certificate SHA256: 9c97e149698a89e51ef69e5a6af1649b1b55004f72f8cf57fd7993c5290ec0f3 (same identity as 3.4 debug).
- No INTERNET permission; the new assets are local only. Room schema11 / backup format3 unchanged.

## Asset contract
- 46 IDs x 2 themes; 92 Lottie JSON + 92 PNG fallbacks, byte-identical to uploaded pack.
- Manifest dimensions/durations and 24fps verified; no external image assets.
- Verifier first rejected a deliberately missing active-tab asset, then passed the complete package.
- Android native asset test passed on API29: all92 Lottie compositions parsed and rendered nonempty pixels over four progress samples; all92 matching PNGs decoded. Runtime: 67.571s on the software emulator. Native UI motion suite passed on rerun, as listed below.

## Review
An independent static review found no Critical issues and one Important accessibility issue: decorative assets used spoken stateDescription. Fixed by a custom test semantics key, hiding purely decorative assets while preserving explicit Back/Switch child descriptions. Native accessibility-state assertion included.

Minor deferred: a button's finite press response can replay after motion eligibility is restored (for example, returning focus) while that button remains composed. This is cosmetic, never repeats the button action or alters records.

## Limits
This is a development APK for evaluation. Runtime checks use an API29 software emulator, not a Galaxy S25. Hardware smoothness, battery consumption, TalkBack audio output and the minified release's runtime behavior require device QA. This focused graphics update does not re-run every historical app test; exact current test results will be listed below after execution.

## Confirmed Android regression checks (API29, 360x800, software rendering)
- GestureTest: 5 passed (calendar long press/date prefill; tooth normal tap/long press; Memory Tree arrange and drag snapping; scrolling across teeth without accidental changes).
- Native sound play/pause/resume/switch/stop: passed against the actual audio service.
- Multiple-child context and read-only caregiver guard: passed.
- Four painted tabs, labels and native navigation: passed.
- Light theme at150% text, native feeding editor and saved80mL record: passed, not skipped.
- Initial motion UI run: native95mL feeding save passed; controlled-clock navigation test failed because unfinished transitions exposed three lazy lists. This was a test harness error. Added explicit advancement of finite navigation transitions; the corrected rerun passed both tests (90.739s).

## Final motion UI run
Both tests passed: selected Today/Schedule/Care/More tab loops; static light-theme fallback; no spoken decorative stateDescription; offscreen Care banner stops and resumes after scrolling back; live system animator-scale changes stop/resume motion; Reduce motion stops it; a native95mL feeding record saves. The controlled clock was advanced through finite native navigation transitions, not disabled in the application. All12 focused Android checks passed across the final runs (1 asset +2 motion +8 regressions +1 large text). Android crash buffers were empty.

The DejaVu font notice is bundled in both APK assets and source. The post-QA rebuild changes asset licence packaging only; runtime code is unchanged.

## Idle frame samples
One8-second sample per mode on the API29 software emulator, debug build, Today screen:

| Mode | App frames |
|---|---:|
| motion-on-idle | 133 |
| system-motion-off-idle | 0 |
| battery-saver-idle | 5 |
| reduce-motion-idle | 0 |

Battery saver was confirmed enabled in dumpsys power. These samples show motion gating, not phone smoothness. The unaccelerated emulator marked rendered frames janky; no Galaxy S25 frame-rate or battery claim is made. Standalone UI hierarchy queries briefly returned null roots after instrumentation and were retried until valid app trees were available.
