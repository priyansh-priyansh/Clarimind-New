package com.example.clarimind

import android.app.Application

class ClariMindApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Firebase will be auto-initialized by the google-services.json
    }
} 