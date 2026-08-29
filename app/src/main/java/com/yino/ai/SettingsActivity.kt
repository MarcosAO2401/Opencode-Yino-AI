package com.yino.ai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

/**
 * Actividad mínima para cumplir con el requisito de configuración del servicio
 * de accesibilidad. Android permite especificar una SettingsActivity en la
 * configuración del AccessibilityService; si no existe, el sistema lanza
 * un error cuando el usuario intenta abrir la configuración del servicio.
 * 
 * Esta actividad simplemente redirige a la pantalla de ajustes principal
 * de la app o muestra un mensaje informativo.
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(android.R.layout.simple_list_item_1)
        val textView = findViewById<android.widget.TextView>(android.R.id.text1)
        textView.text = "La configuración de accesibilidad de Yino se gestiona desde la app principal: Ajustes > Accesibilidad > Yino"
        finish()
    }
}