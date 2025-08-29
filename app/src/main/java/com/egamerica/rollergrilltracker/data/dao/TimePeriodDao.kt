package com.yourcompany.rollergrilltracker.data.dao

import androidx.room.*
import com.yourcompany.rollergrilltracker.data.entities.TimePeriod
import kotlinx.coroutines.flow.Flow

@Dao
interface TimePeriodDao {
    @Query("SELECT * FROM time_periods WHERE isActive = 1 ORDER BY displayOrder")
    fun getAllActiveTimePeriods(): Flow<List<TimePeriod>>
    
    @Query("SELECT * FROM time_periods ORDER BY displayOrder")
    fun getAllTimePeriods(): Flow<List<TimePeriod>>
    
    @Query("SELECT * FROM time_periods WHERE isActive = 1 AND is24HourOnly = 0 ORDER BY displayOrder")
    fun getRegularTimePeriods(): Flow<List<TimePeriod>>
    
    @Query("SELECT * FROM time_periods WHERE isActive = 1 AND is24HourOnly = 1 ORDER BY displayOrder")
    fun get24HourOnlyTimePeriods(): Flow<List<TimePeriod>>
    
    @Query("SELECT * FROM time_periods WHERE id = :id")
    suspend fun getTimePeriodById(id: Int): TimePeriod?
    
    @Query("""
        SELECT * FROM time_periods 
        WHERE isActive = 1 AND :time >= startTime AND :time < endTime 
        LIMIT 1
    """)
    suspend fun getTimePeriodForTime(time: String): TimePeriod?
    
    @Query("""
        SELECT * FROM time_periods 
        WHERE isActive = 1 AND (:time >= startTime AND :time < endTime) OR 
              (startTime > endTime AND (:time >= startTime OR :time < endTime))
        LIMIT 1
    """)
    suspend fun getTimePeriodForTimeWithOvernight(time: String): TimePeriod?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timePeriod: TimePeriod): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(timePeriods: List<TimePeriod>)
    
    @Update
    suspend fun update(timePeriod: TimePeriod)
    
    @Delete
    suspend fun delete(timePeriod: TimePeriod)
    
    @Query("UPDATE time_periods SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveStatus(id: Int, isActive: Boolean)
    
    @Query("UPDATE time_periods SET startTime = :startTime, endTime = :endTime WHERE id = :id")
    suspend fun updateTimePeriodTimes(id: Int, startTime: String, endTime: String)
}