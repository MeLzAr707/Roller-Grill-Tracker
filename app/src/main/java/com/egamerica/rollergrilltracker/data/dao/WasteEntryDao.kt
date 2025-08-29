package com.yourcompany.rollergrilltracker.data.dao

import androidx.room.*
import com.yourcompany.rollergrilltracker.data.entities.WasteEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WasteEntryDao {
    @Query("SELECT * FROM waste_entries ORDER BY date DESC, timePeriodId")
    fun getAllWasteEntries(): Flow<List<WasteEntry>>
    
    @Query("SELECT * FROM waste_entries WHERE date = :date ORDER BY timePeriodId")
    fun getWasteEntriesByDate(date: LocalDate): Flow<List<WasteEntry>>
    
    @Query("SELECT * FROM waste_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date, timePeriodId")
    fun getWasteEntriesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WasteEntry>>
    
    @Query("SELECT * FROM waste_entries WHERE date = :date AND timePeriodId = :timePeriodId")
    suspend fun getWasteEntryByDateAndPeriod(date: LocalDate, timePeriodId: Int): WasteEntry?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wasteEntry: WasteEntry): Long
    
    @Update
    suspend fun update(wasteEntry: WasteEntry)
    
    @Delete
    suspend fun delete(wasteEntry: WasteEntry)
}