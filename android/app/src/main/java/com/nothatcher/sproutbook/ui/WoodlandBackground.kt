package com.nothatcher.sproutbook.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nothatcher.sproutbook.R

/** Existing static painting with one restrained native firefly overlay, only on main pages. */
@Composable
fun WoodlandBackground(mainPage: Boolean) {
    val background = MaterialTheme.colorScheme.background
    val dark = background.red < .3f
    Box(Modifier.fillMaxSize().background(background)) {
        Image(painterResource(R.drawable.woodland_background), null, Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop, alpha = if (dark) .32f else .08f)
        if (mainPage && LocalWoodlandMotionEnabled.current) {
            WoodlandMotionAsset("woodland-fireflies", Modifier.align(Alignment.TopCenter)
                .padding(top = 88.dp).fillMaxWidth().height(112.dp).alpha(if (dark) .28f else .20f),
                play = true, loop = true, dark = dark)
        }
    }
}
