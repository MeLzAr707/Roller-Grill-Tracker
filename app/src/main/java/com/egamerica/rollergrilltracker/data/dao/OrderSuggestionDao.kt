package com.egamerica.rollergrilltracker.data.dao

import androidx.room.*
import com.egamerica.rollergrilltracker.data.entities.OrderSuggestion
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface OrderSuggestionDao {
    @Query("SELECT * FROM order_suggestions WHERE date = :date ORDER BY productId")
    suspend fun getOrderSuggestionsForDate(date: LocalDate): List<OrderSuggestion>
    
    @Query("""
        SELECT os.id, os.date, os.productId, os.suggestedCases, os.suggestedUnits, os.calculatedAt,
               p.name as productName, p.category, p.unitsPerCase
        FROM order_suggestions os
        INNER JOIN products p ON os.productId = p.id
        WHERE os.date = :date
        ORDER BY p.category, p.name
    """)
    suspend fun getOrderSuggestionsWithProductInfo(date: LocalDate): List<OrderSuggestionWithProduct>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSuggestion(suggestion: OrderSuggestion)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSuggestions(suggestions: List<OrderSuggestion>)
    
    @Query("DELETE FROM order_suggestions WHERE date = :date")
    suspend fun deleteOrderSuggestionsForDate(date: LocalDate)
}

data class OrderSuggestionWithProduct(
    val id: Int,
    val date: LocalDate,
    val productId: Int,
    val suggestedCases: Int,
    val suggestedUnits: Int,
    val calculatedAt: LocalDateTime,
    val productName: String,
    val category: String,
    val unitsPerCase: Int
)