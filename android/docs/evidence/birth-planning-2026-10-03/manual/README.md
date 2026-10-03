# Focused native manual QA — 2026-10-03

Target: emulator-5554, dedicated synthetic API 29 software device, 360×800, installed native 3.6.0 debug build. Run occurred during concurrent R8 compilation on the host.

Launch: `am start -n com.nothatcher.sproutbook/.MainActivity` succeeded. `01-launch.png` captured the dark launch window and `01-launch.xml` recorded `ERROR: null root node returned by UiTestAutomationBridge.` A bounded retry produced no UI hierarchy (`02-launch-retry.xml`), and a final 12-second retry also produced no hierarchy (`03-final-root.xml`). No coordinate taps were performed without a hierarchy.

`02-launch-retry.png` captures actual native Willow home UI after it rendered, with the Pregnancy chapter and the Today/Schedule/Care/More navigation. `foreground.txt` records the resumed native activity. `font-scale.txt` reports 1.0. `crash-buffer.txt` is empty (no crash-buffer entries at snapshot).

Emergency phone ACTION_DIAL verification and return to Memory tree were not executed because the hierarchy was unavailable within the assigned three-minute device ownership window. No calls were placed and no synthetic records were changed. Idle gfxinfo/framestats and meminfo sampling was not taken because a stable tree screen could not be reached. These artifacts establish launch and observed UI state only; they are not performance measurements or a dialer pass.

Handoff: native app remains foreground on Willow home; font scale remains 1.0.
