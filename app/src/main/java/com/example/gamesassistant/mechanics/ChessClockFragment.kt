package com.example.gamesassistant.mechanics

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gamesassistant.R
import com.google.android.material.button.MaterialButton
import java.util.Locale

class ChessClockFragment : Fragment() {

    private lateinit var etHours: EditText
    private lateinit var etMinutes: EditText
    private lateinit var etSeconds: EditText
    private lateinit var btnStart: Button
    
    private lateinit var llSettings: LinearLayout
    private lateinit var llClockContainer: LinearLayout

    private lateinit var btnWhite: MaterialButton
    private lateinit var tvWhiteTime: TextView
    private lateinit var btnBlack: MaterialButton
    private lateinit var tvBlackTime: TextView
    
    private lateinit var btnPause: Button
    private lateinit var btnReset: Button

    private var whiteTimeMs: Long = 0
    private var blackTimeMs: Long = 0
    
    private var isWhiteTurn = true
    private var isPaused = false
    private var isRunning = false
    private var lastTickTime: Long = 0

    private val handler = Handler(Looper.getMainLooper())
    private val clockRunnable = object : Runnable {
        override fun run() {
            if (isRunning && !isPaused) {
                val now = System.currentTimeMillis()
                val delta = now - lastTickTime
                lastTickTime = now

                if (isWhiteTurn) {
                    whiteTimeMs -= delta
                    if (whiteTimeMs < 0) whiteTimeMs = 0
                } else {
                    blackTimeMs -= delta
                    if (blackTimeMs < 0) blackTimeMs = 0
                }
                
                updateTimeDisplays()
                
                if (whiteTimeMs > 0 && blackTimeMs > 0) {
                    handler.postDelayed(this, 16) // ~60fps for smooth millisecond updates
                } else {
                    isRunning = false
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chess_clock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etHours = view.findViewById(R.id.etHours)
        etMinutes = view.findViewById(R.id.etMinutes)
        etSeconds = view.findViewById(R.id.etSeconds)
        btnStart = view.findViewById(R.id.btnStart)
        
        llSettings = view.findViewById(R.id.llSettings)
        llClockContainer = view.findViewById(R.id.llClockContainer)

        btnWhite = view.findViewById(R.id.btnWhite)
        tvWhiteTime = view.findViewById(R.id.tvWhiteTime)
        btnBlack = view.findViewById(R.id.btnBlack)
        tvBlackTime = view.findViewById(R.id.tvBlackTime)

        btnPause = view.findViewById(R.id.btnPause)
        btnReset = view.findViewById(R.id.btnReset)

        btnStart.setOnClickListener {
            val h = etHours.text.toString().toLongOrNull() ?: 0
            val m = etMinutes.text.toString().toLongOrNull() ?: 0
            val s = etSeconds.text.toString().toLongOrNull() ?: 0
            val totalMs = ((h * 60 + m) * 60 + s) * 1000
            
            if (totalMs > 0) {
                startGame(totalMs)
            }
        }

        btnWhite.setOnClickListener {
            if (isRunning && !isPaused && isWhiteTurn) {
                isWhiteTurn = false
                updateTurnIndicator()
            }
        }

        btnBlack.setOnClickListener {
            if (isRunning && !isPaused && !isWhiteTurn) {
                isWhiteTurn = true
                updateTurnIndicator()
            }
        }

        btnPause.setOnClickListener {
            if (isRunning) {
                isPaused = !isPaused
                btnPause.text = if (isPaused) "Resume" else "Pause"
                
                updateTurnIndicator()
                
                if (!isPaused) {
                    lastTickTime = System.currentTimeMillis()
                    handler.post(clockRunnable)
                }
            }
        }

        btnReset.setOnClickListener {
            resetGame()
        }

        if (savedInstanceState != null) {
            isRunning = savedInstanceState.getBoolean("isRunning", false)
            isPaused = savedInstanceState.getBoolean("isPaused", false)
            isWhiteTurn = savedInstanceState.getBoolean("isWhiteTurn", true)
            whiteTimeMs = savedInstanceState.getLong("whiteTimeMs", 0L)
            blackTimeMs = savedInstanceState.getLong("blackTimeMs", 0L)
            
            if (isRunning) {
                etHours.isEnabled = false
                etMinutes.isEnabled = false
                etSeconds.isEnabled = false
                btnStart.visibility = View.GONE
                
                llClockContainer.visibility = View.VISIBLE
                
                btnPause.text = if (isPaused) "Resume" else "Pause"
                updateTimeDisplays()
                updateTurnIndicator()
                
                if (!isPaused) {
                    lastTickTime = System.currentTimeMillis()
                    handler.post(clockRunnable)
                }
            }
        }
    }

    private fun startGame(timeMs: Long) {
        whiteTimeMs = timeMs
        blackTimeMs = timeMs
        isWhiteTurn = true
        isPaused = false
        isRunning = true
        
        btnPause.text = "Pause"
        
        etHours.isEnabled = false
        etMinutes.isEnabled = false
        etSeconds.isEnabled = false
        btnStart.visibility = View.GONE
        
        llClockContainer.visibility = View.VISIBLE
        
        updateTimeDisplays()
        updateTurnIndicator()
        
        lastTickTime = System.currentTimeMillis()
        handler.post(clockRunnable)
    }
    
    private fun resetGame() {
        isRunning = false
        handler.removeCallbacks(clockRunnable)
        
        etHours.isEnabled = true
        etMinutes.isEnabled = true
        etSeconds.isEnabled = true
        btnStart.visibility = View.VISIBLE
        
        llClockContainer.visibility = View.GONE
        updateTurnIndicator()
    }

    private fun updateTimeDisplays() {
        tvWhiteTime.text = formatTime(whiteTimeMs)
        tvBlackTime.text = formatTime(blackTimeMs)
    }

    private fun formatTime(ms: Long): String {
        if (ms <= 30000) {
            val seconds = ms / 1000
            val tenths = (ms % 1000) / 100
            return String.format(Locale.getDefault(), "%d.%d", seconds, tenths)
        }
        
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    private fun updateTurnIndicator() {
        if (!isRunning) {
            btnWhite.strokeWidth = 0
            btnBlack.strokeWidth = 0
            btnWhite.alpha = 1.0f
            btnBlack.alpha = 1.0f
            return
        }
        
        if (isPaused) {
            btnWhite.alpha = 0.5f
            btnBlack.alpha = 0.5f
            btnWhite.strokeWidth = 0
            btnBlack.strokeWidth = 0
            return
        }
        
        btnWhite.alpha = 1.0f
        btnBlack.alpha = 1.0f
        
        val blueColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))
        val strokePx = (6 * resources.displayMetrics.density).toInt()
        
        if (isWhiteTurn) {
            btnWhite.strokeColor = blueColor
            btnWhite.strokeWidth = strokePx
            btnBlack.strokeWidth = 0
        } else {
            btnBlack.strokeColor = blueColor
            btnBlack.strokeWidth = strokePx
            btnWhite.strokeWidth = 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(clockRunnable)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isRunning", isRunning)
        outState.putBoolean("isPaused", isPaused)
        outState.putBoolean("isWhiteTurn", isWhiteTurn)
        
        var wTime = whiteTimeMs
        var bTime = blackTimeMs
        
        if (isRunning && !isPaused) {
            val now = System.currentTimeMillis()
            val delta = now - lastTickTime
            if (isWhiteTurn) wTime = (wTime - delta).coerceAtLeast(0)
            else bTime = (bTime - delta).coerceAtLeast(0)
        }
        
        outState.putLong("whiteTimeMs", wTime)
        outState.putLong("blackTimeMs", bTime)
    }
}