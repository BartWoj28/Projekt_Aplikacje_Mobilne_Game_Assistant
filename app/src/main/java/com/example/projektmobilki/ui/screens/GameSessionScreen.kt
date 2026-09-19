package com.example.projektmobilki.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projektmobilki.models.GameHistoryEntry
import com.example.projektmobilki.models.PlayerResult
import com.example.projektmobilki.models.TimerType
import com.example.projektmobilki.ui.screens.components.ChessClockComponent
import com.example.projektmobilki.ui.screens.components.PlayerQueueComponent
import com.example.projektmobilki.ui.screens.components.TurnTimerComponent
import com.example.projektmobilki.ui.theme.ProjektMobilkiTheme
import com.example.projektmobilki.ui.viewmodels.GameViewModel
import com.example.projektmobilki.util.GameHistoryRepository
import com.example.projektmobilki.util.KeepScreenOn
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSessionScreen(
    gameId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    val timerType by viewModel.timerType.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    
    val chessTime1 by viewModel.chessTime1.collectAsState()
    val chessTime2 by viewModel.chessTime2.collectAsState()
    val activeChessPlayer by viewModel.activeChessPlayer.collectAsState()

    val totalTime = when (timerType) {
        TimerType.TURN_30S -> 30000L
        TimerType.TURN_60S -> 60000L
        TimerType.TURN_90S -> 90000L
        TimerType.CHESS_CLOCK -> 300000L
    }

    // Keep screen on during the game session
    KeepScreenOn()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game: $gameId") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
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
                                    durationSeconds = if (durationSeconds > 0) durationSeconds else 5L
                                )
                                GameHistoryRepository.getInstance(context).addGame(entry)
                            }
                            onBack()
                        }
                    ) {
                        Text("End Game", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timer Type Selection
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
                                TimerType.TURN_30S -> "30s"
                                TimerType.TURN_60S -> "60s"
                                TimerType.TURN_90S -> "90s"
                                TimerType.CHESS_CLOCK -> "Chess"
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (timerType == TimerType.CHESS_CLOCK) {
                ChessClockComponent(
                    time1 = chessTime1,
                    time2 = chessTime2,
                    activePlayer = activeChessPlayer,
                    isTimerRunning = isTimerRunning,
                    onTogglePlayer = { viewModel.toggleChessPlayer() },
                    onStartPause = { if (isTimerRunning) viewModel.pauseTimer() else viewModel.startTimer() },
                    onReset = { viewModel.resetTimer() },
                    modifier = Modifier.height(150.dp)
                )
            } else {
                TurnTimerComponent(
                    timeRemaining = timeRemaining,
                    totalTime = totalTime,
                    isTimerRunning = isTimerRunning,
                    onStartPause = { if (isTimerRunning) viewModel.pauseTimer() else viewModel.startTimer() },
                    onReset = { viewModel.resetTimer() }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
            
            PlayerQueueComponent(
                players = gameState.players,
                currentPlayerIndex = gameState.currentPlayerIndex,
                onAddPlayer = { viewModel.addPlayer(it) },
                onRemovePlayer = { viewModel.removePlayer(it) },
                onShuffle = { viewModel.shufflePlayers() },
                onNextTurn = { viewModel.nextTurn() },
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
