package com.nothatcher.sproutbook.features.feeding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.nothatcher.sproutbook.FamilyState
import com.nothatcher.sproutbook.core.FeedingMath
import com.nothatcher.sproutbook.ui.*

@Composable
fun FormulaScreen(state: FamilyState) {
    var weight by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(state.prefs.weightUnit) }
    var output by remember { mutableStateOf(state.prefs.volumeUnit) }
    var feeds by remember { mutableStateOf("8") }
    var applicable by remember { mutableStateOf(false) }
    val estimate =
        remember(weight, unit, feeds) {
            runCatching {
                val raw = number(weight, "weight")
                FeedingMath.estimate(if (unit == "kg") raw else FeedingMath.kg(raw), feeds.toInt())
            }
        }
    Page("Formula planning", "A rough estimate for supplies") {
        item {
            Panel {
                Muted(
                    "For healthy, full-term babies from about the end of the first week to six months. Use an individual feeding plan if your care team has given one. This is not a feeding target or medical instruction."
                )
                Row {
                    Checkbox(applicable, { applicable = it })
                    Text("This general guide applies to my baby", Modifier.weight(1f))
                }
            }
        }
        item {
            Field("Baby's weight", weight, { weight = it }, numeric = true)
            Choices(listOf("kg", "lb"), unit) { unit = it }
            Field("Feeds per day", feeds, { feeds = it }, numeric = true)
            Choices(listOf("mL", "fl oz"), output) { output = it }
        }
        if (applicable && estimate.isSuccess) {
            val range = estimate.getOrThrow()
            val n = feeds.toInt()
            item {
                Panel {
                    Section("Rough daily estimate")
                    Text(
                        "${volume(range.first,output)} – ${volume(range.second,output)}",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Section("Rough amount per feed")
                    Text("${volume(range.first/n,output)} – ${volume(range.second/n,output)}")
                    Muted(
                        "Based on 150–200 mL/kg/day, divided by your chosen feed count. Actual needs and feed sizes vary."
                    )
                }
            }
        } else if (applicable && weight.isNotBlank())
            item {
                Muted(
                    "Check the weight and feed count. For very small babies or needs outside this guide, ask your care team."
                )
            }
        item {
            Panel {
                Section("Follow your baby")
                Text(
                    "Offer feeds responsively and stop when baby shows they are done. Never force them to finish a bottle. Follow preparation instructions exactly; do not dilute or concentrate formula."
                )
                Muted("Ounces here mean US fluid ounces.")
                SourceLink(
                    "Formula guidance · NHS Wales",
                    "https://111.wales.nhs.uk/livewell/pregnancy/bottleformulacommonquest/",
                )
            }
        }
    }
}
