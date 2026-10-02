package com.nothatcher.sproutbook.features.sleep

import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.*
import androidx.lifecycle.repeatOnLifecycle
import com.nothatcher.sproutbook.core.SoundCatalog
import com.nothatcher.sproutbook.services.SoundService
import com.nothatcher.sproutbook.ui.*
import kotlinx.coroutines.delay

@Composable
fun SoundPanel(
    kind: String, volume: Float, minutes: Int,
    onKind: (String) -> Unit, onVolume: (Float) -> Unit, onMinutes: (Int) -> Unit,
) {
    val context = LocalContext.current
    val state by SoundService.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.kind, state.volume, state.minutes, state.active) {
        if (state.active) { onKind(state.kind); onVolume(state.volume); onMinutes(state.minutes) }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val now by produceState(SystemClock.elapsedRealtime(), state.playing, state.clock, lifecycle) {
        value = SystemClock.elapsedRealtime()
        if (state.playing && state.clock.deadlineMs != null) lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) { value = SystemClock.elapsedRealtime(); delay(1000) }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        WoodlandBanner("A softer soundtrack", "A little calm, wherever you are.", night = true)
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                SoundGlyph(kind, Modifier.size(42.dp), MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Section("$kind sound")
                    Muted(when {
                        state.preparing -> "Preparing…"
                        state.paused -> "Paused · timer paused too"
                        state.playing -> "Playing · continues with screen off"
                        else -> SoundCatalog.description(kind)
                    })
                }
            }
            val left = state.clock.remaining(now)
            Text(if (!state.active) { if (minutes == 0) "Continuous" else "$minutes minute timer" }
                else if (left == null) "Continuous" else "${(left + 999) / 60000}:${(((left + 999) / 1000) % 60).toString().padStart(2, '0')} remaining",
                style = MaterialTheme.typography.headlineMedium)
            Action(when {
                state.preparing -> "Preparing sound…"
                state.paused -> "Resume ${state.kind.lowercase()} sound"
                state.playing -> "Pause ${state.kind.lowercase()} sound"
                else -> "Play ${kind.lowercase()} sound"
            }, !state.preparing) {
                when {
                    state.paused -> SoundService.resume(context)
                    state.playing -> SoundService.pause(context)
                    else -> SoundService.play(context, kind, volume, minutes)
                }
            }
            if (state.active) OutlinedButton(onClick = { SoundService.stop(context) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                WoodlandIcon(com.nothatcher.sproutbook.R.drawable.woodland_stop)
                Spacer(Modifier.width(8.dp))
                Text("Stop ${state.kind.lowercase()} sound")
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        Section("Choose your backdrop")
        SoundCatalog.kinds.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { sound ->
                    val selected = kind == sound
                    Surface(onClick = {
                        onKind(sound)
                        if (state.active) SoundService.play(context, sound, volume, minutes, preserve = true)
                    }, enabled = !state.preparing, modifier = Modifier.weight(1f)
                        .semantics { this.selected = selected }, shape = RoundedCornerShape(18.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null) {
                        Row(Modifier.padding(14.dp).heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SoundGlyph(sound, Modifier.size(28.dp), MaterialTheme.colorScheme.primary)
                            Text(sound, style = MaterialTheme.typography.titleSmall)
                            if (selected) Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
        Panel {
            Section("Make it yours")
            Choices(SoundCatalog.timers.map { if (it == 0) "Continuous" else "$it min" },
                if (minutes == 0) "Continuous" else "$minutes min") {
                val chosen = it.substringBefore(" ").toIntOrNull() ?: 0
                onMinutes(chosen)
                SoundService.timer(context, chosen)
            }
            Muted("Choosing a timer starts a new countdown. Pause freezes it.")
            Text("Volume · ${(volume * 100).toInt()}%")
            Slider(volume, { onVolume(it); SoundService.volume(context, it) },
                Modifier.semantics { contentDescription = "Sound volume" })
            Muted("Keep volume quiet and the device well away from the cot. These are generated sound textures; use what feels comfortable.")
        }
    }
}
