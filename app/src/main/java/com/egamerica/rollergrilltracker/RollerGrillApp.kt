package com.yourcompany.rollergrilltracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RollerGrillApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide components here
    }
}