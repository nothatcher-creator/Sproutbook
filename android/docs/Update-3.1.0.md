# SproutBook 3.1.0

Four additions extend the offline native app:

- Diapers: wet/dirty/mixed quick logs; dry and earlier changes through a native editor; daily wet/dirty totals; notes, history, editing and deletion.
- Growth journal: recorded weight/height, kg/lb and cm/in conversion, a static trend chart and accessible measurement history. Note-only changes preserve exact stored values and units. Legacy g/m/mm units convert; unrecognized units remain visible and unchanged. No percentiles or diagnosis.
- Prepared bottles: contents, preparation time, amount and notes; record actual amount fed once, or discard; a used bottle links to Feeding without changing its original prepared amount. Preparation history does not estimate storage safety.
- Shopping: per-child list, editable quantities/units/notes, bring in low supplies without duplicates, mark purchased, delete. Linked purchases replenish inventory in the same transaction; repeated completion cannot add stock twice. Save draft quantity/unlink changes before purchase.

Today shows diaper/growth shortcuts and pending bottle/shopping counts. Care exposes all four tools. Feeding, Health and Inventory have connected shortcuts. Read-only caregiver mode disables every mutation, including direct repository writes.

Database migration 9→10 adds three typed tables without changing or deleting existing tables. Growth reuses health records. Backup format 2 includes the new tables; native format 1 imports retain all older records and initialize the new tables empty. Optional inventory/feeding references clear on parent deletion.

Package `com.nothatcher.sproutbook`, version 3.1.0 / 30100. The evaluation APK keeps the 3.0.0 development signing identity. Install as an update to retain local data. Unsigned release APK/AAB are in the source archive and require a private production upload key.

The executed test results and physical-device release gates are recorded in QA.md. Remaining polish: chart axis/date labels, database-side filtering of bottle/shopping histories, broader international digit-entry coverage. Exact dates/values and older records remain accessible in the current history views.
