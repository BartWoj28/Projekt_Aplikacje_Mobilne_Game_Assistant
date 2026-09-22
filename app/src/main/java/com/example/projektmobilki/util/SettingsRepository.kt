package com.example.projektmobilki.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _isSoundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled

    private val _isVibrationEnabled = MutableStateFlow(prefs.getBoolean("vibration_enabled", true))
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled

    fun setSoundEnabled(enabled: Boolean) {
        _isSoundEnabled.value = enabled
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _isVibrationEnabled.value = enabled
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
    }

    fun saveLastPlayers(playerNames: List<String>) {
        prefs.edit().putString("last_players", playerNames.joinToString(",")).apply()
    }

    fun getLastPlayers(): List<String> {
        val namesStr = prefs.getString("last_players", "") ?: ""
        if (namesStr.isBlank()) return emptyList()
        return namesStr.split(",")
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
