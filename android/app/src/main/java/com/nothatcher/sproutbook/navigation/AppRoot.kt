package com.nothatcher.sproutbook.navigation

import androidx.compose.foundation.layout.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.nothatcher.sproutbook.core.HomeCustomization
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.R
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.features.advice.*
import com.nothatcher.sproutbook.features.backup.*
import com.nothatcher.sproutbook.features.bottles.BottlePrepScreen
import com.nothatcher.sproutbook.features.care.CareScreen
import com.nothatcher.sproutbook.features.diapers.DiaperScreen
import com.nothatcher.sproutbook.features.emergency.EmergencyScreen
import com.nothatcher.sproutbook.features.feeding.*
import com.nothatcher.sproutbook.features.growth.GrowthScreen
import com.nothatcher.sproutbook.features.health.HealthScreen
import com.nothatcher.sproutbook.features.home.*
import com.nothatcher.sproutbook.features.inventory.InventoryScreen
import com.nothatcher.sproutbook.features.memories.MemoryScreen
import com.nothatcher.sproutbook.features.milestones.MilestoneScreen
import com.nothatcher.sproutbook.features.milk.MilkScreen
import com.nothatcher.sproutbook.features.potty.PottyScreen
import com.nothatcher.sproutbook.features.pregnancy.PregnancyScreen
import com.nothatcher.sproutbook.features.profile.*
import com.nothatcher.sproutbook.features.routines.RoutineScreen
import com.nothatcher.sproutbook.features.schedule.ScheduleScreen
import com.nothatcher.sproutbook.features.shopping.ShoppingScreen
import com.nothatcher.sproutbook.features.wishlist.WishlistScreen
import com.nothatcher.sproutbook.features.sleep.SleepScreen
import com.nothatcher.sproutbook.features.solids.*
import com.nothatcher.sproutbook.features.teeth.TeethScreen
import com.nothatcher.sproutbook.ui.*

