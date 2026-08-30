package com.yino.ai.voice

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel

/**
 * Records 16 kHz mono float PCM for voice authentication.
 * Feeds raw FloatArray chunks (16 kHz) to IdentityGate for enrollment/verification.
 */
class AudioRecorder(
    private val sampleRate: Int = 16000,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_FLOAT
) {

    private var recorder: AudioRecord? = null
    private var isRecording = false
    private val bufferSize by lazy {
        val size = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        require(size > 0) { "AudioRecord.getMinBufferSize returned error: $size" }
        size * 2
    }

    /**
     * Start recording and return a channel of FloatArray chunks (16 kHz float PCM).
     * Each chunk is bufferSize/4 floats (4 bytes per float).
     */
    fun start(): Channel<FloatArray> {
        if (isRecording) throw IllegalStateException("Already recording")
        val channel = Channel<FloatArray>(capacity = 10)

        recorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        ).also { it.startRecording() }

        isRecording = true

        CoroutineScope(Dispatchers.IO).launch {
            val floatBuffer = FloatArray(bufferSize / 4)
            while (isRecording && !channel.isClosedForSend) {
                val read = recorder?.read(floatBuffer, 0, floatBuffer.size, AudioRecord.READ_NON_BLOCKING) ?: 0
                if (read > 0) {
                    val chunk = FloatArray(read)
                    System.arraycopy(floatBuffer, 0, chunk, 0, read)
                    channel.send(chunk)
                }
            }
            channel.close()
        }

        return channel
    }

    fun stop() {
        isRecording = false
        recorder?.stop()
        recorder?.release()
        recorder = null
    }

    suspend fun recordFixedDuration(durationMs: Long): List<FloatArray> = withContext(Dispatchers.IO) {
        val channel = start()
        val chunks = mutableListOf<FloatArray>()
        val startTime = System.currentTimeMillis()
        for (chunk in channel) {
            chunks.add(chunk)
            if (System.currentTimeMillis() - startTime >= durationMs) break
        }
        stop()
        chunks
    }
}