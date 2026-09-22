package com.example.projektmobilki.models

data class Player(
    val id: String,
    val name: String,
    val score: Int = 0
)

data class GameState(
    val players: List<Player> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val roundCount: Int = 1,
    val isGameStarted: Boolean = false
) {
    val currentPlayer: Player?
        get() = if (players.isNotEmpty()) players[currentPlayerIndex] else null
}

enum class TimerType {
    TURN_10S,
    TURN_30S,
    TURN_60S,
    TURN_90S,
    TURN_UNLIMITED,
    CHESS_CLOCK
}
