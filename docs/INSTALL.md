# Install SproutBook

[Download the 3.8.0 APK](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.8.0/SproutBook-3.8.0-debug.apk) · [Direct download](https://sproutbook-woodland.nothatch.chatgpt.site/downloads/SproutBook-3.8.0-debug.apk) · [Release notes](RELEASE-3.8.0.md)

Requires Android 8.0 or newer. This is a debug-signed evaluation build, not a Google Play production release.

1. Download `SproutBook-3.8.0-debug.apk` on your Android device.
2. Open it through your browser's downloads or Files app.
3. Android may ask you to allow installations from that app. Enable the permission for that app, return to the APK, and choose Install. You can disable that permission afterward.
4. Open SproutBook and add a child profile. Core features work without Internet access.

## Updating an existing installation

Export a backup from SproutBook first and keep that pre-update file. Install the new APK over the existing installation; avoid uninstalling, because that deletes local app data. Updates require the same package and signing identity. This build uses `com.nothatcher.sproutbook`, version code `30800`, and the existing evaluation certificate. Its SHA-256 fingerprint is:

```text
9c97e149698a89e51ef69e5a6af1649b1b55004f72f8cf57fd7993c5290ec0f3
```

3.8.0 keeps Room schema 13 unchanged. The additive migration introduced in 3.7 retains each child's Today layout and private background references. The leaf-card and new tree-theme update changes presentation without moving saved memory leaves or deleting records. Use [native QA](../android/docs/QA-3.8.md) for the fresh verification and device limits.

If Android reports a signature conflict with an older version, keep that app installed until you have exported its records. A production-signed installation needs its original private production key; this evaluation key cannot update it. Missing-key builds fail instead of silently generating another signing identity.

## Backups and older versions

3.8.0 exports native backup format **5**, including Today layouts and referenced background photos, wishlists and child appearance. It imports formats **1–5**, filling default Today choices when restoring older native backups. Restored photos receive fresh private filenames and their record references are updated. Old HTML/WebView backups are not compatible.

**3.6.0 and earlier native apps reject format-5 backups. 3.7 cannot restore a format-5 backup containing Spring, Blossom, Winter or Rainbow; use 3.8 or newer for those exports.** Keep your pre-update backup if you may need older data later. Do not install an older app over the schema-13 database or assume a new backup can be restored by an older app. A pre-update backup contains only records saved before the update.

## Verify the file

The release includes [SHA256SUMS-3.8.0.txt](https://github.com/nothatcher-creator/Sproutbook/releases/download/v3.8.0/SHA256SUMS-3.8.0.txt). Download the manifest beside the APK and source ZIP. On Linux/macOS with `sha256sum`:

```sh
sha256sum -c SHA256SUMS-3.8.0.txt
```

On Windows PowerShell:

```powershell
Get-FileHash .\SproutBook-3.8.0-debug.apk -Algorithm SHA256
```

Compare the result with the published checksum.

## Report a problem

[Open an issue](https://github.com/nothatcher-creator/Sproutbook/issues) with your Android version, device model, SproutBook version, what you tapped, and what happened. Use synthetic examples and remove personal information from screenshots. Do not post family backups, medical details, or emergency contacts.
