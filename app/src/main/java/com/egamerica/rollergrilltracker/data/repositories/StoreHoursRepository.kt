package com.egamerica.rollergrilltracker.data.repositories

import android.util.Log
import com.egamerica.rollergrilltracker.data.dao.StoreHoursDao
import com.egamerica.rollergrilltracker.data.entities.StoreHours
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreHoursRepository @Inject constructor(
    private val storeHoursDao: StoreHoursDao
) {
    private val TAG = "StoreHoursRepository"
    
    fun getAllStoreHours(): Flow<List<StoreHours>> {
        return storeHoursDao.getAllStoreHours()
            .catch { e ->
                Log.e(TAG, "Error getting store hours: ${e.message}", e)
                throw RepositoryException("Failed to get store hours", e)
            }
    }
    
    suspend fun getStoreHoursForDay(dayOfWeek: DayOfWeek): StoreHours? {
        return try {
            storeHoursDao.getStoreHoursForDay(dayOfWeek.value)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting store hours for day: ${e.message}", e)
            throw RepositoryException("Failed to get store hours for day", e)
        }
    }
    
    suspend fun saveStoreHours(storeHours: StoreHours): Long {
        return try {
            storeHoursDao.insert(storeHours)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving store hours: ${e.message}", e)
            throw RepositoryException("Failed to save store hours", e)
        }
    }
    
    suspend fun saveAllStoreHours(storeHours: List<StoreHours>) {
        try {
            storeHoursDao.insertAll(storeHours)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving all store hours: ${e.message}", e)
            throw RepositoryException("Failed to save all store hours", e)
        }
    }
    
    suspend fun update24HourStatus(dayOfWeek: DayOfWeek, is24Hours: Boolean) {
        try {
            storeHoursDao.update24HourStatus(dayOfWeek.value, is24Hours)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating 24-hour status: ${e.message}", e)
            throw RepositoryException("Failed to update 24-hour status", e)
        }
    }
    
    suspend fun hasAny24HourDays(): Boolean {
        return try {
            storeHoursDao.hasAny24HourDays()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for 24-hour days: ${e.message}", e)
            throw RepositoryException("Failed to check for 24-hour days", e)
        }
    }
    
    suspend fun initializeDefaultStoreHours() {
        try {
            // Create default store hours for each day of the week
            val defaultStoreHours = DayOfWeek.values().map { day ->
                StoreHours(
                    dayOfWeek = day.value,
                    openTime = "06:00",
                    closeTime = "22:00",
                    is24Hours = false
                )
            }
            storeHoursDao.insertAll(defaultStoreHours)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing default store hours: ${e.message}", e)
            throw RepositoryException("Failed to initialize default store hours", e)
        }
    }
}