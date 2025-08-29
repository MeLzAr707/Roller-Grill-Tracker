package com.egamerica.rollergrilltracker

import android.app.Application
import com.egamerica.rollergrilltracker.data.database.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RollerGrillApp : Application() {
    
    @Inject
    lateinit var databaseInitializer: DatabaseInitializer
    
    override fun onCreate() {
        super.onCreate()
        // Initialize the database with default values
        databaseInitializer.initializeDatabase()
    }
}