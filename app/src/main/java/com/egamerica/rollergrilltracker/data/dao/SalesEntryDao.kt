package com.egamerica.rollergrilltracker.data.dao

import androidx.room.*
import com.egamerica.rollergrilltracker.data.entities.SalesEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SalesEntryDao {
    @Query("SELECT * FROM sales_entries ORDER BY date DESC, timePeriodId")
    fun getAllSalesEntries(): Flow<List<SalesEntry>>
    
    @Query("SELECT * FROM sales_entries WHERE date = :date ORDER BY timePeriodId")
    fun getSalesEntriesByDate(date: LocalDate): Flow<List<SalesEntry>>
    
    @Query("SELECT * FROM sales_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date, timePeriodId")
    fun getSalesEntriesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<SalesEntry>>
    
    @Query("SELECT * FROM sales_entries WHERE date = :date AND timePeriodId = :timePeriodId")
    suspend fun getSalesEntryByDateAndPeriod(date: LocalDate, timePeriodId: Int): SalesEntry?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(salesEntry: SalesEntry): Long
    
    @Update
    suspend fun update(salesEntry: SalesEntry): Int
    
    @Delete
    suspend fun delete(salesEntry: SalesEntry): Int
}