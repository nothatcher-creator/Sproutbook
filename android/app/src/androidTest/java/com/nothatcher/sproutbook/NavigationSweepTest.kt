package com.nothatcher.sproutbook

import androidx.compose.ui.test.*
import org.junit.Test

class NavigationSweepTest : FlowFixture() {
    @Test
    fun everyCareDestinationAndSettingsToolsOpen() {
        listOf(
                "Help right now" to "One breath. One next step.",
                "Emergency card" to "Always available on this device, even offline",
                "Health journal" to "Observations and records for better care conversations",
                "Diapers" to "A quick log for little changes",
                "Growth journal" to "Their measurements, one growing story",
                "Prepared bottles" to "From preparation to feeding, kept together",
                "Shopping list" to "A little planning, fewer things to remember",
                "Family cupboard" to "The family cupboard",
                "Mom & Dad advice" to "A little guidance",
                "Pregnancy" to "Waiting for you",
                "Feeding" to "Bottle, breast and pump, in one rhythm",
                "Sleep & sound" to "Rest & rhythm",
                "Solids & meals" to "Little tastes",
                "Little teeth" to "Tap to view · Hold a tooth to change its stage",
                "Milestones" to "Growing, their way",
                "Memory tree" to "Growing memories",
            )
            .forEach { (label, destination) ->
                android.util.Log.i("NavigationSweep", "Opening $label")
                care(label)
                waitText(destination)
                back()
            }
        compose.onNodeWithTag("nav-more").performClick()
        pageClick("Backup & import")
        waitText("Keep their story safe")
        back()
        pageClick("Diagnostics")
        waitText("Device storage and build information")
    }
}
