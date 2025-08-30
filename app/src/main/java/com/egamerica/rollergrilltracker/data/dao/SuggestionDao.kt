package com.egamerica.rollergrilltracker.data.dao

import androidx.room.*
import com.egamerica.rollergrilltracker.data.entities.Suggestion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SuggestionDao {
    @Query("SELECT * FROM suggestions WHERE date = :date AND timePeriodId = :timePeriodId")
    fun getSuggestionsByDateAndTimePeriod(date: LocalDate, timePeriodId: Int): Flow<List<Suggestion>>
    
    @Query("""
        SELECT s.id, s.date, s.timePeriodId, s.productId, s.suggestedQuantity, s.confidenceScore, p.name, p.category
        FROM suggestions s
        INNER JOIN products p ON s.productId = p.id
        WHERE s.date = :date AND s.timePeriodId = :timePeriodId
        ORDER BY s.suggestedQuantity DESC
    """)
    fun getSuggestionsWithProductInfo(date: LocalDate, timePeriodId: Int): Flow<List<SuggestionWithProduct>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(suggestion: Suggestion): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(suggestions: List<Suggestion>)
    
    @Update
    suspend fun update(suggestion: Suggestion)
    
    @Delete
    suspend fun delete(suggestion: Suggestion)
    
    @Query("DELETE FROM suggestions WHERE date = :date AND timePeriodId = :timePeriodId")
    suspend fun deleteByDateAndTimePeriod(date: LocalDate, timePeriodId: Int)
    
    @Query("DELETE FROM suggestions WHERE date < :date")
    suspend fun deleteOldSuggestions(date: LocalDate)
}

data class SuggestionWithProduct(
    val id: Int,
    val date: LocalDate,
    val timePeriodId: Int,
    val productId: Int,
    val suggestedQuantity: Int,
    val confidenceScore: Float,
    val name: String,
    val category: String
)