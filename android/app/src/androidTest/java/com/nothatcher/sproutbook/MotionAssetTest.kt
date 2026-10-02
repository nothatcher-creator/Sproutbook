package com.nothatcher.sproutbook

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.test.core.app.ApplicationProvider
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.RenderMode
import org.junit.Assert.*
import org.junit.Test

/** Real Android parsing and rasterization, not just an archive/JSON syntax check. */
class MotionAssetTest {
    @Test fun everyThemeAssetParsesRendersAndHasAStaticFallback() {
        val context = ApplicationProvider.getApplicationContext<BabyForgeApp>()
        var count = 0
        for (theme in listOf("dark", "light")) {
            val names = requireNotNull(context.assets.list("sproutbook-motion/lottie/$theme"))
            assertEquals(46, names.size)
            for (name in names) {
                val path = "sproutbook-motion/lottie/$theme/$name"
                val result = LottieCompositionFactory.fromAssetSync(context, path)
                assertNull("$path failed native parsing: ${result.exception}", result.exception)
                val composition = requireNotNull(result.value)
                assertTrue("$path is empty", composition.layers.isNotEmpty())
                assertTrue("$path has invalid duration", composition.duration > 0)
                val bitmap = Bitmap.createBitmap(192, 192, Bitmap.Config.ARGB_8888)
                val drawable = LottieDrawable().apply {
                    setComposition(composition)
                    renderMode = RenderMode.SOFTWARE
                    setBounds(0, 0, 192, 192)
                }
                var visible = false
                for (frame in listOf(0f, .33f, .66f, .99f)) {
                    bitmap.eraseColor(0)
                    drawable.progress = frame
                    drawable.draw(Canvas(bitmap))
                    val pixels = IntArray(192 * 192)
                    bitmap.getPixels(pixels, 0, 192, 0, 0, 192, 192)
                    visible = visible || pixels.any { (it ushr 24) > 0 }
                }
                assertTrue("$path drew no pixels", visible)
                drawable.clearComposition()
                bitmap.recycle()
                val fallback = context.assets.open("sproutbook-motion/png/$theme/${name.removeSuffix(".json")}.png")
                    .use { requireNotNull(BitmapFactory.decodeStream(it)) }
                assertTrue(fallback.width > 0 && fallback.height > 0)
                fallback.recycle()
                count++
            }
        }
        assertEquals(92, count)
    }
}
