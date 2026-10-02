# SproutBook Woodland Motion · 1.0

**46 animated assets, each in light and dark themes.** Matched to SproutBook 3.4.0’s current palette and its 33 outline icons. Includes 13 additional brand, memory, feedback, and woodland assets. All animation backgrounds are transparent.

Open **preview.html** to search the collection, change themes, pause motion, and replay an action. The gallery is self-contained and works offline after download. The contact sheet is a quick still overview.

## Choose an export

| Folder | Contents | Suggested use |
| --- | --- | --- |
| `lottie/light`, `lottie/dark` | 92 vector animation JSON files | Native Android / Compose; scalable geometry and small files |
| `webp/light`, `webp/dark` | 92 lossless animated WebP files | Transparent raster playback in a compatible image loader |
| `svg-animated/light`, `svg-animated/dark` | 92 animated SVG files using SMIL | Web pages; embed as an image |
| `gif/light`, `gif/dark` | 92 animated GIF previews | Quick sharing; lower resolution and binary transparency |
| `sprites/light`, `sprites/dark` | 92 PNG sprite sheets plus matching frame metadata | Canvas, game-style renderers, or a custom animation player |
| `png/light`, `png/dark` | 92 still PNG fallbacks | Reduced motion, paused state, thumbnails |
| `svg/light`, `svg/dark` | 92 still SVG fallbacks | Scalable reduced-motion and design assets |
| `source` | Editable vectors, motion keyframes, original icons, and build scripts | Recolouring, editing, and regeneration |

Both themes contain the same IDs. Theme selection is explicit, so load the variant matching the app’s surface. The collection has no downloaded image, font, or network dependencies at playback time. The SproutBook lettering is outlined vector geometry.

## Style and placement

The dark variant uses the app’s `#B6D59C` primary; the light variant uses `#365B35`. Supporting colours include moss, soft gold, rose, and sky. Rounded strokes preserve the existing icon family. The woodland illustrations use simple filled silhouettes that sit alongside the app’s detailed woodland paintings.

Icons use a 64 × 64 vector canvas with a centred 48 × 48 motif and matching padding. PNG / WebP icons are exported at 192 × 192 px. Most illustration accents also use this canvas. Banners use 320 × 96, 320 × 112, or 320 × 128 vector units and 2× raster exports. The wordmark is 240 × 72 vector units.

Use care icons at about 24–32 dp and the illustrations at about 64–128 dp. The Memory Tree illustration is a decorative header/empty state; individual memory leaves and their interactions stay in the app’s existing tree system. The animated assets are visual resources, not replacements for feature logic.

The fireflies and meadow border are transparent overlays. Apply about 20–35% overall opacity to fireflies over a painting. The dawn and night banners have a transparent sky; the hills themselves are opaque. The pack does not replace or animate the full woodland paintings.

## Motion rules

`manifest.json` records each asset’s duration, canvas, intended placement, and `mode`:

- `loop`: subtle breathing, swaying, drifting, or twinkling. Loops range from 2.5 to 8 seconds and return to the starting pose.
- `once`: button press accents and saved confirmation. Play once and hold the final frame. Restart on the next explicit action.

Animate only the active care/navigation icon or the visible header illustration. Pause decorative loops when a screen is not visible. For reduced motion, display the matching `png` or still `svg` file. The gallery automatically starts paused when the browser reports a reduced-motion preference.

GIF previews run at approximately 12 fps. Lottie, WebP, and sprite metadata use 24 fps. WebP frame intervals alternate integer milliseconds to preserve the full clip duration. GIF previews use coarser timing and a binary alpha edge; use Lottie or WebP in the app.

## Android handoff

1. Copy the selected `lottie/light` and `lottie/dark` directories under `app/src/main/assets/sproutbook-motion/`.
2. Use the project’s compatible Lottie / Lottie Compose dependency and load the exact path, for example `sproutbook-motion/dark/memory-tree.json`.
3. Read `mode` in the manifest: repeat loop assets; play action assets once. Set the visual size in Compose and keep existing click handling, labels, and semantics on the parent control.
4. Copy the corresponding PNG fallbacks into the app and display them when motion is disabled.

These animations use only vector shape layers, fills, strokes, opacity, scale, rotation, and position. There are no expressions, masks, external images, text layers, or audio. Standard Lottie playback is the recommended native route. Animated SVG is intended for web playback, rather than Android `VectorDrawable` loading. Sprite sheets require a player; the JSON sidecar supplies frame rectangles and durations.

Official Android player reference: https://github.com/airbnb/lottie-android

The pack has been checked as an asset export. It has not been installed into a SproutBook APK or tested in an Android Lottie runtime.

## Edit and rebuild

The editable asset definitions are in `source/build_pack.py`; the resolved vectors and motion keyframes are in `source/model.json`. Shape parts have individual pivots, so leaves, stars, clouds, and branches can move independently.

Build prerequisites: Python with Pillow and fontTools; Node with `@napi-rs/canvas`. The wordmark build uses DejaVu Serif from the installed font path (the generated assets themselves need no font). The font’s supplied licence is in `source/DejaVu-license.txt`. Original SproutBook icons are preserved in `source/original-icons` for reproducibility.

From the pack directory:

```sh
python3 source/build_pack.py
node source/render_frames.cjs /absolute/path/to/temporary-frames
SPROUTBOOK_FRAMES=/absolute/path/to/temporary-frames python3 source/finish_pack.py
node source/verify_lottie.cjs /absolute/path/to/temporary-frames
```

Use `CODEX_PRIMARY_RUNTIME_NODE_MODULES` when running inside the managed build environment, or install `@napi-rs/canvas` locally. Set `SPROUTBOOK_FONT` to an alternative DejaVu Serif TTF path if needed. Temporary frame files are not included in the pack.

## Asset catalogue

See **ASSETS.md** for every ID and suggested placement. Each asset ID maps directly to its filenames in every export format. A checksum list accompanies the package.
