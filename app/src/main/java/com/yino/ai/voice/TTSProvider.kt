package com.yino.ai.voice

interface TTSProvider {
    fun speak(text: String)
    fun shutdown()
}
