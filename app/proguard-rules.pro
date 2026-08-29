# Yino AI ProGuard/R8 Rules

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class com.yino.ai.**_HiltComponents { *; }
-keep class com.yino.ai.**_GeneratedRootComponent { *; }

# Keep Kotlin serialization
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep Ktor client
-keep class io.ktor.** { *; }
-keepclassmembers class * {
    @io.ktor.serialization.** *;
}

# Keep Room database
-keep class androidx.room.** { *; }
-keep class com.yino.ai.data.memory.** { *; }

# Keep accessibility service
-keep class com.yino.ai.automation.YinoAccessibilityService { *; }

# Keep notification listener
-keep class com.yino.ai.automation.YinoNotificationListener { *; }

# Keep voice service
-keep class com.yino.ai.voice.YinoVoiceService { *; }

# Keep Hilt EntryPoints
-keep class dagger.hilt.android.EntryPointAccessors { *; }

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep BiometricPrompt
-keep class androidx.biometric.** { *; }

# Keep EncryptedSharedPreferences
-keep class androidx.security.crypto.** { *; }

# Keep Vosk
-keep class org.vosk.** { *; }

# Suppress warnings for reflection
-dontwarn kotlinx.serialization.**
-dontwarn io.ktor.**
-dontwarn org.vosk.**

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Optimize
-optimizations !code/simplification/arithmetic
-optimizationpasses 5
