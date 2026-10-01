# SproutBook showcase website

Static website for the native SproutBook Android application. `dist/` contains the complete public site. It can be served by any static hosting provider; it does not require a build, account secrets, or a server.

The download targets the existing V1.0 GitHub Release containing app version 3.4.0. The feature-request form creates a URL for a pre-filled GitHub issue. Visitors must review and submit that issue while signed in to GitHub. The site does not submit issues on their behalf or store their requests privately.

Memory, feeding, tooth and stage examples are temporary website sample state. They do not connect to Android records and reset when the page reloads. Screenshots are authentic 3.3 emulator captures using synthetic data and are labelled as earlier UI. Original tab illustrations are from the 3.4 app.

For future APKs, update the three download links and release metadata in `dist/index.html`. No analytics or database is used. Google Fonts is loaded externally with system-font fallbacks. The interactive script progressively enhances the page; downloads and GitHub links work without JavaScript.
