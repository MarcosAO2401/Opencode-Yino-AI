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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializar YinoGraph en segundo bloqueo, sin bloquear UI
        lifecycleScope.launch(Dispatchers.IO) {
            try { YinoGraph.init(this@MainActivity) } catch (_: Exception) {}
        }
        // Renderizar UI de inmediato (mostrará pantalla de carga si init aún no terminó)
        setContent { YinoApp() }
    }
}
