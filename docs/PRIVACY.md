# Data and privacy notes

These notes describe the SproutBook 3.7.0 Android evaluation build. They are not a claim of an independent security audit.

## Local family records

Children's profiles, care logs, memories, appointments, health notes, emergency information, inventory, wishlists, birth plans and settings are stored on your device. Each child's Today layout, chosen shortcuts and background choice are local records too. The app uses Room for structured records and DataStore for preferences. It has no Internet permission, analytics service, login requirement, or implemented cloud sync. Android automatic system/cloud backup is disabled for this build.

Opening a source link in parenting guidance or a wishlist web link uses your external browser and that website's own privacy practices. Android may independently perform system-level checks or installation scanning.

## Permissions and Android features

Optional notifications support appointment reminders. Native foreground audio provides the sound machine and its playback notification. Background pictures are selected through Android's photo picker; other photo and backup selections use Android's system pickers. The app's emergency copy action writes the chosen text to the Android clipboard. Saved birth-plan dial buttons open the Android phone app for review; they do not call automatically.

## Background pictures

A chosen Today background is copied into SproutBook's private app storage as a resized JPEG. The app reads that private copy offline; it does not upload it or change the original picture. Only the selected picture is copied, rather than importing your photo library.

Removing a picture from a layout or cancelling a draft does not immediately erase every private copy. Previously selected and staged pictures may remain in app storage. Backups include pictures referenced by the saved family records; unused draft copies are not included. Uninstalling the app removes its private local data, including these copies.

## Export and sharing

In-app format-5 backups contain readable family information, Today layouts and referenced photos, including custom backgrounds, encoded as Base64. They are not encrypted by SproutBook. Export only to locations you trust, and keep copies private. Import validates records and photos before requesting replacement of the current local family data. Restored pictures receive fresh private filenames, with their saved references updated.

Uninstalling the app removes its local app data. Export a backup first if you want to keep those records. Removing the app does not delete copies you have exported elsewhere.

## Caregiver mode

Grandparent mode is a read-only convenience that prevents accidental record changes. It is not authentication: a person using an unlocked device can turn it off. Use Android's device lock to protect access to your family's records.

## Public bug reports

GitHub issues are public. Do not upload real family backups, children's photos, medical records, phone numbers or addresses. Describe problems with synthetic data and redact screenshots before posting.
