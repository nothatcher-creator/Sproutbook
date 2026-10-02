# SproutBook 3.4.0 — woodland update

Native Kotlin/Compose. Package com.nothatcher.sproutbook, version code 30400,
min SDK 26, target SDK 37. Room schema 11 and backup format 3 remain unchanged.

## Delivered visuals

Five original gouache-style woodland paintings: offline background and Today,
Schedule, Care and More banners. Thirty-three custom SVG/Android vector icons
cover navigation, feature rows and shared action buttons. Softer rounded cards,
forest/moss/cream palette, scalable serif headings, matching native picker/action
controls and dark/light themes. Native text remains searchable and accessible.
Original paintings, generation prompts and editable SVG geometry are bundled.

Reduce motion defaults on. Turn it off for six gentle edge fireflies. The draw
layer ticks at most 12.5 Hz; the rest of the page does not read its clock. Motion
pauses for lifecycle, unfocused windows/dialogs, battery saver, disabled system
animations, keyboard and detail routes. There is no animated image decoding.

## Figma

https://www.figma.com/design/SjQl6MaLL4e5YqK10grN2c
Editable tokens, typography, components and four tab designs. Starter API quota
blocks additional Figma edits/exports. Native paintings were regenerated in this
chat after workspace recovery, preserving the same art direction; the Figma
paintings and icon paths are earlier variants, not an exact current raster copy.

## Fresh verification

All build tasks passed: core:test, app:testDebugUnitTest, app:assembleDebug,
app:assembleDebugAndroidTest, app:assembleRelease and app:bundleRelease,
including R8 and release vital lint. Seventy-eight core tests and one app unit
test passed, with no failures, errors or skips. Debug package/version/signature
were checked; it retains the existing development certificate. Unsigned release
APK/AAB and matching R8 map are included; distribution requires owner signing.

Conservative pixel analysis of the weakest banner scrim yields minimum subtitle
contrast 4.83–5.30:1 across all four paintings. This is raster/background analysis,
not measurement of anti-aliased glyph edges. An independent read-only review
checked the implementation and the contrast/focus corrections; no further
blocking findings remained. Its inspection is separate from executed tests.

DEVICE_VALIDATION_PENDING

Historical v33 evidence is retained as history and is not claimed as a fresh
v34 run. New logs, screenshots and measured results appear in evidence/v34.
