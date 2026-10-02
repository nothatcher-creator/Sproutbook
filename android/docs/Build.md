# Toolchain refresh · 29 September 2026

The phase 1–11 checkpoint used AGP 8.9.1, Kotlin 2.1.20, Gradle 8.11.1 and SDK 36. The final build targets Android 17/API 37, using the stable SDK 37.0 platform, AGP 9.1.1, Gradle 9.3.1, Kotlin 2.2.10 and KSP 2.3.12. AGP's built-in Kotlin is enabled; no legacy source-set suppression is used. API 37.1/37.2 quarterly APIs are not required by this application.

Primary build references:
- https://developer.android.com/about/versions/17/setup-sdk
- https://developer.android.com/build/releases/agp-9-1-0-release-notes
- https://developer.android.com/build/migrate-to-built-in-kotlin
- https://github.com/google/ksp/issues/2729

The Gradle distribution SHA-256 is pinned in the wrapper. Android SDK archives in the optional bootstrap are checked against the official repository's SHA-1 metadata. App dependencies remain pinned, with no dynamic `+` versions. Android 17 behavior checks on a supported hardware device remain a public-release gate.
