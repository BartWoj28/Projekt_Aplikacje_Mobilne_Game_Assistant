package com.example.projektmobilki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.projektmobilki.navigation.GameHistory
import com.example.projektmobilki.navigation.ChessMatch
import com.example.projektmobilki.ui.screens.ChessScreen
import com.example.projektmobilki.navigation.GameSession
import com.example.projektmobilki.navigation.MainMenu
import com.example.projektmobilki.navigation.NavRoute
import com.example.projektmobilki.navigation.Randomizer
import com.example.projektmobilki.navigation.Settings
import com.example.projektmobilki.navigation.VirtualDeck
import com.example.projektmobilki.ui.screens.DeckScreen
import com.example.projektmobilki.ui.screens.GameSessionScreen
import com.example.projektmobilki.ui.screens.MainMenuScreen
import com.example.projektmobilki.ui.screens.RandomizerScreen
import com.example.projektmobilki.ui.screens.SettingsScreen
import com.example.projektmobilki.ui.theme.ProjektMobilkiTheme
import com.example.projektmobilki.ui.viewmodels.GameViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjektMobilkiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppContent() {
    val backStack = rememberNavBackStack(MainMenu)
    
    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    val gameViewModel: GameViewModel = viewModel()
    val hasActiveGame by gameViewModel.hasActiveGame.collectAsState()

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        sceneStrategy = listDetailStrategy,
        entryProvider = entryProvider {
            entry<MainMenu>(
                metadata = ListDetailSceneStrategy.listPane()
            ) {
                MainMenuScreen(
                    onStartGame = dropUnlessResumed {
                        backStack.add(GameSession("ActiveSession"))
                    },
                    hasActiveGame = hasActiveGame,
                    onOpenChess = dropUnlessResumed {
                        backStack.add(ChessMatch)
                    },
                    onOpenSettings = dropUnlessResumed {
                        backStack.add(Settings)
                    },
                    onOpenRandomizer = dropUnlessResumed {
                        backStack.add(Randomizer)
                    },
                    onOpenVirtualDeck = dropUnlessResumed {
                        backStack.add(VirtualDeck)
                    },
                    onOpenHistory = dropUnlessResumed {
                        backStack.add(GameHistory)
                    }
                )
            }
            
            entry<Randomizer>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                RandomizerScreen(
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            
            entry<GameSession>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { gameSession ->
                GameSessionScreen(
                    gameId = gameSession.gameId,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<VirtualDeck>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                DeckScreen(
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<GameHistory>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                com.example.projektmobilki.ui.screens.GameHistoryScreen(
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            
            entry<ChessMatch>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                ChessScreen(
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            
            entry<Settings>(
                metadata = ListDetailSceneStrategy.extraPane()
            ) {
                SettingsScreen(
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
