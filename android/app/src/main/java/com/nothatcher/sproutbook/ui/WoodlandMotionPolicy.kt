package com.nothatcher.sproutbook.ui

import android.content.*
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nothatcher.sproutbook.core.WoodlandMotion

/** A single observer for all decorative assets. Defaults to still outside the app provider. */
val LocalWoodlandMotionEnabled = staticCompositionLocalOf { false }

@Composable
fun WoodlandMotionProvider(reduceMotion: Boolean, allowMotion: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val power = remember(context) { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    var resumed by remember(lifecycle) { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    var batterySaver by remember { mutableStateOf(power.isPowerSaveMode) }
    // Read the actual setting: ValueAnimator's cached value can lag a live accessibility change.
    fun animationsEnabled() = Settings.Global.getFloat(context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    var systemAnimators by remember { mutableStateOf(animationsEnabled()) }
    DisposableEffect(lifecycle, context) {
        fun refresh() { batterySaver = power.isPowerSaveMode; systemAnimators = animationsEnabled() }
        val observer = LifecycleEventObserver { _, _ ->
            resumed = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            if (resumed) refresh()
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) = refresh()
        }
        val settings = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) = refresh()
        }
        lifecycle.addObserver(observer)
        ContextCompat.registerReceiver(context, receiver, IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED)
        context.contentResolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, settings)
        onDispose {
            lifecycle.removeObserver(observer)
            context.unregisterReceiver(receiver)
            context.contentResolver.unregisterContentObserver(settings)
        }
    }
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val enabled = WoodlandMotion.shouldAnimate(reduceMotion, resumed, batterySaver, systemAnimators,
        allowMotion, keyboardVisible, LocalWindowInfo.current.isWindowFocused)
    CompositionLocalProvider(LocalWoodlandMotionEnabled provides enabled, content = content)
}
