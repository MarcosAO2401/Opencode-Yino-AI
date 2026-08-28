package com.yino.ai.voice

interface STTProvider {
    fun start()
    fun stop()
    suspend fun listen(): String
}
