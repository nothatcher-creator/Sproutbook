# Install SproutBook

[Download the 3.5.1 APK](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.5.1/SproutBook-3.5.1-debug.apk) · [Direct download](https://sproutbook-woodland.nothatch.chatgpt.site/downloads/SproutBook-3.5.1-debug.apk)

Requires Android 8.0 or newer. This is a debug-signed evaluation build, not a Google Play production release.

1. Download `SproutBook-3.5.1-debug.apk` on your Android device.
2. Open it through your browser's downloads or Files app.
3. Android may ask you to allow installations from that app. Enable the permission for that app, return to the APK, and choose Install. You can disable that permission afterward.
4. Open SproutBook and add a child profile. Core features work without Internet access.

## Updating an existing installation

Export a backup from SproutBook first. Install the new APK over the existing installation; avoid uninstalling, because that deletes local app data. Updates require the same package and signing identity. This build uses `com.nothatcher.sproutbook` and the existing native development certificate.

If Android reports a signature conflict with an older version, keep that app installed until you have exported its records. Native backup format versions 1–3 are supported; old HTML/WebView backups are not compatible. Do not assume an old app's backup can be restored into this build.

## Verify the file

The release includes `SHA256SUMS-3.5.1.txt`. On Linux/macOS with `sha256sum`:

```sh
sha256sum -c SHA256SUMS-3.5.1.txt
```

On Windows PowerShell:

```powershell
Get-FileHash .\SproutBook-3.5.1-debug.apk -Algorithm SHA256
```

Compare the result with the published checksum.

## Report a problem

[Open an issue](https://github.com/nothatcher-creator/Sproutbook/issues) with your Android version, device model, SproutBook version, what you tapped, and what happened. Use synthetic examples and remove personal information from screenshots. Do not post family backups, medical details, or emergency contacts.
