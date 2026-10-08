# SproutBook 3.8 memory leaf verification

Updated: 2026-10-04 UTC. Final gates passed: 129 units, 16 unique native methods
and six separately counted viewport repeats. The ledger combines fresh affected
checks with explicitly reused prior checks; it is not a fresh full 16-test run.

Recovered baseline: GitHub source 1790b5b, hosted showcase source 78352f4.
Room schema 13 and backup format 5 are preserved. The intentionally distributed
3.5.0 evaluation key was recovered outside the checkout and its certificate
matches 9c97e149698a89e51ef69e5a6af1649b1b55004f72f8cf57fd7993c5290ec0f3.
The actual delivered 3.7.0 APK and new 3.8.0 debug APK both passed `apksigner`
verification with that same certificate, preserving evaluation update identity.
Signing material remains outside Git; source packaging excludes signing keys.

## Build and source review

The final integrated build passed in 7m 22s with 141 actionable tasks: 33
executed and 108 up-to-date. Both unit tasks freshly executed; its 129 tests
comprise 126 core plus 3 app tests, with zero failures/errors/skips. Outputs
include debug APK and Android test APK,
unsigned R8 release APK and AAB, and vital lint. Package com.nothatcher.sproutbook, version
3.8.0 / code 30800, min SDK 26 and target SDK 37. Both debug and Android test
APKs retain the recovered evaluation certificate. Release outputs are unsigned
and have not been installed at runtime.

The first integrated build before the responsive footer passed in 8m 39s with
139 actionable tasks: 115 executed and 24 up-to-date. Its evidence and original
artifact hashes are preserved separately from the final build.

The first test runs recorded expected failures for missing new-theme backup
acceptance and distinct palettes. A subsequent run found an old invalid-value
test that used Winter; that fixture now uses unsupported Space. The final full
unit suite passed. Evidence includes red results and the final green XML.

Independent source review found no Important/Critical findings in the card,
child scoping, caregiver guards, theme catalogue, profile preview, artwork or
packaging. TreeLayout.kt and MemoryTree.kt are unchanged; branch endpoint
coordinates are retained and decorations render beneath interactive leaves.

The initial software emulator boot failed while compiling, before this app was
installed. It was restarted with no simultaneous build load for native QA.
Hardware startup/performance conclusions cannot be drawn from this emulator.

## Native flow verification

The first normal API-29 run completed 16 tests with nine passes and seven
failures. The retained `native-first-normal.log` and its summary record that run.
Fixture corrections wait for the expected collected leaf count before clicking
Arrange or inspecting the full canopy. The memory save fixture waits for the
unique viewer: both the existing editor and the card say "A moment to keep",
so waiting for that title alone could finish before the editor closed. The
profile fixture selects the child profile card using its action and excludes
the same-name child switcher. The caregiver fixture proves Arrange is active
through its "Select" accessibility action before enabling read-only mode.

The original writable-card Back assertion ran immediately after shell input.
The corrected test sends one Back and waits for actual card dismissal, without
retrying Back or changing production code. Its before/after native input
diagnostics both report `mInputShown=false` and `mShowRequested=false`; the
focused window changes from `ADJUST_PAN` to `ADJUST_RESIZE`. The unchanged app
passes that flow, supporting native window/focus settling in the corrected run.
The original failed run did not capture those per-Back flags, so it does not
establish that the keyboard consumed the original event.

The corrected normal run completed in 541.585 seconds: 15 of 16 passed, including
all four card flows, memory add/view/edit/delete, both tree gesture checks, full
32-leaf native framework accessibility, profile preview/cancel/child-scoped Save
and the eight-scene gallery. The inventory-adjustment check recorded stock 3.0
after one tap, with caregiver mode off and Activity focus true. A focused
diagnostic rerun also failed in 155.354 seconds. Its actual screenshot and
semantics show the "Saved" snackbar occupying y=624–672 while the "Use one"
tap target's center is (85, 630): the tap lands on the overlay. Before/after
screenshots are identical. The fixture tapped the snackbar covering the
inventory button; a fixture-only wait for the snackbar to clear is verified
by the passing inventory decrement/delete case in the final-build
normal run. A separate resumed-emulator attempt
failed during fixture setup before the inventory flow started; that boot
failure is retained separately. No production change was made for these checks.
Original failed logs and diagnostics are retained.

