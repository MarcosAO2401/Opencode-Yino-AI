package com.yino.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.yino.ai.core.YinoGraph
import com.yino.ai.ui.YinoApp
import com.yino.ai.ui.theme.YinoTheme
import com.yino.ai.ui.theme.YinoColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch(Dispatchers.IO) {
            try { YinoGraph.init(this@MainActivity) } catch (_: Exception) {}
        }
        setContent {
            YinoTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = YinoColors.backgroundPrimary) {
                    YinoApp()
                }
            }
        }
    }
}
