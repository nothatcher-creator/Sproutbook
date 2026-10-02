package com.nothatcher.sproutbook.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.hideFromAccessibility
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.*

/** Observability for UI tests; never spoken as a standard accessibility state. */
val WoodlandPlaybackState = SemanticsPropertyKey<String>("Woodland playback")

private object WoodlandStills {
    private val cache = object : LruCache<String, Bitmap>(2 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount
    }
    fun load(context: Context, path: String): Bitmap = synchronized(cache) {
        cache.get(path) ?: context.assets.open(path).use { input ->
            requireNotNull(BitmapFactory.decodeStream(input)).also { cache.put(path, it) }
        }
    }
}

/** Native, local shape animation. Never creates a frame clock for still icons or offscreen art. */
@Composable
fun WoodlandMotionAsset(
    id: String,
    modifier: Modifier = Modifier,
    play: Boolean = false,
    loop: Boolean = false,
    replay: Int = 0,
    dark: Boolean = true,
    tint: Color? = null,
    description: String? = null,
) {
    val theme = if (dark) "dark" else "light"
    val context = LocalContext.current
    val view = LocalView.current
    val still = remember(id, theme) {
        WoodlandStills.load(context, "sproutbook-motion/png/$theme/$id.png").asImageBitmap()
    }
    var visible by remember { mutableStateOf(false) }
    val canPlay = LocalWoodlandMotionEnabled.current && visible && play
    val positioned = modifier.onGloballyPositioned { coordinates ->
        visible = coordinates.boundsInWindow().overlaps(Rect(0f, 0f, view.width.toFloat(), view.height.toFloat()))
    }
    // Removing this subtree cancels the animation immediately on every policy/visibility gate.
    if (canPlay) key(id, theme, replay) {
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("sproutbook-motion/lottie/$theme/$id.json"))
        val animation = animateLottieCompositionAsState(composition,
            iterations = if (loop) LottieConstants.IterateForever else 1,
            useCompositionFrameRate = true)
        val properties = tint?.let {
            rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.COLOR, it.toArgb(), "**"),
                rememberLottieDynamicProperty(LottieProperty.STROKE_COLOR, it.toArgb(), "**"))
        }
        if (composition == null) {
            Image(still, description, positioned.testTag("motion-$id").semantics {
                    this[WoodlandPlaybackState] = "Still"
                    if (description == null) hideFromAccessibility()
                },
                colorFilter = tint?.let(ColorFilter::tint))
        } else {
            LottieAnimation(composition, progress = { animation.progress },
                modifier = positioned.testTag("motion-$id").semantics {
                    this[WoodlandPlaybackState] = if (animation.isPlaying) "Animated" else "Still"
                    if (description == null) hideFromAccessibility()
                    if (description != null) contentDescription = description
                }, dynamicProperties = properties)
        }
    } else {
        Image(still, description, positioned.testTag("motion-$id").semantics {
                    this[WoodlandPlaybackState] = "Still"
                    if (description == null) hideFromAccessibility()
                },
            colorFilter = tint?.let(ColorFilter::tint))
    }
}
