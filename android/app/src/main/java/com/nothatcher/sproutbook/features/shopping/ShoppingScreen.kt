package com.nothatcher.sproutbook.features.shopping

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*

@Composable
fun ShoppingScreen(vm: FamilyViewModel, state: FamilyState, go: (String) -> Unit) {
    val child = state.child ?: return
    var limit by rememberSaveable { mutableIntStateOf(200) }
    var filter by rememberSaveable { mutableStateOf("To buy") }
    val rows by
        remember(child.id, limit) { vm.repo.db.shoppingItems().observe(child.id, limit + 1) }
            .collectAsStateWithLifecycle(emptyList())
    var edit by remember { mutableStateOf<ShoppingItem?>(null) }
    Page("Shopping list", "A little planning, fewer things to remember") {
        item {
            Action("Add shopping item", !state.prefs.grandparent) {
                edit = ShoppingItem(childId = child.id, name = "")
            }
            OutlinedButton(
                onClick = {
                    vm.perform("Low supplies added to shopping") { vm.repo.addLowStock(child.id) }
                },
                enabled = !state.prefs.grandparent,
            ) {
                Text("Add low supplies")
            }
            Muted(
                "Low supplies are linked to your cupboard. Marking them purchased replenishes stock once. The suggested quantity brings stock above its low threshold; edit it to match your purchase."
            )
            Choices(listOf("To buy", "Purchased", "All"), filter) { filter = it }
        }
        val shown =
            rows.take(limit).filter { filter == "All" || it.checked == (filter == "Purchased") }
        if (shown.isEmpty())
            item {
                Empty(
                    if (filter == "Purchased") "Past shopping" else "One less thing on your mind",
                    "Add items or bring in low supplies from your family cupboard.",
                )
            }
        items(shown, key = { it.id }) { s ->
            EntryRow(
                s.name,
                "${s.quantity.toString().removeSuffix(".0")} ${s.unit} · ${if(s.checked) "Purchased" else "To buy"}${if(s.inventoryId != null) " · Linked supply" else ""}",
            ) {
                edit = s
            }
        }
        if (rows.size > limit) item { Action("Load older shopping items") { limit += 200 } }
        item { EntryRow("Family cupboard", "Supplies and current quantities") { go("inventory") } }
    }
    edit?.let { s ->
        var name by remember(s.id) { mutableStateOf(s.name) }
        var quantity by remember(s.id) { mutableStateOf(s.quantity.toString().removeSuffix(".0")) }
        var unit by remember(s.id) { mutableStateOf(s.unit) }
        var notes by remember(s.id) { mutableStateOf(s.notes) }
        var unlink by remember(s.id) { mutableStateOf(false) }
        var deleting by remember { mutableStateOf(false) }
        val existing = rows.any { it.id == s.id }
        val dirty =
            name != s.name ||
                quantity.replace(',', '.').toDoubleOrNull() != s.quantity ||
                unit != s.unit ||
                notes != s.notes ||
                unlink
        Editor("Shopping item", { edit = null }) {
            if (state.prefs.grandparent || s.checked) {
                Section(s.name)
                Text("${s.quantity} ${s.unit}")
                Text(s.notes)
                if (s.checked) Muted("Purchased · add a new item for your next shop.")
            } else {
                Field("Item name", name, { name = it })
                Field("Quantity to buy", quantity, { quantity = it }, numeric = true)
                if (s.inventoryId == null || unlink) Field("Shopping unit", unit, { unit = it })
                else {
                    Muted("Linked supply · ${s.unit}")
                    TextButton(onClick = { unlink = true }) { Text("Unlink supply") }
                }
                Field("Shopping notes", notes, { notes = it }, lines = 2)
                Action("Save shopping item") {
                    vm.perform("Shopping item saved") {
                        vm.repo.saveShopping(
                            s.copy(
                                name = name,
                                quantity = quantity.replace(',', '.').toDoubleOrNull() ?: 0.0,
                                unit = unit,
                                notes = notes,
                                inventoryId = if (unlink) null else s.inventoryId,
                            )
                        )
                        edit = null
                    }
                }
                if (existing && dirty)
                    Muted("Save shopping changes before marking this item purchased.")
                if (existing)
                    Action("Mark purchased", !dirty) {
                        vm.perform(
                            if (s.inventoryId != null) "Purchased · cupboard restocked"
                            else "Purchased"
                        ) {
                            vm.repo.buyShopping(s.id, child.id)
                            edit = null
                        }
                    }
            }
            if (existing && !state.prefs.grandparent)
                TextButton(onClick = { deleting = true }) { Text("Delete shopping item") }
        }
        if (deleting)
            ConfirmDelete("this shopping item", { deleting = false }) {
                vm.perform("Shopping item removed") {
                    vm.repo.write { vm.repo.db.shoppingItems().delete(s.id, child.id) }
                    edit = null
                }
            }
    }
}
