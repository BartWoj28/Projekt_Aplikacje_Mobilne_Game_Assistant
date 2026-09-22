package com.example.projektmobilki.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.activity.ComponentActivity
import android.content.ContextWrapper
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projektmobilki.models.GameHistoryEntry
import com.example.projektmobilki.models.PlayerResult
import com.example.projektmobilki.models.TimerType
import com.example.projektmobilki.ui.screens.components.PlayerQueueComponent
import com.example.projektmobilki.ui.screens.components.TurnTimerComponent
import com.example.projektmobilki.ui.theme.ProjektMobilkiTheme
import com.example.projektmobilki.ui.viewmodels.GameViewModel
import com.example.projektmobilki.util.GameHistoryRepository
import com.example.projektmobilki.util.KeepScreenOn
import com.example.projektmobilki.util.SettingsRepository
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSessionScreen(
    gameId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx is ContextWrapper && ctx !is ComponentActivity) {
            ctx = ctx.baseContext
        }
        ctx as? ComponentActivity
    }
    
    val viewModel: GameViewModel = if (activity != null) {
        viewModel(viewModelStoreOwner = activity)
    } else {
        viewModel()
    }

    LaunchedEffect(gameId) {
        viewModel.initializeGame(gameId)
    }

    DisposableEffect(gameId) {
        onDispose {
            viewModel.pauseTimer()
        }
    }

    val gameState by viewModel.gameState.collectAsState()
    val timerType by viewModel.timerType.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()

    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()

    val totalTime = when (timerType) {
        TimerType.TURN_10S -> 10000L
        TimerType.TURN_30S -> 30000L
        TimerType.TURN_60S -> 60000L
        TimerType.TURN_90S -> 90000L
        TimerType.TURN_UNLIMITED -> -1L
    }

    // Keep screen on during the game session
    KeepScreenOn()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Game Session", modifier = Modifier.weight(1f))
                            // Display compact timer in the TopAppBar so it's always visible during tool usage
                            if (timerType == TimerType.TURN_UNLIMITED) {
                                Text(
                                    text = "∞",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "${timeRemaining / 1000}s",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = if (timeRemaining <= 10000) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            scope.launch {
                                if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                    scaffoldState.bottomSheetState.partialExpand()
                                } else {
                                    scaffoldState.bottomSheetState.expand()
                                }
                            }
                        }) {
                            Icon(Icons.Rounded.Casino, contentDescription = "Tools")
                        }
                        TextButton(
                            onClick = {
                                if (gameState.players.isNotEmpty()) {
                                    val maxScore = gameState.players.maxOf { it.score }
                                    val winners = gameState.players.filter { it.score == maxScore }
                                    val winnerName = if (winners.size == 1) winners.first().name else "Draw"
                                    
                                    val playerResults = gameState.players.map { PlayerResult(it.name, it.score) }
                                    val durationSeconds = viewModel.getGameDurationSeconds()
                                    
                                    val entry = GameHistoryEntry(
                                        id = UUID.randomUUID().toString(),
                                        dateMillis = System.currentTimeMillis(),
                                        playerResults = playerResults,
                                        winnerName = winnerName,
                                        durationSeconds = if (durationSeconds > 0) durationSeconds else 5L,
                                        endStage = "Round ${gameState.roundCount}, Turn ${gameState.currentPlayerIndex + 1}"
                                    )
                                    GameHistoryRepository.getInstance(context).addGame(entry)
                                    
                                    val playerNames = gameState.players.map { it.name }
                                    SettingsRepository.getInstance(context).saveLastPlayers(playerNames)
                                }
                                viewModel.endCurrentGame()
                                onBack()
                            }
                        ) {
                            Text("End Game", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                // Turn Progress Bar (visual timer) always visible at the top, even when tools are used
                val barProgress = if (totalTime > 0) timeRemaining.toFloat() / totalTime.toFloat() else 0f
                if (timerType == TimerType.TURN_UNLIMITED) {
                    LinearProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    LinearProgressIndicator(
                        progress = { barProgress },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = if (timeRemaining <= 10000) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        sheetContent = {
            Box(modifier = Modifier.fillMaxHeight(0.8f).padding(16.dp)) {
                var toolTab by remember { mutableIntStateOf(0) }
                Column {
                    TabRow(selectedTabIndex = toolTab) {
                        Tab(selected = toolTab == 0, onClick = { toolTab = 0 }, text = { Text("Dice/Coin") })
                        Tab(selected = toolTab == 1, onClick = { toolTab = 1 }, text = { Text("Deck") })
                    }
                    
                    Box(modifier = Modifier.weight(1f)) {
                        if (toolTab == 0) {
                            RandomizerScreen(
                                onBack = { scope.launch { scaffoldState.bottomSheetState.partialExpand() } }, 
                                showHeader = false,
                                isActive = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded
                            )
                        } else {
                            DeckScreen(onBack = { scope.launch { scaffoldState.bottomSheetState.partialExpand() } }, showHeader = false)
                        }
                    }
                }
            }
        },
        sheetPeekHeight = 0.dp,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timer components moved to the top of the main layout column
            TurnTimerComponent(
                timeRemaining = timeRemaining,
                totalTime = totalTime,
                isTimerRunning = isTimerRunning,
                onStartPause = { if (isTimerRunning) viewModel.pauseTimer() else viewModel.startTimer() },
                onReset = { viewModel.resetTimer() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Timer Type Selection moved below the timer
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val types = TimerType.entries
                types.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = timerType == type,
                        onClick = { viewModel.setTimerType(type) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size)
                    ) {
                        Text(
                            text = when(type) {
                                TimerType.TURN_10S -> "10s"
                                TimerType.TURN_30S -> "30s"
                                TimerType.TURN_60S -> "60s"
                                TimerType.TURN_90S -> "90s"
                                TimerType.TURN_UNLIMITED -> "∞"
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            PlayerQueueComponent(
                players = gameState.players,
                currentPlayerIndex = gameState.currentPlayerIndex,
                roundCount = gameState.roundCount,
                onAddPlayer = { viewModel.addPlayer(it) },
                onRemovePlayer = { viewModel.removePlayer(it) },
                onClearAllPlayers = { viewModel.clearPlayers() },
                onShuffle = { viewModel.shufflePlayers() },
                onNextTurn = { viewModel.nextTurn() },
                onPreviousTurn = { viewModel.previousTurn() },
                onUpdateScore = { id, delta -> viewModel.updateScore(id, delta) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameSessionScreenPreview() {
    ProjektMobilkiTheme {
        GameSessionScreen(gameId = "123", onBack = {})
    }
}
