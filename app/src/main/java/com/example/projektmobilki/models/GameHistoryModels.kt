package com.example.projektmobilki.models

import kotlinx.serialization.Serializable

@Serializable
data class PlayerResult(
    val name: String,
    val score: Int
)

@Serializable
data class GameHistoryEntry(
    val id: String,
    val dateMillis: Long,
    val playerResults: List<PlayerResult>,
    val winnerName: String,
    val durationSeconds: Long
)
