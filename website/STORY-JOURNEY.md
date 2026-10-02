# SproutBook woodland journey

2026-10-02. Website update; Android remains the verified 3.5.1 evaluation release.

## Experience

Native page scrolling moves from a pale morning sky into the original painted
forest, then the memory oak, appointment noticeboard, feeding picnic, evening
nursery, and growing sapling. The existing temporary demos, stage selector,
screenshots, and GitHub request draft remain in the clearing. The final download
arrives at a nighttime tree with Pregnancy / Newborn / Baby / Toddler / Child / Teen.

Four named depth layers use different scroll rates. Phone screenshots belong to
their scenes, with gentle movement and at most two degrees of rotation. Scrolling
back restores the same camera, animal, plant, leaf, card, and supplied SVG pose.
Copy stays in normal document flow; there is no scroll interception or text fade.

## Artwork and assets

The existing native paintings, sprout/book and butterfly vector timelines, grass,
leaves, night scene, and feature icons are retained. Three matching original
image-generation assets add sunrise scenery, a transparent 2-by-4 prop atlas,
and a transparent 4-by-2 animal atlas. The optimized assets are in `assets/`:

- `story-sunrise.webp`: 1536×1024, 287,704 bytes.
- `story-props.webp`: 887×1774, 563,228 bytes, genuine alpha.
- `story-animals.webp`: 1774×887, 427,164 bytes, genuine alpha.

Atlases use CSS background positions; they require no per-animal downloads.
Original artwork was not replaced. Screenshots are existing synthetic native QA
fixtures: memory/calendar from the recovered source, feeding from 3.5.1 QA,
sound from 3.3, growth from 3.1. They are labeled as actual Android sample records;
older screenshots do not claim new native verification.

## Motion and performance contract

- One coalesced requestAnimationFrame handles scroll events, with cached geometry.
  There is no idle JavaScript animation loop. Resize, fonts and changes to the
  main content's height refresh geometry, including the final CTA.
- Only nearby chapter poses update. Supplied SVG clocks are paused and scrubbed.
- Calm clouds and particles animate only while the forest is visible. Arrival
  fireflies animate only while the final section is visible. Hiding the document
  stops both groups and cancels a pending JavaScript frame.
- Stored motion-off and live `prefers-reduced-motion` use a complete static
  layout with all content, artwork, downloads and demos. Reduced motion cannot be
  overridden by the site toggle. JavaScript-disabled pages start in that same
  static layout; SVG timelines have `begin="indefinite"`, and the toggle is disabled.
- Mobile retains the scroll journey. Short landscape screens use taller normal
  document chapters rather than forcing content into a pinned short viewport.

No GSAP, Lottie runtime, Rive runtime, or new product dependency is required for
this deterministic geometry and the existing vector pack. Hardware frame rate,
GPU cost and physical Android/iPhone battery behavior need device measurements.

## Verification and continuation

Use `tools/verify-journey.cjs` for actual Chromium/WebKit rendering and preserved
sample regressions. `tools/verify-large-text.cjs` checks 200% text. Reports and
selected screenshots are recorded in `docs/evidence/woodland-journey-2026-10-02/`.
See `QA.md` and the root `docs/CONTINUATION.md` for exact results and publication.
The earlier `tools/verify-browser.cjs` describes the historical three-chapter
panel and should not be used as a verifier of this new journey.
