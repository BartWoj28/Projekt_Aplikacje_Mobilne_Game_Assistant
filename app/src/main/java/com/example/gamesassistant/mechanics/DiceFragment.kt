package com.example.gamesassistant.mechanics

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gamesassistant.R
import kotlin.math.sqrt
import kotlin.random.Random

class DiceFragment : Fragment(), SensorEventListener {

    private var diceCount = 1
    private var currentDiceType = 6

    private lateinit var tvDiceCount: TextView
    private lateinit var llDiceContainer: LinearLayout
    private lateinit var spinnerDiceType: Spinner
    
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = SensorManager.GRAVITY_EARTH
    private var lastAcceleration = SensorManager.GRAVITY_EARTH

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDiceCount = view.findViewById(R.id.tvDiceCount)
        llDiceContainer = view.findViewById(R.id.llDiceContainer)
        spinnerDiceType = view.findViewById(R.id.spinnerDiceType)
        
        val btnMinus: Button = view.findViewById(R.id.btnMinus)
        val btnPlus: Button = view.findViewById(R.id.btnPlus)

        if (savedInstanceState != null) {
            diceCount = savedInstanceState.getInt("diceCount", 1)
            currentDiceType = savedInstanceState.getInt("currentDiceType", 6)
        }

        btnMinus.setOnClickListener {
            if (diceCount > 1) {
                diceCount--
                updateDiceViews()
            }
        }

        btnPlus.setOnClickListener {
            if (diceCount < 3) {
                diceCount++
                updateDiceViews()
            }
        }

        spinnerDiceType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentDiceType = when (position) {
                    0 -> 6
                    1 -> 4
                    2 -> 10
                    3 -> 20
                    else -> 6
                }
                updateDiceViews()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        val initialPos = when (currentDiceType) {
            4 -> 1
            10 -> 2
            20 -> 3
            else -> 0
        }
        spinnerDiceType.setSelection(initialPos, false)
        
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        updateDiceViews()
        
        if (savedInstanceState != null) {
            val values = savedInstanceState.getIntegerArrayList("diceValues")
            if (values != null && values.size == diceCount) {
                var index = 0
                for (i in 0 until llDiceContainer.childCount) {
                    val row = llDiceContainer.getChildAt(i) as? LinearLayout
                    row?.let {
                        for (j in 0 until it.childCount) {
                            val child = it.getChildAt(j)
                            if (child is DiceView) {
                                child.value = values[index++]
                            }
                        }
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(
            this,
            sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("diceCount", diceCount)
        outState.putInt("currentDiceType", currentDiceType)
        
        val values = java.util.ArrayList<Int>()
        for (i in 0 until llDiceContainer.childCount) {
            val row = llDiceContainer.getChildAt(i) as? LinearLayout
            row?.let {
                for (j in 0 until it.childCount) {
                    val child = it.getChildAt(j)
                    if (child is DiceView) {
                        values.add(child.value)
                    }
                }
            }
        }
        outState.putIntegerArrayList("diceValues", values)
    }

    private fun updateDiceViews() {
        tvDiceCount.text = diceCount.toString()
        llDiceContainer.removeAllViews()

        val row1 = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = android.view.Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
        }
        val row2 = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = android.view.Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
        }

        llDiceContainer.addView(row1)
        llDiceContainer.addView(row2)

        val diceSizePx = android.util.TypedValue.applyDimension(
            android.util.TypedValue.COMPLEX_UNIT_DIP,
            120f,
            resources.displayMetrics
        ).toInt()
        
        val marginPx = android.util.TypedValue.applyDimension(
            android.util.TypedValue.COMPLEX_UNIT_DIP,
            16f,
            resources.displayMetrics
        ).toInt()

        repeat(diceCount) { index ->
            val diceView = DiceView(requireContext()).apply {
                diceType = currentDiceType
                layoutParams = LinearLayout.LayoutParams(
                    diceSizePx,
                    diceSizePx
                ).apply {
                    setMargins(marginPx, marginPx, marginPx, marginPx)
                }
                setOnClickListener {
                    value = Random.nextInt(1, diceType + 1)
                }
            }
            if (index < 2) {
                row1.addView(diceView)
            } else {
                row2.addView(diceView)
            }
        }
    }
    
    private fun rollAllDice() {
        for (i in 0 until llDiceContainer.childCount) {
            val row = llDiceContainer.getChildAt(i) as? LinearLayout
            row?.let {
                for (j in 0 until it.childCount) {
                    val child = it.getChildAt(j)
                    if (child is DiceView) {
                        child.value = Random.nextInt(1, child.diceType + 1)
                    }
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            lastAcceleration = currentAcceleration
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            if (acceleration > 12) {
                rollAllDice()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}