# Website verification

2026-10-02: actual rendered Chromium 153.0.8010.12 with Playwright 1.63.0,
using desktop 1440x900 and mobile-layout viewports 390x844, 360x640 and 667x375.
Evidence and selected screenshots: `../docs/evidence/browser-2026-10-02/`.
Repeat with `tools/verify-browser.cjs`; install Playwright and its Chromium engine.

The published 3.5.0 baseline failed a real pointer click on every upper tooth.
The full-size lower arch intercepted the upper arch's controls. Containers now
ignore pointer events while their tooth buttons retain them. The same browser
flow passed on the local Sites-source preview after the CSS fix.

Four final viewport runs passed again with the 3.5.1 download metadata, native
health-picker screenshot, and 3.5.1 feature-request context (`v351-browser.log`
and `results.json`):

- APK download visible at the top; no horizontal page overflow.
- Scroll-controlled SVG timelines advance and reverse to identical rendered
  frames; all three chapters are readable; normal wheel scrolling remains intact.
- SVG clocks stay paused while idle; the short-height layout stacks chapters.
- Motion-off persists after reload. A live reduced-motion preference switches to
  the still, stacked layout and disables the override control.
- Sample memory add/reset, feeding save/reset, all twenty tooth taps, long press,
  movement cancellation, tap relocking, explicit unlock/stage change/reset, and
  life-stage selection work through rendered controls.
- Feature requests produce meaningful feedback and a correctly encoded GitHub
  draft. Popup navigation was intercepted in the test; no request was submitted.
- No JavaScript page errors were observed.

Static asset/ID/link/version/checksum checks and the existing DOM-simulated
regressions also passed in the recovered Sites source checkout. Those are separate
checks and are not described as browser tests. WebMCP registry tests are simulated;
no supported real WebMCP browser claim is made.

Limits: these are actual headless Chromium renders on Linux, with viewport
emulation. No physical-phone browser, Safari/Firefox, screen reader speech,
hardware frame-rate, or battery claim is made. Website demos use temporary sample
data separate from family Android records. Native app QA has its own ledger.

The existing public Sites update succeeded as saved version 3 on 2026-10-02 at
21:58:53 UTC. Source commit: `5ee5c4e2bfb31b1972abd554142b4dd9b8b652b5`.
The same four viewport cases then passed against the actual live website:
`live-v351-browser.log`, `live-v351-results.json` and `live-downloads-v351.png`.
The published APK/source GitHub asset digests match the verified files; the release
workflow completed successfully. Deployment IDs and exact release details are in
`../docs/CONTINUATION.md`. GitHub's `website/` folder is a source mirror; publication
used the existing Sites integration.
