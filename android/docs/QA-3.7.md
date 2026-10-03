# SproutBook 3.7 organizer verification

Status: implementation in progress, 2026-10-03 UTC. No organizer release yet.

Fresh baseline: Gradle `:core:test --rerun :app:testDebugUnitTest --rerun`
passed at 18:57 UTC, 109 core + 2 app tests, zero failures/errors/skips.
`artifacts/organizer-baseline.log` records exact output (ignored build evidence).

Planned affected checks: per-child layout and shortcuts; stage-aware visibility;
Save/Cancel/Preview/Restore; caregiver mutation protections; scroll gestures;
background image decoding, private persistence and portable backup remapping;
Room 12→13 upgrade; legacy backup imports; offline startup and app recreation.
Normal and 150% font native screenshots need rendered inspection.

Unit tests, API 29 native emulator tests, real browser verification and physical
phone checks will be recorded separately. None of the new checks are claimed yet.
Existing physical-device and production-signing limitations continue from QA-3.6.
