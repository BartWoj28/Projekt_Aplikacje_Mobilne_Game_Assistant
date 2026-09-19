package com.example.projektmobilki.util

import android.content.Context
import android.content.SharedPreferences
import com.example.projektmobilki.models.GameHistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GameHistoryRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("game_history", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _history = MutableStateFlow<List<GameHistoryEntry>>(loadHistory())
    val history: StateFlow<List<GameHistoryEntry>> = _history

    private fun loadHistory(): List<GameHistoryEntry> {
        val jsonStr = prefs.getString("history_list", null) ?: return emptyList()
        return try {
            json.decodeFromString<List<GameHistoryEntry>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addGame(entry: GameHistoryEntry) {
        val updatedList = listOf(entry) + _history.value
        _history.value = updatedList
        prefs.edit().putString("history_list", json.encodeToString(updatedList)).apply()
    }

    fun clearHistory() {
        _history.value = emptyList()
        prefs.edit().remove("history_list").apply()
    }

    companion object {
        @Volatile
        private var INSTANCE: GameHistoryRepository? = null

        fun getInstance(context: Context): GameHistoryRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GameHistoryRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
