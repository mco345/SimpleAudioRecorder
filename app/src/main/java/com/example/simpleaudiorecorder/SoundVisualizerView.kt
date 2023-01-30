package com.example.simpleaudiorecorder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View

class SoundVisualizerView(
    context: Context,
    attrs: AttributeSet? = null
): View(context, attrs) {
    var onRequestCurrentAmplitude: (() -> Int)? = null

    @RequiresApi(Build.VERSION_CODES.M)
    private val amplitudePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.purple_500)    // 색상 : 보라색
        strokeWidth = LINE_WIDTH    // 선의 넓이
        strokeCap = Paint.Cap.ROUND     // 선의 양 끝 처리 : 둥글게

    }
    private var drawingWidth: Int = 0
    private var drawingHeight: Int = 0
    private var drawingAmplitudes: List<Int> = emptyList()
    private var isReplaying: Boolean = false
    private var replayingPosition: Int = 0

    private val visualizeRepeatAction: Runnable = object : Runnable {
        override fun run() {
            if(!isReplaying){
                val currentAmplitude = onRequestCurrentAmplitude?.invoke() ?: 0

                // Amplitude, Draw
                drawingAmplitudes = listOf(currentAmplitude) + drawingAmplitudes
            }else{
                replayingPosition++
            }

            invalidate()    // 뷰 갱신

            handler?.postDelayed(this, ACTION_INTERVAL)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawingWidth = w
        drawingHeight = h
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return

        val centerY = drawingHeight / 2f    // 중앙
        var offsetX = drawingWidth.toFloat() // 가장 오른쪽

        drawingAmplitudes
            .let{ amplitudes ->
                if(isReplaying){
                    amplitudes.takeLast(replayingPosition)
                }else{
                    amplitudes
                }
            }
            .forEach { amplitude ->
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

    fun startVisualizing(isReplaying: Boolean){
        this.isReplaying = isReplaying
        handler?.post(visualizeRepeatAction)
    }

    fun stopVisualizing(){
        replayingPosition = 0
        handler?.removeCallbacks(visualizeRepeatAction)
    }

    fun clearVisualization(){
        drawingAmplitudes = emptyList()
        invalidate()
    }

    companion object{
        private const val LINE_WIDTH = 10f
        private const val LINE_SPACE = 15f
        private const val MAX_AMPLITUDE = Short.MAX_VALUE.toFloat()   // 32767
        private const val ACTION_INTERVAL = 20L
    }
}