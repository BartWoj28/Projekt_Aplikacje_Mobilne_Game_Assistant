package com.example.gamesassistant.mechanics

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import androidx.fragment.app.Fragment
import com.example.gamesassistant.R
import com.google.android.material.button.MaterialButton
import java.util.Locale

class TimerFragment : Fragment() {

    private lateinit var etSeconds: EditText
    private lateinit var switchMode: Switch
    private lateinit var btnStart: MaterialButton
    
    private lateinit var llSettings: LinearLayout
    private lateinit var llTimerContainer: LinearLayout
    private lateinit var llControls: LinearLayout

    private lateinit var hourglassView: HourglassView
    private lateinit var sevenSegmentView: SevenSegmentView
    
    private lateinit var btnPause: MaterialButton
    private lateinit var btnCancel: MaterialButton

    private var totalTimeMs: Long = 0
    private var remainingTimeMs: Long = 0
    
    private var isPaused = false
    private var isRunning = false
    private var lastTickTime: Long = 0

    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRunning && !isPaused) {
                val now = System.currentTimeMillis()
                val delta = now - lastTickTime
                lastTickTime = now

                remainingTimeMs -= delta
                if (remainingTimeMs <= 0) {
                    remainingTimeMs = 0
                    isRunning = false
                    updateUIStates()
                }
                
                updateDisplays()
                
                if (isRunning) {
                    handler.postDelayed(this, 16) // ~60fps
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_timer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etSeconds = view.findViewById(R.id.etSeconds)
        switchMode = view.findViewById(R.id.switchMode)
        btnStart = view.findViewById(R.id.btnStart)
        
        llSettings = view.findViewById(R.id.llSettings)
        llTimerContainer = view.findViewById(R.id.llTimerContainer)
        llControls = view.findViewById(R.id.llControls)

        hourglassView = view.findViewById(R.id.hourglassView)
        sevenSegmentView = view.findViewById(R.id.sevenSegmentView)
        
        btnPause = view.findViewById(R.id.btnPause)
        btnCancel = view.findViewById(R.id.btnCancel)

        btnStart.setOnClickListener {
            val s = etSeconds.text.toString().toLongOrNull() ?: 0
            val totalMs = s * 1000
            
            if (totalMs > 0) {
                startGame(totalMs)
            }
        }

        btnPause.setOnClickListener {
            if (isRunning || remainingTimeMs > 0) {
                isPaused = !isPaused
                updateUIStates()
                
                if (!isPaused) {
                    lastTickTime = System.currentTimeMillis()
                    handler.post(timerRunnable)
                }
            }
        }

        btnCancel.setOnClickListener {
            cancelGame()
        }

        if (savedInstanceState != null) {
            isRunning = savedInstanceState.getBoolean("isRunning", false)
            isPaused = savedInstanceState.getBoolean("isPaused", false)
            totalTimeMs = savedInstanceState.getLong("totalTimeMs", 0L)
            remainingTimeMs = savedInstanceState.getLong("remainingTimeMs", 0L)
            val savedMode = savedInstanceState.getBoolean("timerMode", false)
            switchMode.isChecked = savedMode
            
            if (isRunning || totalTimeMs > 0) { // Jeżeli timer był aktywny, zachowujemy widok
                llSettings.visibility = View.GONE
                llTimerContainer.visibility = View.VISIBLE
                llControls.visibility = View.VISIBLE
                
                val isSevenSeg = savedMode
                hourglassView.visibility = if (isSevenSeg) View.GONE else View.VISIBLE
                sevenSegmentView.visibility = if (isSevenSeg) View.VISIBLE else View.GONE
                
                updateUIStates()
                updateDisplays()
                
                if (isRunning && !isPaused) {
                    lastTickTime = System.currentTimeMillis()
                    handler.post(timerRunnable)
                }
            }
        }
    }

    private fun startGame(timeMs: Long) {
        totalTimeMs = timeMs
        remainingTimeMs = timeMs
        isPaused = false
        isRunning = true
        
        llSettings.visibility = View.GONE
        llTimerContainer.visibility = View.VISIBLE
        llControls.visibility = View.VISIBLE
        
        val isSevenSeg = switchMode.isChecked
        hourglassView.visibility = if (isSevenSeg) View.GONE else View.VISIBLE
        sevenSegmentView.visibility = if (isSevenSeg) View.VISIBLE else View.GONE
        
        updateUIStates()
        updateDisplays()
        
        lastTickTime = System.currentTimeMillis()
        handler.post(timerRunnable)
    }
    
    private fun cancelGame() {
        isRunning = false
        totalTimeMs = 0
        handler.removeCallbacks(timerRunnable)
        
        llSettings.visibility = View.VISIBLE
        llTimerContainer.visibility = View.GONE
        llControls.visibility = View.GONE
    }

    private fun updateDisplays() {
        if (totalTimeMs > 0) {
            val progress = 1f - (remainingTimeMs.toFloat() / totalTimeMs.toFloat())
            hourglassView.progress = progress.coerceIn(0f, 1f)
        }
        
        val seconds = remainingTimeMs / 1000
        val hundredths = (remainingTimeMs % 1000) / 10
        sevenSegmentView.timeText = String.format(Locale.getDefault(), "%d.%02d", seconds, hundredths)
    }

    private fun updateUIStates() {
        if (!isRunning && remainingTimeMs == 0L) {
            // Koniec odliczania (czas upłynął)
            btnPause.setIconResource(R.drawable.ic_pause)
            btnPause.alpha = 0.5f
            btnPause.isEnabled = false
            
            btnCancel.alpha = 1.0f
            llTimerContainer.alpha = 1.0f
        } else if (isPaused) {
            // Spauzowano
            btnPause.setIconResource(R.drawable.ic_play)
            btnPause.alpha = 1.0f
            btnPause.isEnabled = true
            
            btnCancel.alpha = 1.0f
            llTimerContainer.alpha = 0.5f // Przygaszenie wyświetlacza w trakcie pauzy (zgodnie z życzeniem)
        } else {
            // Działa odliczanie
            btnPause.setIconResource(R.drawable.ic_pause)
            btnPause.alpha = 1.0f
            btnPause.isEnabled = true
            
            btnCancel.alpha = 1.0f
            llTimerContainer.alpha = 1.0f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timerRunnable)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isRunning", isRunning)
        outState.putBoolean("isPaused", isPaused)
        outState.putBoolean("timerMode", switchMode.isChecked)
        
        var rem = remainingTimeMs
        if (isRunning && !isPaused) {
            val now = System.currentTimeMillis()
            rem = (rem - (now - lastTickTime)).coerceAtLeast(0)
        }
        outState.putLong("totalTimeMs", totalTimeMs)
        outState.putLong("remainingTimeMs", rem)
    }
}