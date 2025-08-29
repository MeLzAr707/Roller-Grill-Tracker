package com.yourcompany.rollergrilltracker.data.database

import android.util.Log
import com.yourcompany.rollergrilltracker.data.repositories.GrillConfigRepository
import com.yourcompany.rollergrilltracker.data.repositories.StoreHoursRepository
import com.yourcompany.rollergrilltracker.data.repositories.TimePeriodRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class to initialize database with default values for new entities
 */
@Singleton
class DatabaseInitializer @Inject constructor(
    private val grillConfigRepository: GrillConfigRepository,
    private val storeHoursRepository: StoreHoursRepository,
    private val timePeriodRepository: TimePeriodRepository
) {
    private val TAG = "DatabaseInitializer"
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * Initialize the database with default values for new entities
     */
    fun initializeDatabase() {
        coroutineScope.launch {
            try {
                // Initialize default grill configuration
                initializeDefaultGrillConfig()
                
                // Initialize default store hours
                initializeDefaultStoreHours()
                
                // Initialize default time periods
                initializeDefaultTimePeriods()
                
                Log.d(TAG, "Database initialization completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing database: ${e.message}", e)
            }
        }
    }
    
    private suspend fun initializeDefaultGrillConfig() {
        try {
            // Check if any grill configs exist
            val grillCount = grillConfigRepository.countActiveGrills()
            
            if (grillCount == 0) {
                // Initialize default grill config
                grillConfigRepository.initializeDefaultGrillConfig()
                Log.d(TAG, "Default grill configuration initialized")
            } else {
                Log.d(TAG, "Grill configuration already exists, skipping initialization")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing default grill config: ${e.message}", e)
            throw e
        }
    }
    
    private suspend fun initializeDefaultStoreHours() {
        try {
            // Check if any store hours exist
            val storeHours = storeHoursRepository.getAllStoreHours().first()
            
            if (storeHours.isEmpty()) {
                // Initialize default store hours
                storeHoursRepository.initializeDefaultStoreHours()
                Log.d(TAG, "Default store hours initialized")
            } else {
                Log.d(TAG, "Store hours already exist, skipping initialization")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing default store hours: ${e.message}", e)
            throw e
        }
    }
    
    private suspend fun initializeDefaultTimePeriods() {
        try {
            // Check if any time periods exist
            val timePeriods = timePeriodRepository.getAllTimePeriods().first()
            
            if (timePeriods.isEmpty()) {
                // Initialize default time periods
                timePeriodRepository.initializeDefaultTimePeriods()
                Log.d(TAG, "Default time periods initialized")
            } else {
                Log.d(TAG, "Time periods already exist, skipping initialization")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing default time periods: ${e.message}", e)
            throw e
        }
    }
}