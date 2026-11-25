package com.zaviyanllc.sproutplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zaviyanllc.sproutplay.ui.SproutAppViewModel
import com.zaviyanllc.sproutplay.ui.game.CupGameScreen
import com.zaviyanllc.sproutplay.ui.home.TwinTrapHomeScreen
import com.zaviyanllc.sproutplay.ui.theme.SproutPlayTheme

private const val ROUTE_HOME = "home"
private const val ROUTE_GAME = "game/{level}"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SproutPlayTheme {
                TwinTrapRootApp()
            }
        }
    }
}

/**
 * Twin Trap navigation: home (title + level grid) -> cup game for a level.
 * No onboarding, no profiles UI, no parent zone — one silent player profile.
 */
@Composable
fun TwinTrapRootApp(
    viewModel: SproutAppViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = ROUTE_HOME,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(ROUTE_HOME) {
            TwinTrapHomeScreen(
                state = state,
                audio = viewModel.audioManager,
                onPlayLevel = { level -> navController.navigate("game/$level") },
                onToggleSound = { viewModel.setSoundEnabled(it) },
                onToggleMusic = { viewModel.setMusicEnabled(it) },
            )
        }

        composable(
            route = ROUTE_GAME,
            arguments = listOf(navArgument("level") { type = NavType.IntType }),
        ) { entry ->
            val level = entry.arguments?.getInt("level") ?: 1
            CupGameScreen(
                level = level,
                audio = viewModel.audioManager,
                onBack = { navController.popBackStack() },
                onLevelComplete = { stars, coins ->
                    viewModel.recordLevelResult(level, stars, coins)
                },
                onNextLevel = if (level < 10) {
                    {
                        navController.navigate("game/${level + 1}") {
                            popUpTo("game/$level") { inclusive = true }
                        }
                    }
                } else {
                    null
                },
                onOpenLevels = {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(ROUTE_HOME) { inclusive = true }
                    }
                },
            )
        }
    }
}
