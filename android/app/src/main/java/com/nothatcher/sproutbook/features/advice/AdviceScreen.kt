package com.nothatcher.sproutbook.features.advice

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.nothatcher.sproutbook.core.*
import com.nothatcher.sproutbook.ui.*

@Composable
fun AdviceScreen() {
    var audience by rememberSaveable { mutableStateOf("Mom") }
    var category by rememberSaveable { mutableStateOf("All") }
    var search by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    val focus = androidx.compose.ui.platform.LocalFocusManager.current
    val articles = remember(audience, category, search) { AdviceCatalog.search(audience, category, search) }
    Page("A little guidance", "Practical support for every parent") {
        item { WoodlandBanner("Room to learn", "Short reads for the moments you need a little support.") }
        item {
            Choices(listOf("Mom", "Dad"), audience) {
                audience = it; category = "All"; expanded = null; search = ""
            }
            Muted("Choose what helps you. Every caregiver is welcome.")
            Field("Search advice", search, { search = it })
            Choices(listOf("All") + AdviceCatalog.articles.filter { it.audience == audience }
                .map { it.category }.distinct(), category) { category = it; expanded = null }
            Muted("${articles.size} short reads · Available offline")
        }
        if (articles.isEmpty()) item { Empty("No matching articles", "Try another word or choose All categories.") }
        items(articles, key = { it.audience + it.title }) { a ->
            val open = expanded == a.title
            Surface(onClick = { focus.clearFocus(); expanded = if (open) null else a.title },
                modifier = Modifier.fillMaxWidth().semantics { stateDescription = if (open) "Expanded" else "Collapsed" },
                shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(a.category.uppercase(), style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                            Text(a.title, style = MaterialTheme.typography.titleMedium)
                        }
                        Text(if (open) "−" else "+", Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    if (open) {
                        a.body.split("\n\n").forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }
                        if (a.sourceLinks.isNotEmpty()) a.sourceLinks.forEach { SourceLink(it.label, it.url) }
                        else if (a.source.isNotBlank()) SourceLink("Read the source", a.source)
                        Text("Tap the card to close", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(a.body.substringBefore(". ") + ".", maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { Muted("General information. Ask your care team about your situation. New article sources checked October 2026.") }
    }
}

@Composable
fun HelpScreen() {
    var selected by remember { mutableStateOf<HelpTopic?>(null) }
    val context = LocalContext.current
    Page("Help right now", "One breath. One next step.") {
        if (selected == null) {
            item {
                Panel {
                    Section("If breathing or consciousness is affected")
                    Text(
                        "Contact emergency services immediately. Do not wait for an app to assess the situation.",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            items(AdviceCatalog.help, key = { it.title }) { h ->
                EntryRow(
                    h.title,
                    if (h.emergency) "Emergency · Get help now" else "Three calm first steps",
                ) {
                    selected = h
                }
            }
        } else {
            val topic = selected!!
            item {
                TextButton(onClick = { selected = null }) { Text("‹ Choose another concern") }
                Text(topic.title, style = MaterialTheme.typography.headlineMedium)
            }
            if (topic.emergency)
                item {
                    Surface(color = MaterialTheme.colorScheme.errorContainer) {
                        Panel {
                            Section("Emergency help comes first")
                            Action("Dial 911 · US / Canada") {
                                runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                                        )
                                    }
                                    .onFailure {
                                        android.widget.Toast.makeText(
                                                context,
                                                "Use your phone to call your local emergency number now.",
                                                android.widget.Toast.LENGTH_LONG,
                                            )
                                            .show()
                                    }
                            }
                            Text(
                                "Elsewhere, call your local emergency number. Stay with your child.",
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            items(topic.steps.indices.toList()) { i ->
                Panel { Section("${i+1}. ${topic.steps[i]}") }
            }
            item {
                Panel {
                    Section("A little more guidance")
                    Text(topic.detail)
                    SourceLink("Read the source", topic.source)
                }
            }
        }
    }
}
