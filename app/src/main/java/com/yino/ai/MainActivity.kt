package com.yino.ai

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.yino.ai.core.YinoGraph
import com.yino.ai.ui.MainScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {

    private var isGraphReady by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val msg = "${java.time.Instant.now()} CRASH en $thread: ${throwable.message}\n${Log.getStackTraceString(throwable)}"
            Log.e("YinoAI", "UNCAUGHT: $msg")
            try { File(cacheDir, "yino_crash.log").writeText(msg) } catch (_: Exception) {}
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (isGraphReady) {
                        MainScreen()
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                YinoGraph.init(this@MainActivity)
                withContext(Dispatchers.Main) { isGraphReady = true }
            } catch (e: Exception) {
                val msg = "${java.time.Instant.now()} YinoGraph.init: ${e.message}\n${Log.getStackTraceString(e)}"
                Log.e("YinoAI", "INIT_CRASH: $msg")
                try { File(cacheDir, "yino_crash.log").writeText(msg) } catch (_: Exception) {}
                withContext(Dispatchers.Main) { isGraphReady = true }
            }
        }
    }
}
