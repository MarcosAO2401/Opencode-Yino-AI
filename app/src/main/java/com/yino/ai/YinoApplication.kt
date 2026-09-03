package com.yino.ai

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class YinoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Setup global crash interceptor (Logcat version - non-invasive)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("YinoAI-Crash", "Uncaught exception in application", throwable)
            
            // Pass to default handler to keep existing behavior
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
