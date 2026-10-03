# Native release 3.6: showcase download and gallery verification

Update date: 2026-10-03 UTC. The existing woodland journey remains intact; this
update changes current-release download/version metadata, feature copy and adds
four actual native screenshots. Demos still use temporary sample data.

## Rendered-browser checks

Real local Playwright 1.63 browsers: Chromium 153.0.8010.12 and WebKit 26.6.
All five viewport cases passed per engine: 1440×900,390×844,360×640,320×700,
667×375. Four no-JavaScript cases passed (two per engine). Chromium 200% text
passed at 390×844 and 1440×900. Evidence:
`docs/evidence/native-release-2026-10-03/{chromium,webkit,large-text}.json`.

Checks include visible APK links, keyboard skip link, horizontal overflow,
forward/reverse scene/SVG/plant/phone/animal poses, four depth layers, readable
chapters, native wheel, idle script/offscreen ambient pause, persistent motion-off,
live reduced-motion change, sample memory/feeding/teeth/stage demos and GitHub
issue-draft feedback. No uncaught script errors or failed local resources.

Runtime `styles.css`, `journey.css` and `motion.js` are byte-identical to the prior
published journey. New screenshots are unmodified native API 29 emulator captures;
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

Static asset/version/download validation and sample DOM simulation passed
separately after packaging. APK and reassembled two-part source ZIP match
`SHA256SUMS-3.6.0.txt`; every static file is below 25 MiB. ZIP bytes are unchanged
by splitting. Sites publication succeeded 2026-10-03 16:44:38 UTC. Existing public Site version 5,
source `1f393520be147bcff40f718c459331a29e302115`, saved version
`appgprj_6abeb834e1a88191a8c4907b567c3472~appgver_a9c1cf7803cc8191a32d984aff5a43dd`,
deployment `appgdep_6ac130de87848191bbb45d97523f9273`.
URL https://sproutbook-woodland.nothatch.chatgpt.site

Actual live Chromium passed all five viewport regressions and both no-JavaScript
cases after publication. `docs/evidence/native-release-2026-10-03/live-chromium.json`
and `publication.json` record results. WebKit/200% checks are local and remain
separately identified. Site source checkout is clean at the published SHA. Its
pre-publication QA snapshot is frozen; this GitHub ledger is the final publication
record. A fresh short-lived write credential resolved an initial expired-token
Git push; no credential was persisted.
