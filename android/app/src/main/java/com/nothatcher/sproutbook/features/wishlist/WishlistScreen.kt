package com.nothatcher.sproutbook.features.wishlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*
import java.net.URI

@Composable
fun WishlistScreen(vm: FamilyViewModel, state: FamilyState) {
    val child = state.child ?: return
    key(child.id) {
        var limit by rememberSaveable { mutableIntStateOf(200) }
        var filter by rememberSaveable { mutableStateOf("Wanted") }
        val rows by
            remember(child.id, filter, limit) {
                    vm.repo.observeWishlist(child.id, limit + 1, filter)
                }
                .collectAsStateWithLifecycle(emptyList())
        val total by
            remember(child.id) { vm.repo.db.wishlistItems().count(child.id) }
                .collectAsStateWithLifecycle(0)
        val obtained by
            remember(child.id) { vm.repo.db.wishlistItems().obtainedCount(child.id) }
                .collectAsStateWithLifecycle(0)
        var edit by remember { mutableStateOf<WishlistItem?>(null) }
        Page("Wishlist", "Ideas for ${child.name}, kept in one little place") {
            item {
                Panel {
                    Section("${child.name}'s wishlist")
                    Text(
                        "${(total - obtained).coerceAtLeast(0)} wanted · $obtained obtained",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Muted("Save gifts, books, experiences or other ideas for their growing chapter.")
                }
            }
            item {
                Action("Add wishlist item", !state.prefs.grandparent) {
                    edit = WishlistItem(childId = child.id, title = "")
                }
                WishlistFilters(filter) {
                    filter = it
                    limit = 200
                }
            }
            if (rows.isEmpty())
                item {
                    Empty(
                        when (filter) {
                            "Obtained" -> "Their wishes, remembered"
                            "Wanted" -> "Room for a little inspiration"
                            else -> "Their own little wishlist"
                        },
                        when (filter) {
                            "Obtained" -> "Ideas marked obtained will appear here."
                            "Wanted" -> "Add an idea, or choose All to see wishes already obtained."
                            else -> "Add an idea with optional notes and a web link."
                        },
                    )
                }
            items(rows.take(limit), key = { it.id }) { item ->
                Panel {
                    EntryRow(
                        item.title,
                        "${if (item.obtained) "Obtained" else "Wanted"} · ${stamp(item.updatedAt, state.prefs.time24)}",
                    ) {
                        edit = item
                    }
                    if (item.notes.isNotBlank())
                        Text(
                            item.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        )
                    OutlinedButton(
                        onClick = {
                            vm.perform(
                                if (item.obtained) "Wishlist item marked wanted"
                                else "Wishlist item marked obtained"
                            ) {
                                vm.repo.setWishlistObtained(item.id, child.id, !item.obtained)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).semantics {
                            contentDescription =
                                if (item.obtained) "Mark ${item.title} wanted"
                                else "Mark ${item.title} obtained"
                        },
                        enabled = !state.prefs.grandparent,
                    ) {
                        Text(if (item.obtained) "Mark wanted" else "Mark obtained")
                    }
                    if (item.link.isNotBlank()) WishlistWebLink(item.link)
                }
            }
            if (rows.size > limit)
                item { Action("Load more wishlist items") { limit += 200 } }
        }
        edit?.let { item ->
            WishlistEditor(item, rows.any { it.id == item.id }, vm, state.prefs.grandparent) {
                edit = null
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WishlistFilters(selected: String, choose: (String) -> Unit) {
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf("Wanted", "Obtained", "All").forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { choose(option) },
                label = { Text(option) },
                modifier = Modifier.heightIn(min = 48.dp),
            )
        }
    }
}

@Composable
private fun WishlistEditor(
    item: WishlistItem,
    existing: Boolean,
    vm: FamilyViewModel,
    readOnly: Boolean,
    close: () -> Unit,
) {
    var title by rememberSaveable(item.id) { mutableStateOf(item.title) }
    var notes by rememberSaveable(item.id) { mutableStateOf(item.notes) }
    var link by rememberSaveable(item.id) { mutableStateOf(item.link) }
    var obtained by rememberSaveable(item.id) { mutableStateOf(item.obtained) }
    var deleting by remember(item.id) { mutableStateOf(false) }
    LaunchedEffect(readOnly) {
        if (readOnly) {
            deleting = false
            if (!existing) close()
        }
    }
    Editor("Wishlist item", close) {
        if (readOnly) {
            Section(item.title)
            Muted(if (item.obtained) "Obtained" else "Wanted")
            if (item.notes.isNotBlank()) Text(item.notes)
            if (item.link.isNotBlank()) WishlistWebLink(item.link)
        } else {
            Field("Wishlist title", title, { title = it })
            Field("Wishlist notes (optional)", notes, { notes = it }, lines = 3)
            Field("Web link (optional)", link, { link = it })
            Muted("Use a complete https:// or http:// address. Links open in your browser.")
            if (link.isNotBlank()) WishlistWebLink(link)
            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Already obtained", Modifier.weight(1f))
                Switch(
                    checked = obtained,
                    onCheckedChange = { obtained = it },
                    modifier = Modifier.semantics { contentDescription = "Already obtained" },
                )
            }
            Action("Save wishlist item", title.isNotBlank()) {
                vm.perform("Wishlist item saved") {
                    vm.repo.saveWishlist(
                        item.copy(title = title, notes = notes, link = link, obtained = obtained)
                    )
                    close()
                }
            }
            if (existing)
                TextButton(
                    onClick = { deleting = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    Text("Delete wishlist item", color = MaterialTheme.colorScheme.error)
                }
        }
    }
    if (deleting && !readOnly)
        ConfirmDelete("this wishlist item", { deleting = false }) {
            vm.perform("Wishlist item removed") {
                vm.repo.deleteWishlist(item.id, item.childId)
                close()
            }
        }
}

@Composable
private fun WishlistWebLink(link: String) {
    val handler = LocalUriHandler.current
    var error by remember(link) { mutableStateOf<String?>(null) }
    TextButton(
        onClick = {
            val address = link.trim()
            val valid = runCatching {
                val uri = URI(address)
                uri.scheme?.lowercase() in listOf("http", "https") &&
                    !uri.host.isNullOrBlank() && uri.userInfo == null
            }.getOrDefault(false)
            error =
                if (!valid) "Enter a complete http:// or https:// web address."
                else
                    runCatching { handler.openUri(address) }
                        .fold(
                            onSuccess = { null },
                            onFailure = { "No browser available to open this link." },
                        )
        },
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
    ) {
        Text("Open wishlist link")
    }
    error?.let {
        Text(
            it,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}
