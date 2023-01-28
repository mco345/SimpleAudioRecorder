package com.example.simpleaudiorecorder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class SoundVisualizerView(
    context: Context,
    attrs: AttributeSet? = null
): View(context, attrs) {
    @RequiresApi(Build.VERSION_CODES.M)
    val amplitudePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.purple_500)    // 색상 : 보라색
        strokeWidth = LINE_WIDTH    // 선의 넓이
        strokeCap = Paint.Cap.ROUND     // 선의 양 끝 처리 : 둥글게

    }
    var drawingWidth: Int = 0
    var drawingHeight: Int = 0
    var drawingAmplitudes: List<Int> = (0..10).map { Random.nextInt(Short.MAX_VALUE.toInt())}

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawingWidth = w
        drawingHeight = h
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return

        val centerY = drawingHeight / 2f    // 중앙
        var offsetX = drawingWidth.toFloat() // 가장 오른쪽

        drawingAmplitudes.forEach { amplitude ->
            val lineLength = amplitude / MAX_AMPLITUDE * drawingHeight * 0.8f

            offsetX -= LINE_SPACE
            if(offsetX < 0) return@forEach

            canvas.drawLine(
                offsetX,
                centerY - lineLength /2F,
                offsetX,
                centerY + lineLength / 2f,
                amplitudePaint
            )
        }

    }

    companion object{
        private const val LINE_WIDTH = 10f
        private const val LINE_SPACE = 15f
        private const val MAX_AMPLITUDE = Short.MAX_VALUE.toFloat()   // 32767
    }
}