package com.nothatcher.sproutbook.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nothatcher.sproutbook.R
import com.nothatcher.sproutbook.core.HomeCustomization
import com.nothatcher.sproutbook.services.PhotoStore
import com.nothatcher.sproutbook.ui.*

/** One static, child-owned background. Photo decoding happens off the UI thread, once per file. */
@Composable
internal fun TodayBackdrop(
    childId: String,
    config: HomeCustomization,
    content: @Composable (photoUnavailable: Boolean) -> Unit,
) {
    val context = LocalContext.current
    val background = MaterialTheme.colorScheme.background
    val dark = background.red < .3f
    val photo by produceState<Pair<Boolean, android.graphics.Bitmap?>>(
        true to null, childId, config.backgroundPhoto,
    ) {
        value = false to config.backgroundPhoto?.let { PhotoStore.load(context, it) }
    }
    val unavailable = config.background == "photo" && !photo.first && photo.second == null
    Box(Modifier.fillMaxSize().background(background)) {
        if (config.background == "photo" && photo.second != null) {
            Image(photo.second!!.asImageBitmap(), null, Modifier.fillMaxSize().testTag("today-background-photo"), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(background.copy(alpha = if (dark) .62f else .38f)))
        } else if (config.background != "plain") {
            val resource = when (config.background) {
                "morning" -> R.drawable.woodland_scene_today
                "meadow" -> R.drawable.woodland_scene_care
                "evening" -> R.drawable.woodland_scene_more
                else -> R.drawable.woodland_background
            }
            Image(painterResource(resource), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                alpha = if (config.background == "woodland" || unavailable) {
                    if (dark) .32f else .08f
                } else if (dark) .38f else .48f)
        }
        if (config.background in listOf("woodland", "evening") && LocalWoodlandMotionEnabled.current) {
            WoodlandMotionAsset("woodland-fireflies", Modifier.align(Alignment.TopCenter)
                .padding(top = 24.dp).fillMaxWidth().height(112.dp).alpha(if (dark) .28f else .20f),
                play = true, loop = true, dark = dark)
        }
        content(unavailable)
    }
}
