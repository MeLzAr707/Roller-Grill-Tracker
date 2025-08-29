package com.egamerica.rollergrilltracker.data.dao

import androidx.room.*
import com.egamerica.rollergrilltracker.data.entities.OrderSettings
import java.time.LocalDateTime

@Dao
interface OrderSettingsDao {
    @Query("SELECT * FROM order_settings WHERE id = 1")
    suspend fun getOrderSettings(): OrderSettings?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: OrderSettings)
    
    @Query("UPDATE order_settings SET orderFrequency = :frequency, orderDays = :days, leadTimeDays = :leadTime, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateOrderSettings(frequency: Int, days: String, leadTime: Int, timestamp: LocalDateTime)
}