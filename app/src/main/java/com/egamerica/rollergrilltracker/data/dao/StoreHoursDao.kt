package com.egamerica.rollergrilltracker.data.dao

import androidx.room.*
import com.egamerica.rollergrilltracker.data.entities.StoreHours
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreHoursDao {
    @Query("SELECT * FROM store_hours ORDER BY dayOfWeek")
    fun getAllStoreHours(): Flow<List<StoreHours>>
    
    @Query("SELECT * FROM store_hours WHERE dayOfWeek = :dayOfWeek")
    suspend fun getStoreHoursForDay(dayOfWeek: Int): StoreHours?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(storeHours: StoreHours): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(storeHours: List<StoreHours>)
    
    @Update
    suspend fun update(storeHours: StoreHours)
    
    @Delete
    suspend fun delete(storeHours: StoreHours)
    
    @Query("UPDATE store_hours SET is24Hours = :is24Hours WHERE dayOfWeek = :dayOfWeek")
    suspend fun update24HourStatus(dayOfWeek: Int, is24Hours: Boolean)
    
    @Query("SELECT COUNT(*) FROM store_hours WHERE is24Hours = 1")
    suspend fun count24HourDays(): Int
    
    @Query("SELECT EXISTS(SELECT 1 FROM store_hours WHERE is24Hours = 1)")
    suspend fun hasAny24HourDays(): Boolean
}