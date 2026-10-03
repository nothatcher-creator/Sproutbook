package com.nothatcher.sproutbook.features.care

import androidx.compose.runtime.Composable
import com.nothatcher.sproutbook.FamilyState
import com.nothatcher.sproutbook.ui.*

@Composable
fun CareScreen(state: FamilyState, go: (String) -> Unit) {
    Page("Care", "A helping hand for every chapter") {
        item { EntryRow("Help right now", "Three calm steps when things feel hard") { go("help") } }
        item {
            EntryRow("Emergency card", "Offline contacts and essential information") {
                go("emergency")
            }
        }
        item {
            EntryRow("Health journal", "Measurements, symptoms and visit records") { go("health") }
        }
        item {
            EntryRow(
                "Routines & responsibilities",
                "Flexible rhythms and a dated completion journal",
            ) {
                go("routines")
            }
        }
        item { EntryRow("Growth journal", "Weight, height and recorded trends") { go("growth") } }
        item { EntryRow("Diapers", "Quick changes and today's counts") { go("diapers") } }
        item { EntryRow("Potty journal", "Little visits, at their own pace") { go("potty") } }
        item {
            EntryRow("Milk freezer", "Containers, supply and saved pump sessions") { go("milk") }
        }
        item { EntryRow("Prepared bottles", "Preparation linked to feeding") { go("bottles") } }
        item {
            EntryRow("Shopping list", "Bring low supplies into your next shop") { go("shopping") }
        }
        item { EntryRow("Wishlist", "Ideas, gifts and little wishes for this child") { go("wishlist") } }
        item { EntryRow("Family cupboard", "Supplies and low-stock reminders") { go("inventory") } }
        item { EntryRow("Mom & Dad advice", "Practical, searchable support") { go("advice") } }
        item {
            EntryRow("Pregnancy", "Movements, contractions and birth preparation") {
                go("pregnancy")
            }
        }
        item { EntryRow("Feeding", "Bottle, breast, pump and formula planning") { go("feeding") } }
        item { EntryRow("Sleep & sound", "Sleep logs and a native sound machine") { go("sleep") } }
        item {
            EntryRow("Solids & meals", "Food journal, reactions and a weekly plan") { go("solids") }
        }
        item {
            EntryRow("Little teeth", "A gentle journal of their changing smile") { go("teeth") }
        }
        item { EntryRow("Milestones", "Celebrate growth in every chapter") { go("milestones") } }
        item { EntryRow("Memory tree", "Little moments, kept together") { go("memories") } }
    }
}
