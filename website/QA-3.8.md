# SproutBook 3.8 showcase verification

Fresh checks on 2026-10-04 UTC cover the temporary website memory demo.
Chromium 151.0.7922.173 verified all seven tree themes and distinct scenes; keyboard opening,
focus containment and return; Edit, Cancel, Escape, Save, Add and Reset; retained
feeding and teeth controls; and twelve leaves with non-overlapping 44px targets
at 320px and 390px widths. No page errors were observed.

At 390×844, 320×740 and 844×390, leaf reading and editing remained usable with
200% text and long stories. The initial narrow-screen button overflow was fixed
by allowing labels to wrap. The leaf's decorative corners are bounded so a long
story stays within its reading area. The supplied journey controller and motion
stylesheet retain their original bytes.

The hosted and GitHub HTML/JS/CSS copies match. Node syntax checks and the
existing DOM behavior regression passed. DOM simulation does not verify layout;
the rendered Chromium checks and screenshots provide that evidence. Website
samples use temporary data and do not connect to native family records.

Evidence: `docs/evidence/memory-leaf-2026-10-04/`. Final download checksums and
publication details are recorded after packaging in the repository continuation
checkpoint. The native app has its own [QA ledger](../android/docs/QA-3.8.md).
