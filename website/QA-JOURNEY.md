<!-- Current organizer/gallery regression: see docs/evidence/home-organizer-2026-10-03/BROWSER-VERIFICATION.md. Older journey evidence below remains historical. -->
# Woodland journey QA — 2026-10-02

Website-only update. Native source, package, release version, signing identity,
APK, source download and release checksums remain unchanged at 3.5.1.

## Executed checks

- JavaScript syntax: `node --check` for `app.js`, `motion.js` and browser verifiers: PASS.
- Static verifier: unique IDs, anchor targets, HTML and both stylesheets' local
  assets, two scrubbed supplied SVGs, retained motion pack, 3.5.1 links, APK/source
  SHA-256 and 25 MiB per-file hosting limit: PASS.
- DOM simulation: preserved memory / feeding / teeth / stage / GitHub draft flows:
  PASS. The historical simulated three-chapter motion test was replaced by real
  browser coverage; this simulation does not verify rendering.
- Actual local Chromium 153.0.8010.12 and WebKit 26.6, Playwright 1.63.0:
  PASS at **1440×900, 390×844, 360×640, 320×700 and 667×375** in each engine.
  Each viewport checked top APK, visible keyboard skip link, four named depth
  layers, different parallax rates, forward/reverse camera/SVG/plant/phone/card/
  animal poses, all five chapters, normal wheel scrolling, no idle JavaScript
  frame loop, stopped offscreen ambient animation, stored motion-off, live reduced
  motion, no horizontal overflow, no same-origin HTTP failures and no JS errors.
- Preserved rendered sample flows in all ten viewport cases: add/reset memory,
  log/reset 80 mL bottle, all twenty tooth detail taps, hold-to-edit, movement
  cancel, relock and explicit editor, stage switching, GitHub draft feedback with
  popup intercepted. No GitHub issue was submitted and no app record was accessed.
- JavaScript-disabled normal/reduced-motion layouts: PASS, two cases per engine.
  All chapters remain visible; motion control is disabled. Timelines begin only
  when JavaScript explicitly scrubs them.
- 200% text, Chromium reduced motion at 390×844 and 1440×900: PASS across opening,
  memory, calendar, demos, request form and final download; no horizontal overflow.
  Header wraps and clips its fractional flex overhang at enlarged text size.
- Screenshots visually inspected for opening, memory, noticeboard, feeding,
  nursery, reduced-motion layout and nighttime arrival. Foreground foliage was
  moved away from the copy; reduced-motion opening contrast and skip-link stacking
  were corrected after read-only review.

## Performance evidence and limits

The 45-position sweep recorded 135 requestAnimationFrame callbacks per viewport,
including the verifier's two settle callbacks at each position. Final local
Chromium p95 callback cost: **6.0 / 4.1 / 3.1 / 3.4 / 3.7 ms** in the viewport
order above. WebKit: **10 / 7 / 7 / 6 / 6 ms**. Those are headless Linux CPU
observations under concurrent checks, not GPU paint cost or hardware frame rate.
Idle JavaScript callback count stayed unchanged over 250 ms, and offscreen CSS
animations had no running entries. The visibility handler was reviewed in code;
physical tab lifecycle, battery drain, iPhone Safari and Android device smoothness
were not measured. No 60 FPS, physical-device, VoiceOver or TalkBack claim is made.

Three new optimized generated artwork files total **1,278,096 bytes**. Existing
paintings and motion assets are reused; phone screenshots lazy-load. A single
coalesced scroll callback updates nearby chapters, with no new runtime library.

## Reproduction and evidence

Run `tools/verify-journey.cjs URL OUT 3.5.1 chromium` or `webkit` with Playwright
available through `NODE_PATH`. Run `tools/verify-large-text.cjs URL OUT` separately.
The normal Playwright WebKit installation needs its platform libraries. This
workspace used official Debian packages extracted outside the repo because root
installation was unavailable; only the test runtime was changed.

Reports and selected screenshots: `docs/evidence/woodland-journey-2026-10-02/`.
Full captures stay in the ignored `artifacts/woodland-journey/` workspace folder.
`STORY-JOURNEY.md` records architecture, artwork provenance and continuation notes.
`tools/verify-browser.cjs` remains historical evidence for the previous panel.

## Publication

Published successfully through the existing public Sites integration at
**2026-10-02 23:21:55 UTC**. Site version **4**, source commit
`b48193de6014bac30e6978f5d989ec395b476185`.

- Saved version: `appgprj_6abeb834e1a88191a8c4907b567c3472~appgver_d1a14664ec30819183f1e47622559cb6`.
- Deployment: `appgdep_6ac03c8948288191a90f2a3bb7e7b143`, native status `succeeded`.
- Live URL: https://sproutbook-woodland.nothatch.chatgpt.site
- The same five viewport regressions and both no-JavaScript cases **passed against
  the actual published website in Chromium**. The WebKit and 200% text checks above
  were local. `live-chromium-results.json` records the published-page results.
- Live `motion.js` and `styles.css` returned HTTP 200 with
  `Cache-Control: public, max-age=0, must-revalidate`.
- Upload came from the exact pushed Git commit; the gzip archive's SHA-256 is
  `27d25a23492625ec5dabb9cee0b9d504b4299aafe93e30f74164d8414beeb2bf`
  (94,781,669 bytes). Sites normalized its upload into a tar archive before saving.

The saved source snapshot contains the prepublication ledger. This GitHub ledger
and root continuation checkpoint record final delivery. GitHub website source
uses public GitHub release download URLs; hosted output keeps verified local APK
and checksum downloads. No APK/release artifact was republished.
