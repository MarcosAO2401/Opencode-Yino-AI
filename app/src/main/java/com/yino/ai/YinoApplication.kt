package com.yino.ai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class YinoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
