package com.example.gamesassistant.mechanics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class HourglassView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    var progress: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
        
    private val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF888888.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 12f
        strokeJoin = Paint.Join.ROUND
    }
    
    private val sandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFF4A460.toInt() // Sandy color
        style = Paint.Style.FILL
    }
    
    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF5D4037.toInt() // Brown base
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f
        
        // Zmniejszony rozmiar klepsydry
        val glassHeight = h * 0.65f
        val glassWidth = minOf(w, glassHeight * 0.6f)
        
        val left = cx - glassWidth / 2f
        val right = cx + glassWidth / 2f
        val top = cy - glassHeight / 2f
        val bottom = cy + glassHeight / 2f
        
        val glassPath = Path().apply {
            moveTo(left, top)
            lineTo(right, top)
            cubicTo(right, top + glassHeight * 0.3f, cx + glassWidth * 0.15f, cy - glassHeight * 0.05f, cx, cy)
            cubicTo(cx + glassWidth * 0.15f, cy + glassHeight * 0.05f, right, bottom - glassHeight * 0.3f, right, bottom)
            lineTo(left, bottom)
            cubicTo(left, bottom - glassHeight * 0.3f, cx - glassWidth * 0.15f, cy + glassHeight * 0.05f, cx, cy)
            cubicTo(cx - glassWidth * 0.15f, cy - glassHeight * 0.05f, left, top + glassHeight * 0.3f, left, top)
            close()
        }
        
        canvas.save()
        canvas.clipPath(glassPath)
        
        val topH = cy - top
        val bottomH = bottom - cy
        
        // Górny piasek (maleje wraz z progresem)
        val topSandHeight = topH * (1f - progress)
        if (topSandHeight > 0) {
            canvas.drawRect(left, cy - topSandHeight, right, cy, sandPaint)
        }
        
        // Dolny piasek (rośnie wraz z progresem)
        val bottomSandHeight = bottomH * progress
        if (bottomSandHeight > 0) {
            canvas.drawRect(left, bottom - bottomSandHeight, right, bottom, sandPaint)
        }
        
        canvas.restore()
        
        // Strumień piasku lecący w dół
        if (progress > 0f && progress < 1f) {
            canvas.drawRect(cx - 3f, cy, cx + 3f, bottom - bottomSandHeight, sandPaint)
        }
        
        // Narysuj obramowanie szkła
        canvas.drawPath(glassPath, glassPaint)
        
        // Podstawki (Góra i dół)
        val baseHeight = 24f
        val baseWidth = glassWidth + 40f
        val baseLeft = cx - baseWidth / 2f
        val baseRight = cx + baseWidth / 2f
        
        val baseRadius = 8f
        canvas.drawRoundRect(baseLeft, top - baseHeight, baseRight, top, baseRadius, baseRadius, basePaint)
        canvas.drawRoundRect(baseLeft, bottom, baseRight, bottom + baseHeight, baseRadius, baseRadius, basePaint)
    }
}