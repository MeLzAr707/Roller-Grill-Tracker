package com.egamerica.rollergrilltracker.data.repositories

import android.util.Log
import com.egamerica.rollergrilltracker.data.dao.OrderSettingsDao
import com.egamerica.rollergrilltracker.data.entities.OrderSettings
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderSettingsRepository @Inject constructor(
    private val orderSettingsDao: OrderSettingsDao
) {
    private val TAG = "OrderSettingsRepository"
    
    suspend fun getOrderSettings(): OrderSettings {
        return try {
            orderSettingsDao.getOrderSettings() ?: createDefaultSettings()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting order settings: ${e.message}", e)
            throw RepositoryException("Failed to get order settings", e)
        }
    }
    
    private suspend fun createDefaultSettings(): OrderSettings {
        val defaultSettings = OrderSettings()
        try {
            orderSettingsDao.insertOrUpdateSettings(defaultSettings)
            return defaultSettings
        } catch (e: Exception) {
            Log.e(TAG, "Error creating default settings: ${e.message}", e)
            throw RepositoryException("Failed to create default settings", e)
        }
    }
    
    suspend fun updateOrderSettings(frequency: Int, days: List<Int>, leadTime: Int) {
        try {
            val daysString = days.joinToString(",")
            orderSettingsDao.updateOrderSettings(
                frequency = frequency,
                days = daysString,
                leadTime = leadTime,
                timestamp = LocalDateTime.now()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order settings: ${e.message}", e)
            throw RepositoryException("Failed to update order settings", e)
        }
    }
    
    suspend fun getNextOrderDates(count: Int = 3): List<LocalDate> {
        try {
            val settings = getOrderSettings()
            val orderDays = settings.orderDays.split(",").map { it.toInt() }
            val today = LocalDate.now()
            
            val result = mutableListOf<LocalDate>()
            var currentDate = today
            
            while (result.size < count) {
                currentDate = currentDate.plusDays(1)
                val dayOfWeek = currentDate.dayOfWeek.value
                
                if (orderDays.contains(dayOfWeek)) {
                    result.add(currentDate)
                }
            }
            
            return result
        } catch (e: Exception) {
            Log.e(TAG, "Error getting next order dates: ${e.message}", e)
            throw RepositoryException("Failed to get next order dates", e)
        }
    }
}