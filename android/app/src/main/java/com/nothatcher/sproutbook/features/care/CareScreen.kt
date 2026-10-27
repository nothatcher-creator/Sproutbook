package com.nothatcher.sproutbook.features.care

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nothatcher.sproutbook.FamilyState
import com.nothatcher.sproutbook.ui.*

private data class CareLink(val title: String, val detail: String, val route: String)

private data class CareGroup(val title: String, val links: List<CareLink>)

private val urgent = listOf(
    CareLink("Help right now", "Three calm steps when things feel hard", "help"),
    CareLink("Emergency card", "Offline contacts and essential information", "emergency"),
)

private fun groups(stage: String?) = listOf(
    CareGroup("Everyday care", listOf(
        CareLink("Feeding", "Bottle, breast, pump and formula planning", "feeding"),
        CareLink("Sleep & sound", "Sleep logs and a native sound machine", "sleep"),
        CareLink("Diapers", "Quick changes and today's counts", "diapers"),
        CareLink("Solids & meals", "Food journal, reactions and a weekly plan", "solids"),
        CareLink("Potty journal", "Little visits, at their own pace", "potty"),
        CareLink("Routines & responsibilities", "Flexible rhythms and a dated completion journal", "routines"),
    )),
    CareGroup("Growing up", listOf(
        CareLink("Memory tree", "Little moments, kept together", "memories"),
        CareLink("Milestones", "Celebrate growth in every chapter", "milestones"),
        CareLink("Growth journal", "Weight, height and recorded trends", "growth"),
        CareLink("Little teeth", "A gentle journal of their changing smile", "teeth"),
        CareLink("Pregnancy", "Movements, contractions and birth preparation", "pregnancy"),
    )),
    CareGroup("Health & guidance", listOf(
        CareLink("Health journal", "Measurements, symptoms and visit records", "health"),
        CareLink("Mom & Dad advice", "Practical, searchable support", "advice"),
    )),
    CareGroup("Home & supplies", listOf(
        CareLink("Milk freezer", "Containers, supply and saved pump sessions", "milk"),
        CareLink("Prepared bottles", "Preparation linked to feeding", "bottles"),
        CareLink("Family cupboard", "Supplies and low-stock reminders", "inventory"),
        CareLink("Shopping list", "Bring low supplies into your next shop", "shopping"),
        CareLink("Wishlist", "Ideas, gifts and little wishes for this child", "wishlist"),
    )),
).map { group ->
    // Lead each group with what matters in this chapter; nothing is hidden.
    group.copy(links = group.links.sortedBy { if (it.route in priority(stage)) 0 else 1 })
}

private fun priority(stage: String?) = when (stage) {
    "PREGNANCY" -> setOf("pregnancy", "advice", "health")
    "BABY" -> setOf("feeding", "sleep", "diapers", "memories", "growth")
    "TODDLER" -> setOf("solids", "potty", "sleep", "milestones", "teeth")
    else -> setOf("routines", "milestones", "health", "memories")
}

@Composable
fun CareScreen(state: FamilyState, go: (String) -> Unit) {
    Page("Care", "A helping hand for every chapter") {
        urgent.forEach { link -> item(key = link.route) { EntryRow(link.title, link.detail) { go(link.route) } } }
        groups(state.child?.stage).forEach { group ->
            item(key = "group-${group.title}") {
                Text(group.title, Modifier.padding(top = 10.dp, start = 4.dp).semantics { heading() },
                    style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            }
            group.links.forEach { link -> item(key = link.route) { EntryRow(link.title, link.detail) { go(link.route) } } }
        }
    }
}
