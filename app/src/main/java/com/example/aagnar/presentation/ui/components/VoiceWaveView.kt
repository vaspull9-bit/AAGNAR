package com.example.aagnar.presentation.ui.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.random.Random

class VoiceWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val wavePaint = Paint().apply {
        color = Color.parseColor("#2196F3")
        style = Paint.Style.FILL
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val amplitudes = mutableListOf<Float>()
    private val random = Random(System.currentTimeMillis())

    fun addAmplitude(amplitude: Float) {
        amplitudes.add(amplitude)
        // Ограничиваем количество точек для производительности
        if (amplitudes.size > 100) {
            amplitudes.removeAt(0)
        }
        invalidate()
    }

    fun clear() {
        amplitudes.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (amplitudes.isEmpty()) {
            // Рисуем случайную волну в состоянии покоя
            drawRandomWave(canvas)
        } else {
            // Рисуем волну на основе амплитуд
            drawAmplitudeWave(canvas)
        }
    }

    private fun drawRandomWave(canvas: Canvas) {
        val centerY = height / 2f
        val path = Path()
        path.moveTo(0f, centerY)

        for (x in 0 until width step 5) {
            val y = centerY + random.nextFloat() * 10 - 5
            path.lineTo(x.toFloat(), y)
        }

        canvas.drawPath(path, wavePaint)
    }

    private fun drawAmplitudeWave(canvas: Canvas) {
        val centerY = height / 2f
        val path = Path()
        path.moveTo(0f, centerY)

        val step = width.toFloat() / amplitudes.size

        amplitudes.forEachIndexed { index, amplitude ->
            val x = index * step
            val y = centerY + amplitude * height / 2
            path.lineTo(x, y)
        }

        canvas.drawPath(path, wavePaint)
    }
}