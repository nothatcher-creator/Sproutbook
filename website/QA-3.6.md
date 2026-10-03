# Native release 3.6: showcase download and gallery verification

Update date: 2026-10-03 UTC. The existing woodland journey remains intact; this
update changes current-release download/version metadata, feature copy and adds
four actual native screenshots. Demos still use temporary sample data.

## Rendered-browser checks

Real local Playwright1.63 browsers: Chromium153.0.8010.12 and WebKit26.6.
All five viewport cases passed per engine:1440×900,390×844,360×640,320×700,
667×375. Four no-JavaScript cases passed (two per engine). Chromium200% text
passed at390×844 and1440×900. Evidence:
`docs/evidence/native-release-2026-10-03/{chromium,webkit,large-text}.json`.

Checks include visible APK links, keyboard skip link, horizontal overflow,
forward/reverse scene/SVG/plant/phone/animal poses, four depth layers, readable
chapters, native wheel, idle script/offscreen ambient pause, persistent motion-off,
live reduced-motion change, sample memory/feeding/teeth/stage demos and GitHub
issue-draft feedback. No uncaught script errors or failed local resources.

Runtime `styles.css`, `journey.css` and `motion.js` are byte-identical to the prior
published journey. New screenshots are unmodified native API29 emulator captures;
older captures retain their version labels. Native test results are separate in
`../android/docs/QA-3.6.md`.

Reproduce against the source preview:

```sh
NODE_PATH=/workspace/scratch/sproutbook-browser/node_modules node tools/verify-journey.cjs http://127.0.0.1:8765/ OUT 3.6.0 chromium
NODE_PATH=/workspace/scratch/sproutbook-browser/node_modules PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS=1 node tools/verify-journey.cjs http://127.0.0.1:8765/ OUT 3.6.0 webkit
NODE_PATH=/workspace/scratch/sproutbook-browser/node_modules node tools/verify-large-text.cjs http://127.0.0.1:8765/ OUT
```

These are Linux browser-engine checks. Physical Android/iPhone browsers,
assistive-technology speech, hardware frame rate and battery use remain unmeasured.

## Packaging and publication

Final source archive/checksum validation and Sites publication are pending.
Update this section with the exact source/version/deployment IDs after publishing;
changing GitHub `website/` alone does not publish the existing live Site.
