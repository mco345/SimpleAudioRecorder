package com.example.simpleaudiorecorder

import android.content.Context
import android.os.SystemClock
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet

class CountUpView(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    private var startTimeStamp: Long = 0L

    private val countUpAction: Runnable = object: Runnable{
        override fun run() {
            val currentTimeStamp = SystemClock.elapsedRealtime()
            val countTImeSeconds = ((currentTimeStamp - startTimeStamp)/1000L).toInt()

            updateCountTime(countTImeSeconds)

            handler?.postDelayed(this, 1000L)
        }
    }

    fun startCountUp(){
        startTimeStamp = SystemClock.elapsedRealtime()
        handler?.post(countUpAction)
    }

    fun stopCountUp(){
        handler?.removeCallbacks(countUpAction)
    }

    fun clearCountTime(){
        updateCountTime(0)
    }

    private fun updateCountTime(countTImeSeconds: Int){
        val minutes = countTImeSeconds / 60
        val seconds = countTImeSeconds % 60

        text = "%02d:%02d".format(minutes, seconds)
    }


}