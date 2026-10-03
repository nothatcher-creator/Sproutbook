# SproutBook 3.7.0 local rendered-browser verification

Verified on 2026-10-03 against `http://127.0.0.1:8765/`, serving the existing Sites checkout's `dist/` directory. These are actual headless browser renders and interactions, separate from simulated DOM checks and native Android tests.

| Check | Result |
| --- | --- |
| Chromium 153.0.8010.12 journey | PASS: 1440×900, 390×844, 360×640, 320×700, 667×375 |
| WebKit 26.6 journey | PASS: the same five viewports |
| JavaScript disabled | PASS: both engines at 390×844, normal and reduced-motion preferences (four cases) |
| 200% text | PASS: Chromium at 390×844 and 1440×900, reduced motion |
| Native screenshot gallery | PASS: Chromium and WebKit at 1440×900 and 390×844 (four cases) |

Both journey engines passed forward/backward restoration of the scene, SVG frames, plant growth, phone placement, cards and animal poses; distinct depth-layer movement; readable chapters; normal wheel scrolling; no idle JavaScript frame loop; offscreen ambient-animation suspension; persisted Motion off; and live reduced-motion behavior. Sample memory, feeding, tooth tap/hold/movement-cancellation and stage controls worked. The feature-request control produced a reviewable GitHub draft URL containing the 3.7.0 context; no request was submitted. Header and final APK links point to 3.7.0. No page errors, same-origin HTTP failures or horizontal page overflow were observed.

The gallery's eleven native screenshots all decoded at 360×800. Desktop cards stay 220 px wide and wrap into three rows. Mobile cards stay 200 px wide in the existing horizontal gallery, with the last screenshot reachable. Representative desktop/mobile gallery renders were inspected. At 200% text, the opening, Memory Tree, calendar, demos, feature requests and final download area remained visible without horizontal page overflow.

The story animation files are byte-identical to existing Sites commit `1f393520be147bcff40f718c459331a29e302115`:

- `journey.css`: SHA-256 `72f9c2780be998f59bae5e2ee47829588e2b2559021a38e17fea233864613d40`
- `motion.js`: SHA-256 `650ec3fcfd321e80b3ebdf3e543112187a00717d02d4bb20331cd78608d87d74`

Only the screenshot-gallery style gained desktop wrapping. The update preserves the existing scroll-controlled woodland story.

Reports: [Chromium](local-chromium-results.json), [WebKit](local-webkit-results.json), [200% text](local-large-text-results.json), [gallery](local-gallery-results.json). Browser screenshots remain in ignored local `artifacts/browser-3.7.0/`; this evidence folder contains reports only.

Commands, run from `/workspace/sproutbook-showcase`:

```sh
NODE_PATH=/workspace/scratch/sproutbook-browser/node_modules node tools/verify-journey.cjs http://127.0.0.1:8765/ /workspace/Sproutbook/artifacts/browser-3.7.0/chromium 3.7.0 chromium
PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS=1 NODE_PATH=/workspace/scratch/sproutbook-browser/node_modules node tools/verify-journey.cjs http://127.0.0.1:8765/ /workspace/Sproutbook/artifacts/browser-3.7.0/webkit 3.7.0 webkit
NODE_PATH=/workspace/scratch/sproutbook-browser/node_modules node tools/verify-large-text.cjs http://127.0.0.1:8765/ /workspace/Sproutbook/artifacts/browser-3.7.0/large
PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS=1 NODE_PATH=/workspace/scratch/sproutbook-browser/node_modules node /workspace/scratch/sproutbook-gallery37.cjs
```

Limits: Linux headless Chromium/WebKit are not physical Android or iPhone browsers. Frame callback timings in the reports describe this local headless run and do not establish mobile 60 FPS, battery usage or device performance. This local run precedes publication; live deployment verification is a separate check. APK link paths were checked, but release binaries were not downloaded by these browser tests.
