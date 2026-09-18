package com.example.gamesassistant.mechanics

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class DiceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var diceType: Int = 6 // 4, 6, 10, 20
        set(value) {
            field = value
            this.value = 1
            invalidate()
        }

    var value: Int = 1
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = 60f
        typeface = Typeface.DEFAULT_BOLD
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = 200 // 200px default size
        val size = resolveSize(desiredSize, widthMeasureSpec)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val size = min(width, height) / 2f * 0.9f

        when (diceType) {
            6 -> drawD6(canvas, cx, cy, size)
            4 -> drawD4(canvas, cx, cy, size)
            10 -> drawD10(canvas, cx, cy, size)
            20 -> drawD20(canvas, cx, cy, size)
        }
    }

    private fun drawD6(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val rect = RectF(cx - size, cy - size, cx + size, cy + size)
        canvas.drawRoundRect(rect, size * 0.2f, size * 0.2f, paint)
        canvas.drawRoundRect(rect, size * 0.2f, size * 0.2f, borderPaint)

        val dotRadius = size * 0.15f
        val offset = size * 0.5f

        val drawDot = { dx: Float, dy: Float ->
            canvas.drawCircle(cx + dx, cy + dy, dotRadius, dotPaint)
        }

        when (value) {
            1 -> drawDot(0f, 0f)
            2 -> {
                drawDot(-offset, -offset)
                drawDot(offset, offset)
            }
            3 -> {
                drawDot(-offset, -offset)
                drawDot(0f, 0f)
                drawDot(offset, offset)
            }
            4 -> {
                drawDot(-offset, -offset)
                drawDot(offset, -offset)
                drawDot(-offset, offset)
                drawDot(offset, offset)
            }
            5 -> {
                drawDot(-offset, -offset)
                drawDot(offset, -offset)
                drawDot(0f, 0f)
                drawDot(-offset, offset)
                drawDot(offset, offset)
            }
            6 -> {
                drawDot(-offset, -offset)
                drawDot(-offset, 0f)
                drawDot(-offset, offset)
                drawDot(offset, -offset)
                drawDot(offset, 0f)
                drawDot(offset, offset)
            }
        }
    }

    private fun drawD4(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val path = Path()
        // Triangle pointing up
        path.moveTo(cx, cy - size)
        path.lineTo(cx + size * 0.866f, cy + size * 0.5f)
        path.lineTo(cx - size * 0.866f, cy + size * 0.5f)
        path.close()

        canvas.drawPath(path, paint)
        canvas.drawPath(path, borderPaint)
        
        // Adjust text position slightly for visual centering
        drawCenteredText(canvas, value.toString(), cx, cy + size * 0.2f)
    }

    private fun drawD10(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val path = Path()
        // Kite shape
        path.moveTo(cx, cy - size)
        path.lineTo(cx + size * 0.8f, cy)
        path.lineTo(cx, cy + size)
        path.lineTo(cx - size * 0.8f, cy)
        path.close()

        canvas.drawPath(path, paint)
        canvas.drawPath(path, borderPaint)
        
        drawCenteredText(canvas, value.toString(), cx, cy)
    }

    private fun drawD20(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val path = Path()
        // Hexagon
        for (i in 0 until 6) {
            val angle = Math.PI / 3 * i - Math.PI / 6
            val px = cx + size * cos(angle).toFloat()
            val py = cy + size * sin(angle).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()

        canvas.drawPath(path, paint)
        canvas.drawPath(path, borderPaint)
        
        drawCenteredText(canvas, value.toString(), cx, cy)
    }

    private fun drawCenteredText(canvas: Canvas, text: String, cx: Float, cy: Float) {
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val textHeight = textBounds.height()
        canvas.drawText(text, cx, cy + textHeight / 2f, textPaint)
    }
}
