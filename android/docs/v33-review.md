# 3.3 review and resolutions

One independent read-only review inspected every changed/new production and test
file. It found no critical issue. Two important and three minor findings were
addressed in this update:

- Paused sound switching requested audio focus. An Android test with another
  focus owner reproduced the interruption. The service now acquires focus only
  for actual playback/resume; paused replacement keeps the frozen timer.
- Tooth stage symbols had poor dark-theme contrast. Erupted uses onPrimary;
  Observed uses a dark foreground against muted gold.
- Narrow-portrait 48dp hitboxes overlapped. A unit test reproduced the overlap;
  disjoint targets now follow a curve through their centres at widths 240–420dp.
- Idle sound choices disappeared when changing sub-tabs. A Compose test
  reproduced the reset; pending selection now belongs to saveable hub state.
- Read-only mode exposed a no-op long-click. The action and label are now omitted
  and page instructions say Read only.

Initial UI captures exposed an AOSP System UI “isn't responding” overlay on the
software emulator. It stole window focus, causing INJECT_EVENTS errors and an
Android 29 clipboard denial. The overlay was dismissed. QA now requires app
window focus before beginning and capturing screens, and sends hardware Back
through UiAutomation shell input. Product Back and clipboard handling are unchanged.
The clean tooth and clipboard checks passed.

One navigation regression then waited for a Care heading outside a restored lazy
viewport. The QA helper now verifies the selected Care tab and scrolls to the
heading before asserting it. This changes the test harness only; the product's
scroll restoration is retained. The original failing log is preserved.

## Scope decisions

The reviewer explicitly set aside these behaviors. Each is accepted as outside
this bounded care/graphics update, with these limits:

- Automatic recovery after transient focus loss: inherited stop policy retained;
  users can restart intentionally.
- Headphone/Bluetooth disconnect policy and MediaSession controls: additional
  playback policy; track as future audio hardening before public distribution.
- Session restoration after process death/reboot: START_NOT_STICKY retained;
  unexpected automatic sound is avoided.
- Per-child sound preferences: sound is device-level and contains no child data.
- Physical speaker quality, screen-off power, Galaxy S25 performance: require
  hardware QA; an emulator and code review cannot establish them.
- Dental predictions/clinical assessment and schema changes: journal scope only;
  no diagnosis or migration added.
- External generated artwork: Higgsfield rejected generation due the account
  plan; native graphics are used and no generated asset is claimed.
- Rewriting unchanged emergency content: separate clinical/editorial review;
  this update checked the new articles against their primary sources.