@Composable
fun AppRoot(
    vm: FamilyViewModel,
    state: FamilyState,
    pending: NavigationRequest? = null,
    onHandled: () -> Unit = {},
) {
    val nav = rememberNavController()
    val back by nav.currentBackStackEntryAsState()
    val route = back?.destination?.route ?: "today"
    LaunchedEffect(pending, state.child?.id, state.loading, back) {
        if (pending != null && !state.loading) {
            if (state.children.none { it.id == pending.childId }) {
                vm.message("This child is no longer on this device.")
                onHandled()
            } else if (state.child?.id != pending.childId) vm.select(pending.childId)
            else if (back != null) {
                nav.navigate("schedule") { launchSingleTop = true }
                onHandled()
            }
        }
    }
    val snackbar = remember { SnackbarHostState() }
    var switcher by remember { mutableStateOf(false) }
    var adding by remember { mutableStateOf(false) }
    var organizerPreview by remember(state.child?.id) { mutableStateOf<HomeCustomization?>(null) }
    LaunchedEffect(route) { if (route != "home-organizer") organizerPreview = null }
    BackHandler(enabled = route == "home-organizer" && organizerPreview != null) { organizerPreview = null }
    LaunchedEffect(vm) { vm.messages.collect { snackbar.showSnackbar(it) } }
    WoodlandMotionProvider(state.prefs.reduceMotion, !switcher && !adding && !state.loading) {
    Box(Modifier.fillMaxSize()) {
    WoodlandBackground(route in listOf("schedule", "care", "more") && state.child != null)
    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                Snackbar {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (data.visuals.message == "Saved") WoodlandMotionAsset("feedback-saved", Modifier.size(32.dp),
                            play = true, dark = MaterialTheme.colorScheme.background.red < .3f,
                            tint = MaterialTheme.colorScheme.inverseOnSurface)
                        Text(data.visuals.message)
                    }
                }
            }
        },
        topBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.background.copy(alpha = .96f)).statusBarsPadding()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (route !in listOf("today", "schedule", "care", "more"))
                        IconButton(onClick = {
                            if (route == "home-organizer" && organizerPreview != null) organizerPreview = null
                            else nav.popBackStack()
                        }) {
                            WoodlandIcon(R.drawable.woodland_back, "Back")
                        }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            state.child?.name?.take(1)?.uppercase() ?: "S",
                            Modifier.padding(14.dp),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    TextButton(onClick = { switcher = true }, Modifier.weight(1f)) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                state.child?.name ?: "SproutBook",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                state.child?.let {
                                    Stage.valueOf(it.stage).label + " · Your growing family"
                                } ?: "A little care, every day",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                        WoodlandIcon(R.drawable.woodland_profile, "Switch child")
                    }
                }
                if (state.prefs.grandparent)
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "Grandparent mode · Read only",
                            Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
            }
        },
        bottomBar = {
            NavigationBar(tonalElevation = 0.dp, containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                listOf(
                        "today" to R.drawable.woodland_today,
                        "schedule" to R.drawable.woodland_schedule,
                        "care" to R.drawable.woodland_care,
                        "more" to R.drawable.woodland_more,
                    )
                    .forEach { (name, icon) ->
                        NavigationBarItem(
                            enabled = state.child != null && !state.loading && back != null,
                            modifier = Modifier.testTag("nav-$name"),
                            selected = route == name,
                            onClick = {
                                nav.navigate(name) {
                                    popUpTo("today") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = name != "today"
                                }
                            },
                            icon = { WoodlandIcon(icon, animated = route == name, loop = true) },
                            label = { Text(name.replaceFirstChar { it.uppercase() }) },
                        )
                    }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            if (state.loading)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            else if (state.error != null)
                Page("Records unavailable") {
                    item {
                        Panel {
                            Text(state.error)
                            Action("Try again") { vm.retryOpen() }
                        }
                    }
                }
            else if (state.child == null)
                Page("A place to grow", "Small moments. One connected family.") {
                    item {
                        Panel {
                            Text(
                                "Welcome to SproutBook",
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Muted(
                                "Create a child profile to begin. From pregnancy through the teenage years, their story stays together."
                            )
                            Action("Create first profile") { adding = true }
                        }
                    }
                }
            else
                NavHost(nav, startDestination = "today") {
                    composable("today") {
                        key(state.child.id) { HomeScreen(vm, state) { nav.navigate(it) } }
                    }
                    composable("schedule") { key(state.child.id) { ScheduleScreen(vm, state) } }
                    composable("care") { CareScreen(state) { nav.navigate(it) } }
                    composable(
                        "memories?memoryId={memoryId}&new={new}",
                        arguments =
                            listOf(
                                navArgument("memoryId") {
                                    nullable = true
                                    defaultValue = null
                                },
                                navArgument("new") {
                                    type = NavType.BoolType
                                    defaultValue = false
                                },
                            ),
                    ) { entry ->
                        key(state.child.id) {
                            MemoryScreen(vm, state, entry.arguments?.getString("memoryId"),
                                addNew = entry.arguments?.getBoolean("new") == true && entry.savedStateHandle.get<Boolean>("newHandled") != true,
                                onAddHandled = { entry.savedStateHandle["newHandled"] = true })
                        }
                    }
                    composable("feeding") {
                        key(state.child.id) { FeedingScreen(vm, state) { nav.navigate(it) } }
                    }
                    composable("backup") { BackupScreen(vm, state) }
                    composable("diagnostics") { DiagnosticsScreen(vm, state) }
                    composable("inventory") {
                        key(state.child.id) { InventoryScreen(vm, state) { nav.navigate(it) } }
                    }
                    composable("emergency") { key(state.child.id) { EmergencyScreen(vm, state) } }
                    composable("health") {
                        key(state.child.id) { HealthScreen(vm, state) { nav.navigate(it) } }
                    }
                    composable("routines") { key(state.child.id) { RoutineScreen(vm, state) } }
                    composable("potty") { key(state.child.id) { PottyScreen(vm, state) } }
                    composable("milk") { key(state.child.id) { MilkScreen(vm, state) } }
                    composable("growth") { key(state.child.id) { GrowthScreen(vm, state) } }
                    composable("diapers") { key(state.child.id) { DiaperScreen(vm, state) } }
                    composable("bottles") {
                        key(state.child.id) { BottlePrepScreen(vm, state) { nav.navigate(it) } }
                    }
                    composable("shopping") {
                        key(state.child.id) { ShoppingScreen(vm, state) { nav.navigate(it) } }
                    }
                    composable("wishlist") { key(state.child.id) { WishlistScreen(vm, state) } }
                    composable("help") { HelpScreen() }
                    composable("advice") { AdviceScreen() }
                    composable("pregnancy") {
                        key(state.child.id) { PregnancyScreen(vm, state) { nav.navigate(it) } }
                    }
                    composable("teeth") { key(state.child.id) { TeethScreen(vm, state) } }
                    composable("milestones") { key(state.child.id) { MilestoneScreen(vm, state) } }
                    composable("solids") {
                        key(state.child.id) { SolidsScreen(vm, state) { nav.navigate(it) } }
                    }
                    composable("meals") { key(state.child.id) { MealScreen(vm, state) } }
                    composable("solids-guide") { SolidsGuide() }
                    composable("sleep") { key(state.child.id) { SleepScreen(vm, state) } }
                    composable("formula") { key(state.child.id) { FormulaScreen(state) } }
                    composable("home-organizer") {
                        key(state.child.id) {
                            Box(Modifier.fillMaxSize()) {
                                // Keep the draft composed, but remove its hit targets as well as semantics in preview.
                                Box(if (organizerPreview != null) Modifier.size(0.dp).clearAndSetSemantics {} else Modifier.fillMaxSize()) {
                                    HomeOrganizerScreen(vm, state.child, state.prefs.grandparent,
                                        onClose = { nav.popBackStack() }, onPreview = { organizerPreview = it })
                                }
                                organizerPreview?.let { draft ->
                                    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                        Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
                                            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                                                Text("Preview · Nothing has been saved", style = MaterialTheme.typography.titleSmall)
                                                Text("Actions are disabled while you preview your layout.", style = MaterialTheme.typography.bodySmall)
                                                TextButton(onClick = { organizerPreview = null }, modifier = Modifier.heightIn(min = 48.dp).testTag("organizer-preview-back")) {
                                                    Text("Back to organizer")
                                                }
                                            }
                                        }
                                        Box(Modifier.weight(1f)) { HomeScreen(vm, state, draft, preview = true) {} }
                                    }
                                }
                            }
                        }
                    }
                    composable("more") { ProfileScreen(vm, state) { nav.navigate(it) } }
                }
        }
    }
    }
    if (switcher)
        Editor("Your growing family", { switcher = false }) {
            state.children.forEach { child ->
                EntryRow(child.name, Stage.valueOf(child.stage).label) {
                    vm.select(child.id)
                    switcher = false
                }
            }
            Action("Add another child", !state.prefs.grandparent) {
                switcher = false
                adding = true
            }
        }
    if (adding) ChildEditor(null, state.prefs.grandparent, vm) { adding = false }
    }
}
