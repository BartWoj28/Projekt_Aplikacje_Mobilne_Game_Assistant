package com.example.projektmobilki.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektmobilki.mechanics.chess.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChessMatchState(
    val playerWhite: String = "Player 1",
    val playerBlack: String = "Player 2",
    val scoreWhite: Int = 0,
    val scoreBlack: Int = 0,
    val timeMinutes: Int = 10,
    val incrementSeconds: Int = 0,
    val isGameActive: Boolean = false
)

class ChessViewModel : ViewModel() {
    
    private val _matchState = MutableStateFlow(ChessMatchState())
    val matchState: StateFlow<ChessMatchState> = _matchState.asStateFlow()

    private val _engine = MutableStateFlow(ChessEngine())
    val engine: StateFlow<ChessEngine> = _engine.asStateFlow()
    
    private val _boardState = MutableStateFlow<Array<Array<Piece?>>>(Array(8) { arrayOfNulls(8) })
    val boardState: StateFlow<Array<Array<Piece?>>> = _boardState.asStateFlow()

    private val _whiteTimeMs = MutableStateFlow(0L)
    val whiteTimeMs: StateFlow<Long> = _whiteTimeMs.asStateFlow()

    private val _blackTimeMs = MutableStateFlow(0L)
    val blackTimeMs: StateFlow<Long> = _blackTimeMs.asStateFlow()

    private val _drawOffer = MutableStateFlow<PieceColor?>(null)
    val drawOffer: StateFlow<PieceColor?> = _drawOffer.asStateFlow()

    private var timerJob: Job? = null
    private var lastTickTime: Long = 0

    init {
        updateBoardState()
    }

    fun setPlayers(p1: String, p2: String) {
        _matchState.value = _matchState.value.copy(playerWhite = p1, playerBlack = p2)
    }

    fun resetSeries() {
        val current = _matchState.value
        _matchState.value = current.copy(
            scoreWhite = 0,
            scoreBlack = 0,
            isGameActive = false
        )
    }

    fun randomizeSides() {
        val current = _matchState.value
        _matchState.value = current.copy(
            playerWhite = current.playerBlack,
            playerBlack = current.playerWhite,
            scoreWhite = current.scoreBlack,
            scoreBlack = current.scoreWhite
        )
    }

    fun setTimeFormat(minutes: Int) {
        _matchState.value = _matchState.value.copy(timeMinutes = minutes)
    }

    fun setIncrement(seconds: Int) {
        _matchState.value = _matchState.value.copy(incrementSeconds = seconds)
    }

    fun startGame() {
        _engine.value = ChessEngine()
        updateBoardState()
        val startingMs = _matchState.value.timeMinutes * 60 * 1000L
        _whiteTimeMs.value = startingMs
        _blackTimeMs.value = startingMs
        
        _drawOffer.value = null
        _matchState.value = _matchState.value.copy(isGameActive = true)
        
        startTimer()
    }

    fun getLegalMoves(pos: Position): List<Move> {
        if (!_matchState.value.isGameActive || _engine.value.isCheckmate || _engine.value.isStalemate) return emptyList()
        return _engine.value.getLegalMoves(pos)
    }

    fun makeMove(move: Move) {
        _engine.value.makeMove(move)
        updateBoardState()
        
        if (_engine.value.turn == PieceColor.BLACK) {
            _whiteTimeMs.value += _matchState.value.incrementSeconds * 1000L
        } else {
            _blackTimeMs.value += _matchState.value.incrementSeconds * 1000L
        }

        if (_engine.value.isCheckmate) {
            stopTimer()
            if (_engine.value.turn == PieceColor.WHITE) {
                // Black wins
                _matchState.value = _matchState.value.copy(scoreBlack = _matchState.value.scoreBlack + 1)
            } else {
                // White wins
                _matchState.value = _matchState.value.copy(scoreWhite = _matchState.value.scoreWhite + 1)
            }
        } else if (_engine.value.isStalemate) {
            stopTimer()
        }
    }

    private fun updateBoardState() {
        // Deep copy board to force UI refresh
        val newBoard = Array(8) { arrayOfNulls<Piece>(8) }
        val currBoard = _engine.value.board
        for (r in 0..7) {
            for (c in 0..7) {
                newBoard[r][c] = currBoard[r][c]?.copy()
            }
        }
        _boardState.value = newBoard
    }

    private fun startTimer() {
        timerJob?.cancel()
        lastTickTime = System.currentTimeMillis()
        timerJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val delta = now - lastTickTime
                lastTickTime = now

                if (!_engine.value.isCheckmate && !_engine.value.isStalemate && _matchState.value.isGameActive) {
                    if (_engine.value.turn == PieceColor.WHITE) {
                        _whiteTimeMs.value = (_whiteTimeMs.value - delta).coerceAtLeast(0L)
                        if (_whiteTimeMs.value == 0L) handleTimeout(PieceColor.WHITE)
                    } else {
                        _blackTimeMs.value = (_blackTimeMs.value - delta).coerceAtLeast(0L)
                        if (_blackTimeMs.value == 0L) handleTimeout(PieceColor.BLACK)
                    }
                }
                delay(100)
            }
        }
    }
    
    private fun handleTimeout(color: PieceColor) {
        stopTimer()
        if (color == PieceColor.WHITE) {
            _matchState.value = _matchState.value.copy(scoreBlack = _matchState.value.scoreBlack + 1)
        } else {
            _matchState.value = _matchState.value.copy(scoreWhite = _matchState.value.scoreWhite + 1)
        }
        _matchState.value = _matchState.value.copy(isGameActive = false)
    }

    fun stopTimer() {
        timerJob?.cancel()
    }

    fun offerDraw(color: PieceColor) {
        _drawOffer.value = color
    }

    fun acceptDraw() {
        stopTimer()
        _drawOffer.value = null
        _matchState.value = _matchState.value.copy(isGameActive = false)
    }

    fun declineDraw() {
        _drawOffer.value = null
    }

    fun resign(color: PieceColor) {
        stopTimer()
        if (color == PieceColor.WHITE) {
            _matchState.value = _matchState.value.copy(scoreBlack = _matchState.value.scoreBlack + 1)
        } else {
            _matchState.value = _matchState.value.copy(scoreWhite = _matchState.value.scoreWhite + 1)
        }
        _matchState.value = _matchState.value.copy(isGameActive = false)
    }

    fun returnToSetup() {
        _matchState.value = _matchState.value.copy(isGameActive = false)
        stopTimer()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}