package com.example.projektmobilki.ui.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektmobilki.util.SettingsRepository
import com.example.projektmobilki.util.ShakeDetector
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class RandomizerViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val shakeDetector = ShakeDetector { triggerAction() }

    private val _diceResults = MutableStateFlow<List<Int>>(listOf(1))
    val diceResults: StateFlow<List<Int>> = _diceResults.asStateFlow()

    private val _isHeads = MutableStateFlow(true)
    val isHeads: StateFlow<Boolean> = _isHeads.asStateFlow()

    private val _isRolling = MutableStateFlow(false)
    val isRolling: StateFlow<Boolean> = _isRolling.asStateFlow()
    
    private val _activeTab = MutableStateFlow(0) // 0 for Dice, 1 for Coin
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    init {
        // Listening will be controlled by screen lifecycle via startListening/stopListening
    }

    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(shakeDetector, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(shakeDetector)
    }

    fun setActiveTab(tab: Int) {
        _activeTab.value = tab
    }

    fun setDiceCount(count: Int) {
        if (!_isRolling.value) {
            _diceResults.value = List(count) { 1 }
        }
    }

    fun triggerAction() {
        if (_isRolling.value) return
        
        if (_activeTab.value == 0) {
            rollDice()
        } else {
            flipCoin()
        }
    }

    private fun rollDice() {
        viewModelScope.launch {
            _isRolling.value = true
            playSound("dice_roll")
            
            // Simulation of rolling animation
            repeat(10) {
                _diceResults.value = List(_diceResults.value.size) { Random.nextInt(1, 7) }
                delay(100)
            }
            
            _diceResults.value = List(_diceResults.value.size) { Random.nextInt(1, 7) }
            _isRolling.value = false
        }
    }

    private fun flipCoin() {
        viewModelScope.launch {
            _isRolling.value = true
            playSound("coin_flip")
            
            // Simulation of flipping animation
            repeat(10) {
                _isHeads.value = Random.nextBoolean()
                delay(100)
            }
            
            _isHeads.value = Random.nextBoolean()
            _isRolling.value = false
        }
    }

    private fun playSound(soundName: String) {
        if (!SettingsRepository.getInstance(getApplication()).isSoundEnabled.value) return

        val resId = getApplication<Application>().resources.getIdentifier(
            soundName, "raw", getApplication<Application>().packageName
        )
        
        if (resId != 0) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(getApplication(), resId)
            mediaPlayer?.start()
        } else {
            try {
                ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                    .startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(shakeDetector)
        mediaPlayer?.release()
    }
}
