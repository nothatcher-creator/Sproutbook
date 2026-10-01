# Data and privacy notes

These notes describe the distributed SproutBook 3.4.0 Android preview. They are not a claim of an independent security audit.

## Local family records

Children's profiles, care logs, memories, appointments, health notes, emergency information, inventory and settings are stored on your device. The app uses Room for structured records and DataStore for preferences. It has no Internet permission, analytics service, login requirement, or implemented cloud sync. Android automatic system/cloud backup is disabled for this build.

Opening a source link in parenting guidance uses your external browser and that website's own privacy practices. Android may independently perform system-level checks or installation scanning.

## Permissions and Android features

Optional notifications support appointment reminders. Native foreground audio provides the sound machine and its playback notification. Photos and backup files are selected through Android's file picker. The app's emergency copy action writes the chosen text to the Android clipboard.

## Export and sharing

In-app backups contain readable family information and may include photos encoded as Base64. They are not encrypted by SproutBook. Export only to locations you trust, and keep copies private. Import validates records before requesting replacement of the current local family data.

Uninstalling the app removes its local app data. Export a backup first if you want to keep those records. Removing the app does not delete copies you have exported elsewhere.

## Caregiver mode

Grandparent mode is a read-only convenience that prevents accidental record changes. It is not authentication: a person using an unlocked device can turn it off. Use Android's device lock to protect access to your family's records.

## Public bug reports

GitHub issues are public. Do not upload real family backups, children's photos, medical records, phone numbers or addresses. Describe problems with synthetic data and redact screenshots before posting.
