package com.example.projektmobilki.ui.viewmodels

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektmobilki.models.GameState
import com.example.projektmobilki.models.Player
import com.example.projektmobilki.models.TimerType
import com.example.projektmobilki.util.SettingsRepository
import com.example.projektmobilki.util.TTSHelper
import com.example.projektmobilki.util.VibrationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val ttsHelper = TTSHelper(application)
    private val vibrationHelper = VibrationHelper(application)

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _timerType = MutableStateFlow(TimerType.TURN_30S)
    val timerType: StateFlow<TimerType> = _timerType.asStateFlow()

    private val _timeRemaining = MutableStateFlow(30000L)
    val timeRemaining: StateFlow<Long> = _timeRemaining.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    // Chess Clock specific
    private val _chessTime1 = MutableStateFlow(300000L) // 5 minutes default
    val chessTime1: StateFlow<Long> = _chessTime1.asStateFlow()

    private val _chessTime2 = MutableStateFlow(300000L)
    val chessTime2: StateFlow<Long> = _chessTime2.asStateFlow()

    private val _activeChessPlayer = MutableStateFlow(1)
    val activeChessPlayer: StateFlow<Int> = _activeChessPlayer.asStateFlow()

    private val _hasActiveGame = MutableStateFlow(false)
    val hasActiveGame: StateFlow<Boolean> = _hasActiveGame.asStateFlow()

    private var isVibrationAlertTriggered = false
    private var gameStartTime: Long = 0L
    private var currentGameId: String? = null

    private var timerJob: Job? = null

    init {
        resetTimer()
        loadLastPlayers()
    }

    private fun checkActiveGameState() {
        val hasScore = _gameState.value.players.any { it.score != 0 }
        val timerStarted = gameStartTime > 0L
        _hasActiveGame.value = hasScore || timerStarted
    }

    fun initializeGame(gameId: String) {
        if (currentGameId != gameId) {
            currentGameId = gameId
            gameStartTime = 0L
            loadLastPlayers()
            resetTimer()
        }
        checkActiveGameState()
    }

    fun endCurrentGame() {
        currentGameId = null
        gameStartTime = 0L
        checkActiveGameState()
    }

    private fun loadLastPlayers() {
        val lastPlayers = SettingsRepository.getInstance(getApplication()).getLastPlayers()
        val players = lastPlayers.map { name ->
            Player(UUID.randomUUID().toString(), name, 0)
        }
        _gameState.value = _gameState.value.copy(players = players, currentPlayerIndex = 0, roundCount = 1)
    }

    private fun saveLastPlayers() {
        val names = _gameState.value.players.map { it.name }
        SettingsRepository.getInstance(getApplication()).saveLastPlayers(names)
    }

    fun addPlayer(name: String) {
        val newPlayer = Player(UUID.randomUUID().toString(), name)
        _gameState.value = _gameState.value.copy(
            players = _gameState.value.players + newPlayer
        )
        saveLastPlayers()
        ttsHelper.speak("Added $name")
    }

    fun removePlayer(playerId: String) {
        val currentPlayers = _gameState.value.players
        val playerToRemove = currentPlayers.find { it.id == playerId }
        val updatedPlayers = currentPlayers.filter { it.id != playerId }
        val newIndex = if (_gameState.value.currentPlayerIndex >= updatedPlayers.size) {
            maxOf(0, updatedPlayers.size - 1)
        } else {
            _gameState.value.currentPlayerIndex
        }
        _gameState.value = _gameState.value.copy(
            players = updatedPlayers,
            currentPlayerIndex = newIndex
        )
        saveLastPlayers()
        playerToRemove?.let { ttsHelper.speak("Removed ${it.name}") }
    }

    fun clearPlayers() {
        _gameState.value = _gameState.value.copy(
            players = emptyList(),
            currentPlayerIndex = 0,
            roundCount = 1
        )
        saveLastPlayers()
        ttsHelper.speak("Cleared all players")
    }

    fun shufflePlayers() {
        _gameState.value = _gameState.value.copy(
            players = _gameState.value.players.shuffled(),
            currentPlayerIndex = 0,
            roundCount = 1
        )
        saveLastPlayers()
        ttsHelper.speak("Players shuffled")
    }

    fun updateScore(playerId: String, delta: Int) {
        val updatedPlayers = _gameState.value.players.map {
            if (it.id == playerId) it.copy(score = it.score + delta) else it
        }
        _gameState.value = _gameState.value.copy(players = updatedPlayers)
        checkActiveGameState()
        
        val player = updatedPlayers.find { it.id == playerId }
        player?.let {
            ttsHelper.speak("${it.name} score is now ${it.score}")
        }
    }

    fun nextTurn() {
        if (_gameState.value.players.isEmpty()) return
        
        val nextIndex = (_gameState.value.currentPlayerIndex + 1) % _gameState.value.players.size
        val nextRound = if (nextIndex == 0) _gameState.value.roundCount + 1 else _gameState.value.roundCount
        
        _gameState.value = _gameState.value.copy(
            currentPlayerIndex = nextIndex,
            roundCount = nextRound
        )
        
        val nextPlayer = _gameState.value.players[nextIndex]
        ttsHelper.speak("It's ${nextPlayer.name}'s turn")
        
        isVibrationAlertTriggered = false
        resetTimer()
        startTimer()
    }

    fun previousTurn() {
        if (_gameState.value.players.isEmpty()) return
        if (_gameState.value.roundCount == 1 && _gameState.value.currentPlayerIndex == 0) return
        
        val prevIndex = if (_gameState.value.currentPlayerIndex - 1 < 0) {
            _gameState.value.players.size - 1
        } else {
            _gameState.value.currentPlayerIndex - 1
        }
        
        val prevRound = if (prevIndex == _gameState.value.players.size - 1) {
            maxOf(1, _gameState.value.roundCount - 1)
        } else {
            _gameState.value.roundCount
        }
        
        _gameState.value = _gameState.value.copy(
            currentPlayerIndex = prevIndex,
            roundCount = prevRound
        )
        
        val prevPlayer = _gameState.value.players[prevIndex]
        ttsHelper.speak("It's ${prevPlayer.name}'s turn")
        
        isVibrationAlertTriggered = false
        resetTimer()
        startTimer()
    }

    fun setTimerType(type: TimerType) {
        _timerType.value = type
        resetTimer()
    }

    fun startTimer() {
        if (gameStartTime == 0L) {
            gameStartTime = System.currentTimeMillis()
            checkActiveGameState()
        }
        if (_isTimerRunning.value) return
        
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            if (_timerType.value == TimerType.CHESS_CLOCK) {
                runChessClock()
            } else {
                runTurnTimer()
            }
        }
    }

    private suspend fun runTurnTimer() {
        if (_timerType.value == TimerType.TURN_UNLIMITED) {
            _isTimerRunning.value = false
            return
        }
        
        while (_timeRemaining.value > 0 && _isTimerRunning.value) {
            delay(100)
            _timeRemaining.value -= 100
            
            if (_timeRemaining.value <= 10000L && !isVibrationAlertTriggered) {
                vibrationHelper.alert()
                isVibrationAlertTriggered = true
            }
        }
        if (_timeRemaining.value <= 0) {
            _isTimerRunning.value = false
            try {
                if (SettingsRepository.getInstance(getApplication()).isSoundEnabled.value) {
                    val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                    toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            ttsHelper.speak("Time is up!")
        }
    }

    private suspend fun runChessClock() {
        while (_isTimerRunning.value) {
            delay(100)
            if (_activeChessPlayer.value == 1) {
                _chessTime1.value -= 100
                if (_chessTime1.value <= 0) {
                    _isTimerRunning.value = false
                    try {
                        if (SettingsRepository.getInstance(getApplication()).isSoundEnabled.value) {
                            val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                            toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    ttsHelper.speak("Player 1 out of time")
                }
            } else {
                _chessTime2.value -= 100
                if (_chessTime2.value <= 0) {
                    _isTimerRunning.value = false
                    try {
                        if (SettingsRepository.getInstance(getApplication()).isSoundEnabled.value) {
                            val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                            toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    ttsHelper.speak("Player 2 out of time")
                }
            }
        }
    }

    fun toggleChessPlayer() {
        _activeChessPlayer.value = if (_activeChessPlayer.value == 1) 2 else 1
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        val duration = when (_timerType.value) {
            TimerType.TURN_10S -> 10000L
            TimerType.TURN_30S -> 30000L
            TimerType.TURN_60S -> 60000L
            TimerType.TURN_90S -> 90000L
            TimerType.TURN_UNLIMITED -> -1L
            TimerType.CHESS_CLOCK -> 300000L // 5 mins
        }
        _timeRemaining.value = duration
        _chessTime1.value = 300000L
        _chessTime2.value = 300000L
        isVibrationAlertTriggered = false
    }

    fun getGameDurationSeconds(): Long {
        if (gameStartTime == 0L) return 0L
        return (System.currentTimeMillis() - gameStartTime) / 1000L
    }

    override fun onCleared() {
        super.onCleared()
        pauseTimer()
        ttsHelper.shutdown()
    }
}
