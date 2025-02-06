// app/src/main/java/com/example/plyviewer/MyApplication.kt
package com.example.plyviewer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialization if needed.
    }
}
