package com.yourcompany.rollergrilltracker.data.dao

import androidx.room.*
import com.yourcompany.rollergrilltracker.data.entities.GrillConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface GrillConfigDao {
    @Query("SELECT * FROM grill_config ORDER BY grillNumber")
    fun getAllGrillConfigs(): Flow<List<GrillConfig>>
    
    @Query("SELECT * FROM grill_config WHERE isActive = 1 ORDER BY grillNumber")
    fun getActiveGrillConfigs(): Flow<List<GrillConfig>>
    
    @Query("SELECT * FROM grill_config WHERE grillNumber = :grillNumber")
    suspend fun getGrillConfigByNumber(grillNumber: Int): GrillConfig?
    
    @Query("SELECT COUNT(*) FROM grill_config WHERE isActive = 1")
    suspend fun countActiveGrills(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(grillConfig: GrillConfig): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(grillConfigs: List<GrillConfig>)
    
    @Update
    suspend fun update(grillConfig: GrillConfig)
    
    @Delete
    suspend fun delete(grillConfig: GrillConfig)
    
    @Query("UPDATE grill_config SET isActive = :isActive WHERE grillNumber = :grillNumber")
    suspend fun updateActiveStatus(grillNumber: Int, isActive: Boolean)
    
    @Query("UPDATE grill_config SET numberOfSlots = :numberOfSlots WHERE grillNumber = :grillNumber")
    suspend fun updateNumberOfSlots(grillNumber: Int, numberOfSlots: Int)
}