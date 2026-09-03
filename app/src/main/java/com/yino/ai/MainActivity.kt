package com.yino.ai

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.yino.ai.core.YinoGraph
import com.yino.ai.ui.YinoApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            var isReady by remember { mutableStateOf(YinoGraph.isInitialized) }
            
            if (isReady) {
                YinoApp()
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        YinoGraph.init(this@MainActivity)
                        isReady = true
                    } catch (e: Exception) {
                        Log.e("YinoAI", "Error inicializando graph", e)
                    }
                }
            }
        }
    }
}
