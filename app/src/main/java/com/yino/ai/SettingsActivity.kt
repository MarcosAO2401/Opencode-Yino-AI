package com.yino.ai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(android.R.layout.simple_list_item_1)
        val textView = findViewById<android.widget.TextView>(android.R.id.text1)
        textView.text = "La configuracion de accesibilidad de Yino se gestiona desde la app principal: Ajustes > Accesibilidad > Yino"
        finish()
    }
}
