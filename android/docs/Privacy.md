# Privacy notes for the local Android build

SproutBook stores child profiles, health notes, appointments, logs, memories and settings in the app's private storage on the device. This build has no account system, advertising SDK, analytics SDK, cloud sync or Internet permission. It does not upload family records to a developer-operated server.

Photos selected by the user are copied into app-private storage, resized and re-encoded. The app does not scan the photo library. Android system/cloud app backup is disabled for this build. In-app export creates an unencrypted backup at the location the user chooses, which may itself be a cloud-backed document provider. The user controls that provider and any sharing afterward.

Clipboard, Android sharing, browser source links and phone dialer actions occur only when the user chooses those actions. External applications have their own privacy policies. Appointment notifications can expose their title on the unlocked device; the public lock-screen version uses generic wording.

Deleting a child removes their database records. Unreferenced copied photos may remain until app data is cleared; see Review.md. Removing the app or clearing its storage deletes local data. Keep an export before doing so if the records must be retained.

Grandparent mode is a read-only convenience setting. It is not a password-protected caregiver account. Secure the device with Android's screen lock.

Before publishing, the app owner must provide a public privacy policy URL and contact details, verify these statements against the signed release and complete the store's data-safety disclosures. This file describes implementation behavior; it is not a published legal policy.
