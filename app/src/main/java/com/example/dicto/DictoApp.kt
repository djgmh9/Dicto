package com.example.dicto

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DictoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // App initialization happens here
    }
}

