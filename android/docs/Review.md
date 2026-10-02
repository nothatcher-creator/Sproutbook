# Native rebuild review · 29 September 2026

One independent whole-branch review of the phase 11 checkpoint found no Critical and six Important issues. The phase 12 correction pass addresses:

1. Formula input/results must reset when the selected child changes.
2. Local saves must enforce the backup format's text and pump-total limits with readable errors.
3. Backup tooth indices must be validated as Long before narrowing to Int.
4. Import must reject future historical entries, while allowing future appointments and meal plans.
5. Milestone leaf creation must not attach a memory belonging to another child when IDs collide.
6. Feeding, sleep, health and pregnancy history need access beyond their initial display limits.

Deferred Minor findings: unused photo files can accumulate after replacement/deletion; same-title, same-date appointments are ambiguous in the health link picker; a deleted milestone leaf cannot be recreated through the milestone editor without clearing its reference. These do not delete existing records, but need a later maintenance pass.

The review did not certify medical content, real-device audio, Android 17/API 37 background/notification behavior, Galaxy S25 performance or store readiness. Those need separate release gates. There will be one correction pass and final verification, rather than repeated independent reviews.

Final QA also found and corrected two interaction issues: redundant sleep success
snackbars obscuring rapid sound controls, and a full Memory Tree exceeding the
Android per-node accessibility action limit. Both have reproduced failures and
passing corrective regressions; see QA.md.


## 3.1.0 update review

One fresh review found four important integrity issues: growth note editing changed unknown units, rounding changed precise measurements, bottle completion ignored visible drafts, and shopping completion ignored quantity/unlink drafts. All were addressed in one correction pass with reproducing regressions. The new dashboard flow additionally reproduced Today restoring a tool; its corrected navigation passed in the final batch.

Deferred update polish is listed in QA.md and Update-3.1.0.md. The review did not independently certify real-device behavior or medical content.

## 3.2.0 update review

One independent review of the full update found one Important issue and no Critical or Minor issues. During the later occurrence of a daylight-saving repeated hour, opening a milk container reconstructed its timestamp using the earlier offset. This incorrectly disabled use/discard on untouched drafts, changed the recorded time on save, and rejected notes-only edits of terminal history. The same conversion also affected the new potty notes editor. Regression and correction evidence is tracked in `v32-ledger.md` and `evidence/v32/`.

The review traced serialized writes, transactions, parent-child ownership, duplicate completion/pump protection, strict backup compatibility, restore order, editor disposal on child switch and independent caregiver repository guards. Those areas had no findings. It declined to certify physical Galaxy/S25 behavior, physical audio behavior, Android 37 device behavior and store release gates. Those remain separate QA requirements; no second review is planned.
