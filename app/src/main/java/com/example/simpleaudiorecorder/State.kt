package com.example.simpleaudiorecorder

enum class State {
    BEFORE_RECORDING,   // 녹음 전
    ON_RECORDING,       // 녹음 중
    AFTER_RECORDING,    // 녹음 후(녹음 끝)
    ON_PLAYING          // 녹음 결과 재생 중
}