Before the action layout polish, the fixture-only APK was rebuilt separately.
Its metadata verifies that
the debug APK, unsigned release APK/AAB and R8 mapping remain byte-identical to
the full integrated build. Direct SHA-256 comparison against
`pre-responsive-actions-final-build-metadata.json` reconfirmed all four production
outputs unchanged before the localized action-layout source edit.

The first 150% portrait repeat completed five checks in 323.611 seconds, with
four passes and one failure. The writable card reached editing but timed out
waiting for the edited title after a single Save tap; that run did not capture
the editor's native input geometry, so its cause is unresolved. Read-only long
story/photo, caregiver revocation, Today recreation and profile theme preview
passed. The writable-card screenshot showed "Edit memory" wrapping into three
short lines. The localized footer now measures both labels and default button
padding, gives Close its intrinsic width when a row fits, and stacks full-width
buttons otherwise. Native controls retain their labels and 48dp minimum height.

The first final-build normal attempt completed five checks in 337.898 seconds:
four passed, including inventory decrement/delete, and writable Save failed.
It reproduced the Save miss with native captures. The edited title is present
in editor semantics and caregiver mode
is off. Before the click, Save occupies y=612–664 and the visible frame ends
at y=752; the IME service already reports a pending/shown keyboard, while the
input dispatcher still marks its surface invisible. After the click, the
keyboard is visible from y=489, the frame ends there, and Save is clipped out.
Those captures establish a keyboard entrance/layout race in this run. The
fixture now waits for physical IME visibility and the resized frame, dismisses
the keyboard once, then waits for both IME flags to clear and the original
visible frame to return before re-scrolling and making exactly one Save tap.
The earlier 150% failure did not capture these flags, so its exact cause remains
unproven. Production editor and save behavior are unchanged.

The keyboard-readiness test-only build passed in 1m 13s with 57 tasks, six
executed. Its separate metadata identifies the refreshed signed test APK.
Direct SHA-256 checks reconfirmed that all four production artifacts remain
byte-identical to the final 7m 22s integrated build.

The focused writable rerun with that readiness gate passed in 118.221 seconds,
through one Save, restored keepsake viewing, Timeline reopening and Back
dismissal. Both Save captures show cleared IME flags, the restored y=752
visible-frame bottom and unchanged y=612–664 target geometry. Together with
the three successful card cases and inventory from
the first final-build attempt, all five selected normal checks pass on the
final production artifact. The eleven unaffected successful normal checks and
the earlier 150% profile preview are reused with their original logs; they
were not rerun after the localized footer change.

All four final-build 150% portrait card repeats passed in 252.345 seconds.
Reviewed captures show clear single-line Close/Edit labels in the stacked
footer and a reachable complete long-story ending. The writable Save target
remains y=642–694 in both captures with IME flags cleared and the original
visible frame restored; the stored title is updated after the single tap.

The first compact-landscape attempt failed before opening the card in 122.967
seconds: its wait targeted a tree lazy-list item that was not composed in that
viewport. The same writable method now accepts `openViaTimeline=true` for the
compact-window repeat. The fixture records bounds, screenshots, semantics,
window and IME state before and after exactly one Save tap, and on a database
wait failure. Neither the editor nor its mutations were changed. The completed
compact result is recorded below; repeats do not increase the unique test count.

The first final-build compact attempt also stopped before the test body, in
92.756 seconds, while fixture setup waited for the Today chapter caption.
That failure did not reach Timeline, the keepsake or Save and supplies no
card-layout result. A responsive warm screenshot/hierarchy at the measured
720×360 size shows only the Today header/date; the chapter lazy section is
uncomposed. Test setup now scrolls to that tagged section before its existing
seeded-caption and native-focus checks. Display preparation for the final
repeat verifies actual 800×360 bounds and 150% text, with a 752×360 app area.
The compact-readiness test-only rebuild passed in 1m 16s with 57 tasks, six
executed; its refreshed test APK retains the same certificate. Direct SHA-256
checks again confirm all production outputs unchanged from the final integrated
build.

