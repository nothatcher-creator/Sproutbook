# Independent organizer reviews

Data/core review found no blocking record-loss, child-isolation or backup issue.
UI review found three concrete issues, all corrected before final native QA:
preview editor pointer targets remained underneath; detached background imports
silently discarded results; Add-memory query reopened an editor on child switch.
Preview now zero-measures the retained editor, detached results report choose-again
feedback, and the query is consumed in the navigation entry saved-state handle.

Meaningful test gaps identified: caregiver toggle while blocked photo IO,
photo-result staging/Save/Cancel/recreation, cancellation during IO and actual
external picker UI. The first two have dedicated new native fixtures; cancellation
and external picker UI remain explicitly unverified unless the final QA says otherwise.

First UI attempt used an older seven-case test APK and failed four saved-anchor
assertions because fixture saveMemory allocates anchors itself. Fixtures now use
arrangeMemory to persist the requested branch; normal app behavior was correct.
The original failing run is retained. Final results are recorded separately.
