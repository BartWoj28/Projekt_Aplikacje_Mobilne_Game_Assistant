package com.example.projektmobilki.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projektmobilki.mechanics.chess.*
import com.example.projektmobilki.models.GameHistoryEntry
import com.example.projektmobilki.models.PlayerResult
import com.example.projektmobilki.ui.viewmodels.ChessMatchState
import com.example.projektmobilki.ui.viewmodels.ChessViewModel
import com.example.projektmobilki.util.GameHistoryRepository
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessScreen(
    onBack: () -> Unit,
    viewModel: ChessViewModel = viewModel()
) {
    val state by viewModel.matchState.collectAsState()
    
    if (state.isGameActive) {
        ChessGameScreen(viewModel = viewModel)
    } else {
        ChessSetupScreen(state = state, viewModel = viewModel, onBack = onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessSetupScreen(state: ChessMatchState, viewModel: ChessViewModel, onBack: () -> Unit) {
    var p1 by remember { mutableStateOf(state.playerWhite) }
    var p2 by remember { mutableStateOf(state.playerBlack) }
    var timeMin by remember { mutableStateOf(state.timeMinutes) }
    var incSec by remember { mutableStateOf(state.incrementSeconds) }
    
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chess Setup") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (state.scoreWhite > 0 || state.scoreBlack > 0) {
                        TextButton(onClick = {
                            val results = listOf(
                                PlayerResult(p1, state.scoreWhite),
                                PlayerResult(p2, state.scoreBlack)
                            )
                            val winner = if (state.scoreWhite > state.scoreBlack) p1 else if (state.scoreBlack > state.scoreWhite) p2 else "Draw"
                            
                            GameHistoryRepository.getInstance(context).addGame(
                                GameHistoryEntry(
                                    id = UUID.randomUUID().toString(),
                                    dateMillis = System.currentTimeMillis(),
                                    playerResults = results,
                                    winnerName = winner,
                                    durationSeconds = 0,
                                    endStage = "",
                                    gameType = "Chess"
                                )
                            )
                            viewModel.resetSeries()
                            onBack()
                        }) {
                            Text("End Series", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier.padding(padding).fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Scores: $p1 ${state.scoreWhite} - ${state.scoreBlack} $p2", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(32.dp))
                
                OutlinedTextField(value = p1, onValueChange = { p1 = it; viewModel.setPlayers(it, p2) }, label = { Text("White Player") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = p2, onValueChange = { p2 = it; viewModel.setPlayers(p1, it) }, label = { Text("Black Player") })
                
                Spacer(Modifier.height(16.dp))
                Button(onClick = { 
                    viewModel.randomizeSides()
                    val temp = p1
                    p1 = p2
                    p2 = temp
                }) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Randomize Sides")
                }
                
                Spacer(Modifier.height(32.dp))
                Text("Time Format", style = MaterialTheme.typography.titleMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val times = listOf(1, 3, 5, 10)
                    times.forEachIndexed { index, t ->
                        SegmentedButton(
                            selected = timeMin == t,
                            onClick = { timeMin = t; viewModel.setTimeFormat(t) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = times.size)
                        ) { Text("${t}m") }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                Text("Increment", style = MaterialTheme.typography.titleMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val incs = listOf(0, 2)
                    incs.forEachIndexed { index, t ->
                        SegmentedButton(
                            selected = incSec == t,
                            onClick = { incSec = t; viewModel.setIncrement(t) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = incs.size)
                        ) { Text("+${t}s") }
                    }
                }
                
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { viewModel.startGame() },
                    modifier = Modifier.fillMaxWidth().height(64.dp)
                ) {
                    Text("Start Game", fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
fun ChessGameScreen(viewModel: ChessViewModel) {
    val state by viewModel.matchState.collectAsState()
    val engine by viewModel.engine.collectAsState()
    val board by viewModel.boardState.collectAsState()
    val whiteTime by viewModel.whiteTimeMs.collectAsState()
    val blackTime by viewModel.blackTimeMs.collectAsState()
    
    var selectedPos by remember { mutableStateOf<Position?>(null) }
    var validMoves by remember { mutableStateOf<List<Move>>(emptyList()) }
    var promotionMove by remember { mutableStateOf<Move?>(null) }
    var resignConfirm by remember { mutableStateOf<PieceColor?>(null) }
    val drawOffer by viewModel.drawOffer.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            // Black Player Info
            PlayerInfoRow(
                name = state.playerBlack,
                timeMs = blackTime,
                captured = engine.blackCaptured,
                capturedColor = PieceColor.WHITE,
                isActive = engine.turn == PieceColor.BLACK && !engine.isCheckmate && !engine.isStalemate,
                onResign = { resignConfirm = PieceColor.BLACK },
                onDrawOffer = { viewModel.offerDraw(PieceColor.BLACK) }
            )
        
        Spacer(Modifier.height(16.dp))
        
        // Board
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.aspectRatio(1f).border(2.dp, Color.Black)) {
                for (r in 0..7) {
                    Row(modifier = Modifier.weight(1f)) {
                        for (c in 0..7) {
                            val isLight = (r + c) % 2 == 0
                            val bgColor = if (isLight) Color(0xFFF0D9B5) else Color(0xFFB58863)
                            val pos = Position(r, c)
                            val isSelected = pos == selectedPos
                            val isMove = validMoves.any { it.to == pos }
                            val piece = board[r][c]
                            
                            val squareColor = if (isSelected) Color(0xFFFFF59D) else if (isMove) Color(0xFFA5D6A7) else bgColor
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(squareColor)
                                    .clickable {
                                        if (promotionMove != null) return@clickable
                                        val move = validMoves.find { it.to == pos }
                                        if (move != null) {
                                            if ((piece?.type == PieceType.PAWN || engine.board[move.from.r][move.from.c]?.type == PieceType.PAWN) && (pos.r == 0 || pos.r == 7)) {
                                                promotionMove = move
                                            } else {
                                                viewModel.makeMove(move)
                                                selectedPos = null
                                                validMoves = emptyList()
                                            }
                                        } else {
                                            if (piece?.color == engine.turn) {
                                                selectedPos = pos
                                                validMoves = viewModel.getLegalMoves(pos)
                                            } else {
                                                selectedPos = null
                                                validMoves = emptyList()
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                piece?.let {
                                    Text(
                                        text = getPieceChar(it),
                                        fontSize = 32.sp,
                                        color = if (it.color == PieceColor.WHITE) Color.White else Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Overlays
            if (promotionMove != null) {
                PromotionDialog(
                    onPromote = { type ->
                        viewModel.makeMove(promotionMove!!.copy(promotion = type))
                        promotionMove = null
                        selectedPos = null
                        validMoves = emptyList()
                    }
                )
            }
            
            if (engine.isCheckmate) {
                val winner = if (engine.turn == PieceColor.WHITE) state.playerBlack else state.playerWhite
                GameOverDialog(title = "Checkmate!", message = "$winner wins.", onDismiss = { viewModel.returnToSetup() })
            } else if (engine.isStalemate) {
                GameOverDialog(title = "Stalemate!", message = "Draw.", onDismiss = { viewModel.returnToSetup() })
            } else if (whiteTime == 0L) {
                GameOverDialog(title = "Time Out!", message = "${state.playerBlack} wins on time.", onDismiss = { viewModel.returnToSetup() })
            } else if (blackTime == 0L) {
                GameOverDialog(title = "Time Out!", message = "${state.playerWhite} wins on time.", onDismiss = { viewModel.returnToSetup() })
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
            // White Player Info
            PlayerInfoRow(
                name = state.playerWhite,
                timeMs = whiteTime,
                captured = engine.whiteCaptured,
                capturedColor = PieceColor.BLACK,
                isActive = engine.turn == PieceColor.WHITE && !engine.isCheckmate && !engine.isStalemate,
                onResign = { resignConfirm = PieceColor.WHITE },
                onDrawOffer = { viewModel.offerDraw(PieceColor.WHITE) }
            )
        }
    }
    
    if (resignConfirm != null) {
        val player = if (resignConfirm == PieceColor.WHITE) state.playerWhite else state.playerBlack
        AlertDialog(
            onDismissRequest = { resignConfirm = null },
            title = { Text("Resign") },
            text = { Text("$player, are you sure you want to resign?") },
            confirmButton = { 
                Button(
                    onClick = { 
                        viewModel.resign(resignConfirm!!)
                        resignConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Resign") } 
            },
            dismissButton = { TextButton(onClick = { resignConfirm = null }) { Text("Cancel") } }
        )
    }
    
    if (drawOffer != null) {
        val offeringPlayer = if (drawOffer == PieceColor.WHITE) state.playerWhite else state.playerBlack
        val receivingPlayer = if (drawOffer == PieceColor.WHITE) state.playerBlack else state.playerWhite
        AlertDialog(
            onDismissRequest = { viewModel.declineDraw() },
            title = { Text("Draw Offer") },
            text = { Text("$offeringPlayer offered a draw to $receivingPlayer.") },
            confirmButton = { Button(onClick = { viewModel.acceptDraw() }) { Text("Accept") } },
            dismissButton = { TextButton(onClick = { viewModel.declineDraw() }) { Text("Decline") } }
        )
    }
}

@Composable
fun PlayerInfoRow(name: String, timeMs: Long, captured: List<PieceType>, capturedColor: PieceColor, isActive: Boolean, onResign: () -> Unit, onDrawOffer: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    captured.forEach { type ->
                        Text(
                            text = getPieceChar(Piece(type, capturedColor)), 
                            fontSize = 20.sp,
                            color = if (capturedColor == PieceColor.WHITE) Color.White else Color.Black,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Gray,
                                    blurRadius = 2f
                                )
                            )
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    Button(onClick = onResign, modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text("Resign", fontSize = 12.sp)
                    }
                    OutlinedButton(onClick = onDrawOffer, modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text("Draw", fontSize = 12.sp)
                    }
                }
            }
            Text(
                formatChessTime(timeMs),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (timeMs < 30000) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PromotionDialog(onPromote: (PieceType) -> Unit) {
    Card(modifier = Modifier.padding(16.dp), elevation = CardDefaults.cardElevation(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Promote Pawn to:")
            Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT).forEach { type ->
                    Button(onClick = { onPromote(type) }) {
                        Text(getPieceChar(Piece(type, PieceColor.WHITE)), fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(title: String, message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { Button(onClick = onDismiss) { Text("OK") } }
    )
}

fun formatChessTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun getPieceChar(piece: Piece): String {
    return when (piece.type) {
        PieceType.KING -> "♚"
        PieceType.QUEEN -> "♛"
        PieceType.ROOK -> "♜"
        PieceType.BISHOP -> "♝"
        PieceType.KNIGHT -> "♞"
        PieceType.PAWN -> "♟"
    }
}