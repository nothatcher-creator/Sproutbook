# Install SproutBook

[Download the 3.6.0 APK](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.6.0/SproutBook-3.6.0-debug.apk) · [Direct download](https://sproutbook-woodland.nothatch.chatgpt.site/downloads/SproutBook-3.6.0-debug.apk) · [Release notes](RELEASE-3.6.0.md)

Requires Android 8.0 or newer. This is a debug-signed evaluation build, not a Google Play production release.

1. Download `SproutBook-3.6.0-debug.apk` on your Android device.
2. Open it through your browser's downloads or Files app.
3. Android may ask you to allow installations from that app. Enable the permission for that app, return to the APK, and choose Install. You can disable that permission afterward.
4. Open SproutBook and add a child profile. Core features work without Internet access.

## Updating an existing installation

Export a backup from SproutBook first and keep that pre-update file. Install the new APK over the existing installation; avoid uninstalling, because that deletes local app data. Updates require the same package and signing identity. This build uses `com.nothatcher.sproutbook`, version code `30600`, and the existing evaluation certificate. Its SHA-256 fingerprint is:

```text
9c97e149698a89e51ef69e5a6af1649b1b55004f72f8cf57fd7993c5290ec0f3
```

An install-as-update from 3.5.1 to 3.6.0 was verified on a dedicated offline Android 10 emulator. Room's explicit, additive schema 11→12 migration preserved all pre-existing fixture rows and values and the settings file. Existing children receive Forest accents and Summer trees; their records remain in place.

If Android reports a signature conflict with an older version, keep that app installed until you have exported its records. A production-signed installation needs its original private production key; this evaluation key cannot update it. Missing-key builds fail instead of silently generating another signing identity.

## Backups and older versions

3.6.0 exports native backup format **4**, including wishlists and child appearance. It imports formats **1–4**, filling defaults when restoring older native backups. Old HTML/WebView backups are not compatible.

**3.5.1 and earlier native apps reject format-4 backups.** Keep your pre-update backup if you may need older data later. Do not install an older app over the schema-12 database or assume a new backup can be restored by an older app. A pre-update backup contains only records saved before the update.

## Verify the file

The release includes [SHA256SUMS-3.6.0.txt](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.6.0/SHA256SUMS-3.6.0.txt). Download the manifest beside the APK and source ZIP. On Linux/macOS with `sha256sum`:

```sh
sha256sum -c SHA256SUMS-3.6.0.txt
```

On Windows PowerShell:

```powershell
Get-FileHash .\SproutBook-3.6.0-debug.apk -Algorithm SHA256
```

Compare the result with the published checksum.

## Report a problem

[Open an issue](https://github.com/nothatcher-creator/Sproutbook/issues) with your Android version, device model, SproutBook version, what you tapped, and what happened. Use synthetic examples and remove personal information from screenshots. Do not post family backups, medical details, or emergency contacts.
