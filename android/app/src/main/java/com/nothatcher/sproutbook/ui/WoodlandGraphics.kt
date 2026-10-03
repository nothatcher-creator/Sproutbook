package com.nothatcher.sproutbook.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nothatcher.sproutbook.R

/** Decorative vector paths leave the native labels as the accessible action names. */
@Composable
fun WoodlandIcon(@DrawableRes resource: Int, description: String? = null,
    modifier: Modifier = Modifier, tint: Color = LocalContentColor.current,
    animated: Boolean = false, loop: Boolean = false, replay: Int = 0) {
    val context = LocalContext.current
    val name = remember(resource) { context.resources.getResourceEntryName(resource).removePrefix("woodland_") }
    val supported = name in setOf("add", "advice", "back", "care", "clock", "delete", "diapers", "feeding",
        "growth", "health", "help", "inventory", "memory", "milestones", "more", "next", "pause", "photo",
        "play", "pregnancy", "profile", "pump", "routine", "save", "schedule", "settings", "shopping",
        "sleep", "solids", "sound", "stop", "teeth", "today")
    if (supported) WoodlandMotionAsset("icon-$name", modifier.size(24.dp), play = animated,
        loop = loop, replay = replay, dark = MaterialTheme.colorScheme.background.red < .3f,
        tint = tint, description = description)
    else Icon(painterResource(resource), description, modifier.size(24.dp), tint = tint)
}

@DrawableRes
fun woodlandIconFor(label: String): Int {
    val text = label.lowercase()
    return when {
        text.startsWith("play") || text.startsWith("resume") -> R.drawable.woodland_play
        text.startsWith("pause") -> R.drawable.woodland_pause
        text.startsWith("stop") -> R.drawable.woodland_stop
        text.startsWith("delete") || text.startsWith("remove") -> R.drawable.woodland_delete
        text.startsWith("save") || text.startsWith("complete") -> R.drawable.woodland_save
        text.startsWith("add") || text.startsWith("create") -> R.drawable.woodland_add
        "sleep" in text || "nap" in text -> R.drawable.woodland_sleep
        "pump" in text || "milk" in text -> R.drawable.woodland_pump
        "feed" in text || "bottle" in text || "formula" in text -> R.drawable.woodland_feeding
        "memor" in text || "tree" in text -> R.drawable.woodland_memory
        "help" in text || "emergency" in text -> R.drawable.woodland_help
        "teeth" in text || "tooth" in text -> R.drawable.woodland_teeth
        "milestone" in text -> R.drawable.woodland_milestones
        "stock" in text || "cupboard" in text || "invent" in text -> R.drawable.woodland_inventory
        "meal" in text || "solid" in text || "food" in text -> R.drawable.woodland_solids
        "diaper" in text || "potty" in text -> R.drawable.woodland_diapers
        "health" in text -> R.drawable.woodland_health
        "advice" in text || "mom" in text || "dad" in text -> R.drawable.woodland_advice
        "pregnan" in text || "kick" in text || "birth" in text -> R.drawable.woodland_pregnancy
        "growth" in text -> R.drawable.woodland_growth
        "routine" in text -> R.drawable.woodland_routine
        "shopping" in text || "wishlist" in text -> R.drawable.woodland_shopping
        "photo" in text -> R.drawable.woodland_photo
        "sound" in text -> R.drawable.woodland_sound
        "schedule" in text || "appointment" in text || "up next" in text -> R.drawable.woodland_schedule
        "backup" in text || "diagnostic" in text || "setting" in text -> R.drawable.woodland_settings
        else -> R.drawable.woodland_profile
    }
}

@Composable
fun WoodlandHeader(title: String, subtitle: String) {
    val art = when (title) {
        "Today", "A place to grow" -> R.drawable.woodland_scene_today
        "Schedule" -> R.drawable.woodland_scene_schedule
        "Care" -> R.drawable.woodland_scene_care
        "Your family" -> R.drawable.woodland_scene_more
        else -> null
    }
    if (art != null) PaintedBanner(title, subtitle, art)
    else Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        WoodlandIcon(woodlandIconFor(title), modifier = Modifier.padding(top = 6.dp),
            tint = MaterialTheme.colorScheme.primary, animated = true)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineLarge)
            if (subtitle.isNotEmpty()) Muted(subtitle)
        }
    }
}

/** Paintings are separate from scalable, searchable, screen-reader-accessible native text. */
@Composable
fun PaintedBanner(title: String, detail: String, @DrawableRes art: Int) {
    Box(Modifier.fillMaxWidth().heightIn(min = 128.dp).clip(RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.CenterStart) {
        Image(painterResource(art), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
        Box(Modifier.matchParentSize().background(Brush.horizontalGradient(
            listOf(Color(0xFF10261C).copy(alpha = .88f), Color(0xFF10261C).copy(alpha = .84f),
                Color(0xFF10261C).copy(alpha = .78f)))))
        val accent = when (title) {
            "Today" -> "brand-sprout-book"
            "Schedule" -> "woodland-butterfly"
            "Care" -> "milestone-bloom"
            else -> "brand-sprout-book"
        }
        WoodlandMotionAsset(accent, Modifier.align(Alignment.CenterEnd).padding(end = 12.dp).size(54.dp),
            play = true, loop = true, dark = true)
        Column(Modifier.fillMaxWidth().padding(18.dp).padding(end = 64.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = Color(0xFFF5F2E7), style = MaterialTheme.typography.headlineLarge)
            if (detail.isNotEmpty()) Text(detail, color = Color(0xFFC2CEBF),
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}
