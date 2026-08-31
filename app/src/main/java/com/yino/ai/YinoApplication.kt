package com.yino.ai

import android.app.Application
import com.google.dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class YinoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // NO inicializar YinoGraph aquí - se hace en MainActivity tras UI lista
    }
}