The following 800×360 compact attempt reached the card but failed in 102.344
seconds because its global title query matched both the keepsake and the
underlying Timeline row. The fixture now scopes card metadata and Edit to the
keepsake ancestor and captures the card immediately after readiness. This
retains the original display/existence checks and single Save action.
The scoped fixture rebuild passed in 1m 16s with 57 tasks, five executed.
Its separately recorded test APK preserves the certificate and all production
hashes remain unchanged.

The scoped compact attempt stopped in 39.670 seconds before a capture with
"Animators may only be run on Looper threads". Its stack runs from native
ripple animation through Compose UI-test continuation interceptors and
`TestMonotonicFrameClock` to a thread pool, with no product frame or metadata
assertion. That failure is retained separately and no production workaround
was introduced.

The unchanged focused compact rerun passed in 125.827 seconds at verified
800×360 bounds and 150% text through Timeline opening, viewing, editing, one
Save, reopening and Back. Its reviewed capture shows the full title, child
context and clear Close/Edit row within the leaf. Native Save captures show
the stored title changing while target geometry remains stable. The prior
test-clock failure did not recur. Emulator display settings were restored to
360×800, 100% text and rotation zero after the captures.

The completed selection covers 16 unique successful methods with no unresolved
failed or incomplete gate. Six successful repeats comprise four final 150%
card checks, the earlier unaffected 150% profile check and the final compact
Timeline check. Ten selected executions are fresh on the final production
artifact; eleven normal checks and one profile repeat are reused. The
machine-readable final summary names each source log and artifact hash and
preserves earlier failed attempts separately.

## Compatibility and remaining limits

The four new tree names use the existing child presentation column and require
no Room migration. Unit tests validate all seven supported names, portable
backup round trips and unchanged memory chapter/anchor values. Older backup
formats remain accepted by 3.8.0. Room schema 13 and backup format 5 are
unchanged, but 3.7 and earlier reject exports containing Spring, Blossom,
Winter or Rainbow; use 3.8 or newer to restore those exports.

The native card fixtures read actual private JPEG files created from synthetic
test data. They do not exercise Android's external photo picker. Read-only
viewing, recreation, memory editing/deletion, profile Save/Cancel and child-scoped
theme updates are covered by the recorded emulator checks. The native
accessibility check inspects Android's framework provider and paged custom
actions; spoken TalkBack has not been tested.

No fresh physical-device startup, FPS, battery, audio or Android 17/API-37
background-service/notification result is claimed. The R8 outputs were built
and checked as unsigned artifacts; neither a minified APK nor an AAB-derived
installation has been tested at runtime. A real 3.7.0-to-3.8.0 installed update
and its record preservation have not been exercised. The earlier release's
upgrade results remain historical evidence. Production signing is unavailable;
this update retains the existing evaluation identity.

## Evidence

All native fixtures use a dedicated API-29 software emulator and synthetic
family records. They must not run against a family installation.

- [First integrated build](evidence/memory-leaf-2026-10-03/pre-responsive-actions-final-build.log),
  [its build metadata](evidence/memory-leaf-2026-10-03/pre-responsive-actions-final-build-metadata.json).
- [Final integrated build](evidence/memory-leaf-2026-10-03/final-build.log)
  and [129 units and final artifact metadata](evidence/memory-leaf-2026-10-03/final-build-metadata.json).
- [Delivered 3.7.0 APK signature](evidence/memory-leaf-2026-10-03/prior-3.7-apk-signature.txt)
  and [final 3.8 debug APK signature](evidence/memory-leaf-2026-10-03/debug_apk-signature.txt).
- [Original 9/16 normal run](evidence/memory-leaf-2026-10-03/native-first-normal.log)
  and [its summary](evidence/memory-leaf-2026-10-03/native-first-normal-summary.json).
- [Corrected 15/16 normal run](evidence/memory-leaf-2026-10-03/native-normal.log)
  and [its summary](evidence/memory-leaf-2026-10-03/native-normal-summary.json).
- [First 150% portrait repeat](evidence/memory-leaf-2026-10-03/native-large.log)
  and [its 4/5 summary](evidence/memory-leaf-2026-10-03/native-large-summary.json);
  [first compact-landscape attempt](evidence/memory-leaf-2026-10-03/native-landscape.log)
  and [its pre-viewer failure summary](evidence/memory-leaf-2026-10-03/native-landscape-summary.json).
