package com.example.gamesassistant.mechanics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class SevenSegmentView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var timeText: String = "0.00"
        set(value) {
            field = value
            invalidate()
        }

    // Tło wyświetlacza (ciemnozielone)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0F3818.toInt() 
        style = Paint.Style.FILL
    }
    
    // Brązowa obwódka / rama urządzenia
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF5D4037.toInt() 
        style = Paint.Style.STROKE
        strokeWidth = 16f
    }

    // Jasny, bursztynowy "piksel" diody (styl starych tablic dworcowych/autobusowych)
    private val onPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFC107.toInt() 
        style = Paint.Style.FILL
    }
    
    // Zgaszony "piksel" wpasowujący się w tło
    private val offPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF174D23.toInt() 
        style = Paint.Style.FILL
    }

    private val digitMap = arrayOf(
        booleanArrayOf(true, true, true, true, true, true, false), // 0
        booleanArrayOf(false, true, true, false, false, false, false), // 1
        booleanArrayOf(true, true, false, true, true, false, true), // 2
        booleanArrayOf(true, true, true, true, false, false, true), // 3
        booleanArrayOf(false, true, true, false, false, true, true), // 4
        booleanArrayOf(true, false, true, true, false, true, true), // 5
        booleanArrayOf(true, false, true, true, true, true, true), // 6
        booleanArrayOf(true, true, true, false, false, false, false), // 7
        booleanArrayOf(true, true, true, true, true, true, true), // 8
        booleanArrayOf(true, true, true, true, false, true, true)  // 9
    )

    private val bgRect = RectF()
    private val borderRect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Rysowanie tła
        bgRect.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRect(bgRect, bgPaint)
        
        // Rysowanie brązowej ramki
        val bInset = borderPaint.strokeWidth / 2f
        borderRect.set(bInset, bInset, width - bInset, height - bInset)
        canvas.drawRect(borderRect, borderPaint)
        
        var digitW = height * 0.4f
        var digitSpacing = digitW * 0.25f
        var dotW = digitW * 0.25f
        
        // Oblicz wymaganą szerokość
        var totalW = 0f
        for (i in timeText.indices) {
            val c = timeText[i]
            if (c == '.' || c == ':') totalW += dotW
            else totalW += digitW
            
            if (i < timeText.length - 1) totalW += digitSpacing
        }
        
        // Jeśli tekst jest za szeroki, przeskaluj go by zmieścił się na ekranie (odstęp od ramki)
        if (totalW > width * 0.85f) {
            val scale = (width * 0.85f) / totalW
            digitW *= scale
            digitSpacing *= scale
            dotW *= scale
            totalW *= scale
        }
        
        var currentX = (width - totalW) / 2f
        if (currentX < 0) currentX = 0f
        
        val t = digitW * 0.15f // Baza do grubości/wielkości kropek
        val p = height * 0.2f // Padding
        val radius = t / 1.7f // Promień pojedynczego kropkowego piksela
        
        for (char in timeText) {
            if (char == '.' || char == ':') {
                canvas.drawCircle(currentX + dotW/2f, height - p - t/2f, radius, onPaint)
                currentX += dotW + digitSpacing
            } else if (char.isDigit()) {
                val digit = char - '0'
                drawDigit(canvas, currentX, p, digitW, height - p * 2f, t, radius, digit)
                currentX += digitW + digitSpacing
            }
        }
    }
    
    private fun drawDigit(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, t: Float, radius: Float, digit: Int) {
        val s = digitMap[digit]
        val gap = t * 0.6f // Odstęp od rogów dla segmentów
        
        // Funkcja rysująca poziomy segment jako ciąg 4 punktów (pikseli)
        fun drawDotsH(segX: Float, segY: Float, isOn: Boolean) {
            val startX = segX + t/2f + gap
            val endX = segX + w - t/2f - gap
            val numDots = 4
            val paint = if(isOn) onPaint else offPaint
            for (i in 0 until numDots) {
                val cx = startX + (endX - startX) * i / (numDots - 1).toFloat()
                canvas.drawCircle(cx, segY, radius, paint)
            }
        }
        
        // Funkcja rysująca pionowy segment jako ciąg 4 punktów (pikseli)
        fun drawDotsV(segX: Float, segY: Float, isOn: Boolean) {
            val startY = segY + t/2f + gap
            val endY = segY + h/2f - t/2f - gap
            val numDots = 4
            val paint = if(isOn) onPaint else offPaint
            for (i in 0 until numDots) {
                val cy = startY + (endY - startY) * i / (numDots - 1).toFloat()
                canvas.drawCircle(segX, cy, radius, paint)
            }
        }

        drawDotsH(x, y + t/2f, s[0])          // A
        drawDotsV(x + w - t/2f, y, s[1])      // B
        drawDotsV(x + w - t/2f, y + h/2f, s[2])// C
        drawDotsH(x, y + h - t/2f, s[3])      // D
        drawDotsV(x + t/2f, y + h/2f, s[4])   // E
        drawDotsV(x + t/2f, y, s[5])          // F
        drawDotsH(x, y + h/2f, s[6])          // G
    }
}