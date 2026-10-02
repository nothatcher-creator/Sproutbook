package com.nothatcher.sproutbook.features.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*

@Composable
fun InventoryScreen(vm: FamilyViewModel, state: FamilyState, go: (String) -> Unit = {}) {
    val child = state.child ?: return
    val rows by
        remember(child.id) { vm.repo.db.inventorys().observe(child.id, 10000) }
            .collectAsStateWithLifecycle(emptyList())
    var filter by remember { mutableStateOf("All") }
    var search by remember { mutableStateOf("") }
    var edit by remember { mutableStateOf<Inventory?>(null) }
    Page("The family cupboard", "Small supplies, fewer last-minute surprises") {
        item { EntryRow("Shopping list", "Low supplies and your next shop") { go("shopping") } }
        item {
            Action("Add supply", !state.prefs.grandparent) {
                edit = Inventory(childId = child.id, name = "")
            }
            Field("Find supplies", search, { search = it })
            Choices(listOf("All", "Low stock"), filter) { filter = it }
        }
        val visible =
            rows
                .filter {
                    it.name.contains(search, true) &&
                        (filter != "Low stock" || it.quantity <= it.threshold)
                }
                .sortedBy { it.name.lowercase() }
        if (visible.isEmpty())
            item {
                Empty(
                    "A little peace of mind",
                    "Add nappies, wipes, formula or other everyday supplies.",
                )
            }
        items(visible, key = { it.id }) { i ->
            Panel {
                EntryRow(
                    i.name,
                    "${i.quantity.toString().removeSuffix(".0")} ${i.unit} · ${i.category}",
                ) {
                    edit = i
                }
                if (i.quantity <= i.threshold)
                    Text("Low stock · Refill soon", color = MaterialTheme.colorScheme.tertiary)
                Row {
                    OutlinedButton(
                        onClick = {
                            vm.perform("") { vm.repo.adjustInventory(i.id, i.childId, -1.0) }
                        },
                        enabled = !state.prefs.grandparent,
                        modifier =
                            Modifier.weight(1f).semantics {
                                contentDescription = "Use one ${i.name}"
                            },
                    ) {
                        Text("− 1")
                    }
                    TextButton(onClick = { edit = i }, Modifier.weight(1f)) { Text("Details") }
                    OutlinedButton(
                        onClick = {
                            vm.perform("") { vm.repo.adjustInventory(i.id, i.childId, 1.0) }
                        },
                        enabled = !state.prefs.grandparent,
                        modifier =
                            Modifier.weight(1f).semantics {
                                contentDescription = "Add one ${i.name}"
                            },
                    ) {
                        Text("+ 1")
                    }
                }
            }
        }
    }
    edit?.let { i ->
        var name by remember { mutableStateOf(i.name) }
        var quantity by remember { mutableStateOf(i.quantity.toString()) }
        var unit by remember { mutableStateOf(i.unit) }
        var threshold by remember { mutableStateOf(i.threshold.toString()) }
        var category by remember { mutableStateOf(i.category) }
        var deleting by remember { mutableStateOf(false) }
        Editor("Supply details", { edit = null }) {
            if (state.prefs.grandparent) {
                Section(name)
                Text("$quantity $unit · Refill at $threshold")
                Text(category)
            } else {
                Field("Name", name, { name = it })
                Field("Quantity", quantity, { quantity = it }, numeric = true)
                Field("Unit", unit, { unit = it })
                Field("Low-stock threshold", threshold, { threshold = it }, numeric = true)
                Field("Category", category, { category = it })
                Action("Save supply") {
                    vm.perform {
                        vm.repo.saveInventory(
                            i.copy(
                                name = name,
                                quantity = number(quantity, "quantity"),
                                unit = unit,
                                threshold = number(threshold, "threshold"),
                                category = category,
                            )
                        )
                        edit = null
                    }
                }
                if (rows.any { it.id == i.id })
                    TextButton(onClick = { deleting = true }) { Text("Delete supply") }
            }
        }
        if (deleting)
            ConfirmDelete("this supply", { deleting = false }) {
                vm.perform("Supply removed") {
                    vm.repo.write { vm.repo.db.inventorys().delete(i.id, i.childId) }
                    edit = null
                }
            }
    }
}
