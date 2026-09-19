package com.example.projektmobilki.ui.screens.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projektmobilki.models.Player

@Composable
fun TurnTimerComponent(
    timeRemaining: Long,
    totalTime: Long,
    isTimerRunning: Boolean,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (totalTime > 0) timeRemaining.toFloat() / totalTime.toFloat() else 0f,
        label = "timerProgress"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${timeRemaining / 1000}s",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            color = if (timeRemaining <= 10000) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = onStartPause) {
                Icon(if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (isTimerRunning) "Pause" else "Start")
            }
            OutlinedButton(onClick = onReset) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Reset")
            }
        }
    }
}

@Composable
fun ChessClockComponent(
    time1: Long,
    time2: Long,
    activePlayer: Int,
    isTimerRunning: Boolean,
    onTogglePlayer: () -> Unit,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChessPlayerCard(
                time = time1,
                totalTime = 300000L,
                isActive = activePlayer == 1,
                label = "Player 1",
                onClick = { if (activePlayer == 1 && isTimerRunning) onTogglePlayer() else if (activePlayer == 1 && !isTimerRunning) onStartPause() },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ChessPlayerCard(
                time = time2,
                totalTime = 300000L,
                isActive = activePlayer == 2,
                label = "Player 2",
                onClick = { if (activePlayer == 2 && isTimerRunning) onTogglePlayer() else if (activePlayer == 2 && !isTimerRunning) onStartPause() },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onStartPause) {
                Icon(if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
            }
            IconButton(onClick = onReset) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessPlayerCard(
    time: Long,
    totalTime: Long,
    isActive: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (totalTime > 0) time.toFloat() / totalTime.toFloat() else 0f,
        label = "chessProgress"
    )

    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Text(
                text = formatTime(time),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = if (time <= 30000) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

@Composable
fun PlayerQueueComponent(
    players: List<Player>,
    currentPlayerIndex: Int,
    onAddPlayer: (String) -> Unit,
    onRemovePlayer: (String) -> Unit,
    onShuffle: () -> Unit,
    onNextTurn: () -> Unit,
    onUpdateScore: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newPlayerName by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Players Queue", style = MaterialTheme.typography.titleLarge)
            Row {
                IconButton(onClick = onShuffle) { Icon(Icons.Default.Shuffle, contentDescription = "Shuffle") }
                IconButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Add") }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(players) { player ->
                val index = players.indexOf(player)
                val isCurrent = index == currentPlayerIndex
                PlayerItem(
                    player = player,
                    isCurrent = isCurrent,
                    onRemove = { onRemovePlayer(player.id) },
                    onUpdateScore = { delta -> onUpdateScore(player.id, delta) }
                )
            }
        }

        Button(
            onClick = onNextTurn,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Next Player")
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Player") },
            text = {
                OutlinedTextField(
                    value = newPlayerName,
                    onValueChange = { newPlayerName = it },
                    label = { Text("Player Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newPlayerName.isNotBlank()) {
                        onAddPlayer(newPlayerName)
                        newPlayerName = ""
                        showAddDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun PlayerItem(
    player: Player,
    isCurrent: Boolean,
    onRemove: () -> Unit,
    onUpdateScore: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isCurrent) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
                Text(text = "Score: ${player.score}", style = MaterialTheme.typography.bodySmall)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onUpdateScore(-1) }) { Icon(Icons.Default.Remove, contentDescription = "Decrease Score") }
                IconButton(onClick = { onUpdateScore(1) }) { Icon(Icons.Default.Add, contentDescription = "Increase Score") }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, contentDescription = "Remove Player") }
            }
        }
    }
}