- [First final-build normal attempt](evidence/memory-leaf-2026-10-03/native-final-normal.log)
  and [its 4/5 summary](evidence/memory-leaf-2026-10-03/native-final-normal-summary.json);
  [single-Save native input evidence](evidence/memory-leaf-2026-10-03/diagnostics/memory-save-final-normal/).
- [Corrected final normal writable flow](evidence/memory-leaf-2026-10-03/native-final-normal-ready.log),
  [its summary](evidence/memory-leaf-2026-10-03/native-final-normal-ready-summary.json)
  and [corrected Save geometry](evidence/memory-leaf-2026-10-03/diagnostics/memory-save-final-normal-ready/).
- [Final 150% portrait card repeats](evidence/memory-leaf-2026-10-03/native-final-large.log)
  and [large-text Save geometry](evidence/memory-leaf-2026-10-03/diagnostics/memory-save-final-large/).
- [Final-build compact setup failure](evidence/memory-leaf-2026-10-03/native-final-landscape-setup.log),
  [its summary](evidence/memory-leaf-2026-10-03/native-final-landscape-setup-summary.json),
  [warm compact view](evidence/memory-leaf-2026-10-03/tooling/compact-home-ready.png)
  and [final display configuration](evidence/memory-leaf-2026-10-03/tooling/final-landscape-ready-display-state.txt).
- [Compact title-query ambiguity](evidence/memory-leaf-2026-10-03/native-final-landscape-ready.log)
  and [its summary](evidence/memory-leaf-2026-10-03/native-final-landscape-ready-summary.json).
- [Scoped compact test-clock failure](evidence/memory-leaf-2026-10-03/native-final-landscape-card.log)
  and [its summary](evidence/memory-leaf-2026-10-03/native-final-landscape-card-summary.json).
- [Passing final compact flow](evidence/memory-leaf-2026-10-03/native-final-landscape-card-repeat.log),
  [its summary](evidence/memory-leaf-2026-10-03/native-final-landscape-card-repeat-summary.json)
  and [800×360 keepsake capture](evidence/memory-leaf-2026-10-03/screenshots/final-landscape-card-repeat-keepsake-top.png).
- [Completed unique/repeat ledger](evidence/memory-leaf-2026-10-03/final-summary.json)
  and [restored display settings](evidence/memory-leaf-2026-10-03/tooling/restored-display-settings.txt).
- [Focused inventory diagnostic](evidence/memory-leaf-2026-10-03/native-inventory-diagnostic.log),
  [input screenshots and state](evidence/memory-leaf-2026-10-03/diagnostics/inventory-input/)
  and [resumed-emulator setup failure](evidence/memory-leaf-2026-10-03/tooling/native-inventory-diagnostic-boot.log).
- [Before Back](evidence/memory-leaf-2026-10-03/diagnostics/normal-before-timeline-back.txt)
  and [after Back](evidence/memory-leaf-2026-10-03/diagnostics/normal-after-timeline-back.txt)
  native input flags; [original logcat](evidence/memory-leaf-2026-10-03/back-debug-normal-logcat.log).
- [Normal-readiness fixture metadata](evidence/memory-leaf-2026-10-03/normal-readiness-test-apk-metadata.json),
  [later diagnostic fixture metadata](evidence/memory-leaf-2026-10-03/inventory-diagnostic-test-apk-metadata.json)
  and [native screenshots](evidence/memory-leaf-2026-10-03/screenshots/).
- [Final keyboard-readiness test build](evidence/memory-leaf-2026-10-03/keyboard-ready-test-build.log),
  [test APK metadata](evidence/memory-leaf-2026-10-03/keyboard-readiness-test-apk-metadata.json)
  and [its signature](evidence/memory-leaf-2026-10-03/keyboard-readiness-test-apk-signature.txt).
- [Compact-readiness test APK metadata](evidence/memory-leaf-2026-10-03/compact-readiness-test-apk-metadata.json)
  and [its signature](evidence/memory-leaf-2026-10-03/compact-readiness-test-apk-signature.txt).
- [Scoped compact fixture build](evidence/memory-leaf-2026-10-03/compact-card-test-build.log),
  [its test APK metadata](evidence/memory-leaf-2026-10-03/compact-card-test-apk-metadata.json)
  and [its signature](evidence/memory-leaf-2026-10-03/compact-card-test-apk-signature.txt).
