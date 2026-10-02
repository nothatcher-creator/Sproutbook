package com.nothatcher.sproutbook.ui

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.*

@Composable
fun rememberNow(active: Boolean = true, interval: Long = 1000): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(active, interval, owner) {
        if (active)
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    now = System.currentTimeMillis()
                    delay(interval)
                }
            }
    }
    return now
}

fun durationText(millis: Long): String {
    val seconds = (millis / 1000).coerceAtLeast(0)
    return when {
        seconds >= 3600 -> "${seconds/3600}h ${(seconds%3600)/60}m"
        seconds >= 60 -> "${seconds/60}m ${seconds%60}s"
        else -> "${seconds}s"
    }
}

fun number(value: String, label: String, optional: Boolean = false): Double {
    if (optional && value.isBlank()) return 0.0
    return value.replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }
        ?: throw IllegalArgumentException("Enter a valid $label.")
}
