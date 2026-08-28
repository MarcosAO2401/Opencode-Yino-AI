package com.yino.ai

import android.app.Application
import com.yino.ai.core.YinoGraph
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class YinoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        YinoGraph.init(this)
    }
}
