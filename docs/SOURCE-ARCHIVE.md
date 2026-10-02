# SproutBook 3.5.1 source

The actual native Android Gradle project is in `android/`. Open that directory
in Android Studio, or run its wrapper with JDK 17 and Android SDK 37 installed.
The showcase's separate static source is in `website/`; its demos use sample data.

```sh
cd android
./gradlew :core:test :app:testDebugUnitTest
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest \
  -Psprout.debugKeystore=/absolute/path/to/the/existing/evaluation.keystore
./gradlew :app:assembleRelease :app:bundleRelease
```

No signing key is packaged. Debug signing requires the existing evaluation
identity documented in `android/README.md`; it fails if the key is missing.
Release APK/AAB outputs are unsigned until configured with the original private
production identity. Do not generate another key and assume it updates an
existing installation. Keep private keys and credentials outside Git.

`android/release-artifacts/` contains this update's verified R8 unsigned APK,
AAB, and mapping file. `SOURCE-MANIFEST.json` records the exact source checkpoint
and file hashes. The release's separate `SHA256SUMS-3.5.1.txt` verifies the entire
source ZIP and the installable evaluation APK.

Read `android/docs/QA-3.5.1.md` for fresh checks and limits, and
`docs/CONTINUATION.md` for the recovery history and next steps. The authoritative
continuation checkpoint is maintained in the GitHub repository after publication:
https://github.com/nothatcher-creator/Sproutbook/blob/main/docs/CONTINUATION.md

Instrumentation fixtures replace sample data. Run them only on a dedicated QA
emulator, never on a family's installation. Use native backup/import for records.
