package com.yourcompany.rollergrilltracker.data.dao

import androidx.room.*
import com.yourcompany.rollergrilltracker.data.entities.Setting
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings WHERE key = :key")
    suspend fun getSettingByKey(key: String): Setting?
    
    @Query("SELECT * FROM settings")
    fun getAllSettings(): Flow<List<Setting>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: Setting): Long
    
    @Update
    suspend fun update(setting: Setting)
    
    @Delete
    suspend fun delete(setting: Setting)
    
    @Query("DELETE FROM settings WHERE key = :key")
    suspend fun deleteByKey(key: String)
}