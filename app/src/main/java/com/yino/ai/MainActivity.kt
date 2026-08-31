package com.yino.ai

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.yino.ai.core.YinoGraph
import com.yino.ai.ui.MainScreen
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Captura CUALQUIER crash en UI y lo escribe a archivo
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val msg = "${java.time.Instant.now()} CRASH en $thread: ${throwable.message}\n${throwable.stackTrace.joinToString("\n")}"
            Log.e("YinoAI", "UNCAUGHT: $msg")
            try {
                File(cacheDir, "yino_crash.log").writeText(msg)
            } catch (e: Exception) { /* ignore */ }
        }

        try {
            YinoGraph.init(this)
            setContent { MainScreen() }
        } catch (e: Exception) {
            val msg = "${java.time.Instant.now()} MainActivity.onCreate: ${e.message}\n${e.stackTrace.joinToString("\n")}"
            Log.e("YinoAI", "ON_CREATE_CRASH: $msg")
            try { File(cacheDir, "yino_crash.log").writeText(msg) } catch (_: Exception) {}
            throw e
        }
    }
}
