package com.zaviyanllc.sproutplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zaviyanllc.sproutplay.domain.model.AgeTier
import com.zaviyanllc.sproutplay.domain.model.GameCategory
import com.zaviyanllc.sproutplay.ui.SproutAppViewModel
import com.zaviyanllc.sproutplay.ui.catalog.CatalogScreen
import com.zaviyanllc.sproutplay.ui.game.GameHostScreen
import com.zaviyanllc.sproutplay.ui.home.HomeHubScreen
import com.zaviyanllc.sproutplay.ui.onboarding.OnboardingScreen
import com.zaviyanllc.sproutplay.ui.parents.ParentZoneScreen
import com.zaviyanllc.sproutplay.ui.stickers.StickersScreen
import com.zaviyanllc.sproutplay.ui.theme.SproutPlayTheme

private const val ROUTE_HOME = "home"
private const val ROUTE_CATALOG = "catalog"
private const val ROUTE_STICKERS = "stickers"
private const val ROUTE_PARENTS = "parents"
private const val ROUTE_ADD_PROFILE = "add_profile"
private const val ROUTE_GAME = "game/{gameId}/{tierIndex}"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SproutPlayTheme {
                SproutPlayRootApp()
            }
        }
    }
}

@Composable
fun SproutPlayRootApp(
    viewModel: SproutAppViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    var catalogInitialCategory by remember { mutableStateOf<GameCategory?>(null) }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.needsOnboarding) {
        OnboardingScreen(
            isAddingAdditionalProfile = false,
            onSpeak = { viewModel.audioManager.speak(it) },
            onTapSound = { viewModel.audioManager.playTap() },
            onCancelAdd = null,
            onComplete = { name, age, avatarId ->
                viewModel.completeOnboarding(name, age, avatarId) {}
            },
        )
        return
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_HOME
    val showMainNav = currentRoute in setOf(ROUTE_HOME, ROUTE_CATALOG, ROUTE_STICKERS)
    val isWideScreen = LocalConfiguration.current.screenWidthDp >= 720

    val openGame = { gameId: String, tier: AgeTier ->
        navController.navigate("game/$gameId/${tier.levelIndex}")
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (showMainNav && !isWideScreen) {
                NavigationBar(windowInsets = WindowInsets.navigationBars) {
                    NavigationBarItem(
                        selected = currentRoute == ROUTE_HOME,
                        onClick = {
                            viewModel.audioManager.playTap()
                            navController.navigate(ROUTE_HOME) {
                                popUpTo(ROUTE_HOME) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_home"),
                    )
                    NavigationBarItem(
                        selected = currentRoute == ROUTE_CATALOG,
                        onClick = {
                            viewModel.audioManager.playTap()
                            catalogInitialCategory = null
                            navController.navigate(ROUTE_CATALOG) {
                                popUpTo(ROUTE_HOME)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Rounded.Apps, contentDescription = "50 Games") },
                        label = { Text("50 Games") },
                        modifier = Modifier.testTag("nav_catalog"),
                    )
                    NavigationBarItem(
                        selected = currentRoute == ROUTE_STICKERS,
                        onClick = {
                            viewModel.audioManager.playTap()
                            navController.navigate(ROUTE_STICKERS) {
                                popUpTo(ROUTE_HOME)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = "Stickers") },
                        label = { Text("Stickers") },
                        modifier = Modifier.testTag("nav_stickers"),
                    )
                }
            }
        },
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (showMainNav && isWideScreen) {
                NavigationRail {
                    NavigationRailItem(
                        selected = currentRoute == ROUTE_HOME,
                        onClick = {
                            viewModel.audioManager.playTap()
                            navController.navigate(ROUTE_HOME) { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                    )
                    NavigationRailItem(
                        selected = currentRoute == ROUTE_CATALOG,
                        onClick = {
                            viewModel.audioManager.playTap()
                            catalogInitialCategory = null
                            navController.navigate(ROUTE_CATALOG) { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Rounded.Apps, contentDescription = "50 Games") },
                        label = { Text("50 Games") },
                    )
                    NavigationRailItem(
                        selected = currentRoute == ROUTE_STICKERS,
                        onClick = {
                            viewModel.audioManager.playTap()
                            navController.navigate(ROUTE_STICKERS) { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = "Stickers") },
                        label = { Text("Stickers") },
                    )
                }
            }

            NavHost(
                navController = navController,
                startDestination = ROUTE_HOME,
                modifier = Modifier.weight(1f),
            ) {
                composable(ROUTE_HOME) {
                    HomeHubScreen(
                        state = state,
                        onPlayGame = { gameId -> openGame(gameId, state.activeTier) },
                        onOpenCategory = { cat ->
                            catalogInitialCategory = cat
                            navController.navigate(ROUTE_CATALOG) { launchSingleTop = true }
                        },
                        onOpenAllGames = {
                            catalogInitialCategory = null
                            navController.navigate(ROUTE_CATALOG) { launchSingleTop = true }
                        },
                        onOpenParents = { navController.navigate(ROUTE_PARENTS) },
                        onSpeak = { viewModel.audioManager.speak(it) },
                        onTapSound = { viewModel.audioManager.playTap() },
                    )
                }

                composable(ROUTE_CATALOG) {
                    CatalogScreen(
                        state = state,
                        initialCategory = catalogInitialCategory,
                        onPlayGame = { gameId, tier -> openGame(gameId, tier) },
                        onTapSound = { viewModel.audioManager.playTap() },
                    )
                }

                composable(ROUTE_STICKERS) {
                    StickersScreen(state = state)
                }

                composable(ROUTE_PARENTS) {
                    ParentZoneScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onSwitchProfile = {
                            viewModel.switchProfile(it)
                            navController.popBackStack(ROUTE_HOME, inclusive = false)
                        },
                        onAddNewProfile = { navController.navigate(ROUTE_ADD_PROFILE) },
                        onUpdateProfileAge = { profile, newAge ->
                            viewModel.updateProfile(profile.copy(age = newAge))
                        },
                        onDeleteProfile = { viewModel.deleteProfile(it) },
                        onToggleSound = { viewModel.setSoundEnabled(it) },
                        onToggleVoice = { viewModel.setVoiceEnabled(it) },
                        onToggleMusic = { viewModel.setMusicCuesEnabled(it) },
                        onSetTimeLimit = { viewModel.setDailyTimeLimit(it) },
                    )
                }

                composable(ROUTE_ADD_PROFILE) {
                    OnboardingScreen(
                        isAddingAdditionalProfile = true,
                        onSpeak = { viewModel.audioManager.speak(it) },
                        onTapSound = { viewModel.audioManager.playTap() },
                        onCancelAdd = { navController.popBackStack() },
                        onComplete = { name, age, avatarId ->
                            viewModel.completeOnboarding(name, age, avatarId) {
                                navController.navigate(ROUTE_HOME) {
                                    popUpTo(ROUTE_HOME) { inclusive = true }
                                }
                            }
                        },
                    )
                }

                composable(
                    route = ROUTE_GAME,
                    arguments = listOf(
                        navArgument("gameId") { type = NavType.StringType },
                        navArgument("tierIndex") { type = NavType.IntType },
                    ),
                ) { entry ->
                    val gameId = entry.arguments?.getString("gameId") ?: "candy_math"
                    val tierIdx = entry.arguments?.getInt("tierIndex") ?: state.activeTier.levelIndex
                    val tier = AgeTier.entries.getOrElse(tierIdx) { state.activeTier }
                    GameHostScreen(
                        gameId = gameId,
                        initialTier = tier,
                        childName = state.activeProfile?.name ?: "Explorer",
                        onBack = {
                            viewModel.audioManager.stopSpeaking()
                            navController.popBackStack()
                        },
                        onSpeak = { viewModel.audioManager.speak(it) },
                        onTapSound = { viewModel.audioManager.playTap() },
                        onPlaySuccess = { viewModel.audioManager.playSuccess() },
                        onPlayCelebration = { viewModel.audioManager.playCelebration() },
                        onPlayGentleTryAgain = { viewModel.audioManager.playGentleTryAgain() },
                        onPlayNote = { viewModel.audioManager.playMusicalNote(it) },
                        onRecordWin = { id, stars, score, playedTier, onStickers ->
                            viewModel.recordGameResult(id, stars, score, playedTier, onStickers)
                        },
                        progressMap = state.progressMap,
                    )
                }
            }
        }
    }

    if (state.showScreenTimeReminder) {
        AlertDialog(
            onDismissRequest = { viewModel.snoozeTimeReminder() },
            title = { Text("Time for a Stretch Break! 🌿") },
            text = {
                Text(
                    "You've played for ${state.minutesUsedToday} minutes today. Great learning! Take a quick stretch or water break before playing more.",
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.snoozeTimeReminder() }) {
                    Text("Okay, Thanks!")
                }
            },
        )
    }
}
