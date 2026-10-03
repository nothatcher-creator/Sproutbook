package com.nothatcher.sproutbook

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nothatcher.sproutbook.navigation.AppRoot
import com.nothatcher.sproutbook.ui.BabyTheme

data class NavigationRequest(val childId: String, val token: Long = System.nanoTime())

class MainActivity : ComponentActivity() {
    private var pending by mutableStateOf<NavigationRequest?>(null)

    private fun receive(intent: Intent) {
        pending =
            if (intent.getStringExtra("destination") == "schedule")
                intent.getStringExtra("childId")?.let { NavigationRequest(it) }
            else null
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        receive(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        receive(intent)
        setContent {
            val vm: FamilyViewModel = viewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            SideEffect {
                enableEdgeToEdge(
                    statusBarStyle =
                        if (state.prefs.dark)
                            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                        else
                            SystemBarStyle.light(
                                android.graphics.Color.TRANSPARENT,
                                android.graphics.Color.TRANSPARENT,
                            ),
                    navigationBarStyle =
                        if (state.prefs.dark)
                            SystemBarStyle.dark(android.graphics.Color.rgb(16, 37, 30))
                        else
                            SystemBarStyle.light(
                                android.graphics.Color.rgb(246, 243, 233),
                                android.graphics.Color.TRANSPARENT,
                            ),
                )
            }
            BabyTheme(state.prefs.dark, state.child?.accent ?: "Forest") {
                AppRoot(vm, state, pending) {
                    pending = null
                    intent.removeExtra("childId")
                    intent.removeExtra("destination")
                }
            }
        }
    }
}
