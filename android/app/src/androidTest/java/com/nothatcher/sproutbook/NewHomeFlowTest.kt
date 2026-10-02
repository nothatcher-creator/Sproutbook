package com.nothatcher.sproutbook

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import com.nothatcher.sproutbook.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Test

class NewHomeFlowTest : FlowFixture() {
    @Test
    fun liveDashboardLinksAndAccessibleRecordedChart() {
        runBlocking {
            app.settings.string("weightUnit", "kg")
            app.settings.string("volumeUnit", "mL")
            val now = System.currentTimeMillis()
            app.repository.saveHealth(
                HealthRecord(
                    childId = "qa-child",
                    kind = "Weight",
                    recordedAt = now - 7 * 86400000L,
                    title = "Weight",
                    value = "3.215",
                    unit = "kg",
                )
            )
            app.repository.saveHealth(
                HealthRecord(
                    childId = "qa-child",
                    kind = "Weight",
                    recordedAt = now,
                    title = "Weight",
                    value = "4.5",
                    unit = "kg",
                )
            )
            app.repository.saveBottlePrep(
                BottlePrep(childId = "qa-child", preparedAt = now, amountMl = 90.0)
            )
            app.repository.saveShopping(
                ShoppingItem(childId = "qa-child", name = "Wipes", quantity = 2.0, unit = "packs")
            )
        }
        pageClick("1 prepared bottles")
        waitText("From preparation to feeding, kept together")
        compose.onNodeWithTag("nav-today").performClick()
        pageClick("Shopping · 1 to buy")
        waitText("A little planning, fewer things to remember")
        compose.onNodeWithTag("nav-today").performClick()
        pageClick("Log diaper")
        waitText("A quick log for little changes")
        compose.onNodeWithTag("nav-today").performClick()
        pageClick("Growth journal")
        compose
            .onNode(hasContentDescription("Recorded trend in kg. 2 measurements", substring = true))
            .performScrollTo()
            .assertExists()
        val image = compose.onRoot().captureToImage().asAndroidBitmap()
        val output = java.io.File(app.getExternalFilesDir(null), "qa-growth-v31.png")
        output.outputStream().use {
            image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
