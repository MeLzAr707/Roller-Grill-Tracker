package com.yourcompany.rollergrilltracker.data.repositories

import android.util.Log
import androidx.room.Transaction
import com.yourcompany.rollergrilltracker.data.dao.SuggestionDao
import com.yourcompany.rollergrilltracker.data.dao.SuggestionWithProduct
import com.yourcompany.rollergrilltracker.data.entities.Suggestion
import com.yourcompany.rollergrilltracker.util.SuggestionEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuggestionRepository @Inject constructor(
    private val suggestionDao: SuggestionDao,
    private val productRepository: ProductRepository,
    private val salesRepository: SalesRepository,
    private val wasteRepository: WasteRepository,
    private val slotRepository: SlotRepository
) {
    private val TAG = "SuggestionRepository"
    private val suggestionEngine = SuggestionEngine()
    
    fun getSuggestionsByDateAndTimePeriod(date: LocalDate, timePeriodId: Int): Flow<List<Suggestion>> {
        return suggestionDao.getSuggestionsByDateAndTimePeriod(date, timePeriodId)
            .catch { e ->
                Log.e(TAG, "Error getting suggestions by date and time period: ${e.message}", e)
                throw RepositoryException("Failed to get suggestions by date and time period", e)
            }
    }
    
    fun getSuggestionsWithProductInfo(date: LocalDate, timePeriodId: Int): Flow<List<SuggestionWithProduct>> {
        return suggestionDao.getSuggestionsWithProductInfo(date, timePeriodId)
            .catch { e ->
                Log.e(TAG, "Error getting suggestions with product info: ${e.message}", e)
                throw RepositoryException("Failed to get suggestions with product info", e)
            }
    }
    
    @Transaction
    suspend fun generateSuggestionsForDateAndPeriod(date: LocalDate, timePeriodId: Int) {
        try {
            // Get active products
            val products = productRepository.getActiveProducts().first()
            
            // Get historical sales data
            val startDate = date.minusDays(28) // Look at the last 4 weeks
            val endDate = date.minusDays(1)
            val historicalSales = salesRepository.getProductSalesByTimePeriod(
                startDate.toString(),
                endDate.toString()
            )
            
            // Get historical waste data
            val historicalWaste = wasteRepository.getProductWasteByTimePeriod(
                startDate.toString(),
                endDate.toString()
            )
            
            // Get assigned product IDs
            val assignedProductIds = slotRepository.getAssignedProductIds()
            
            // Prioritize assigned products
            val prioritizedProducts = products.sortedByDescending { 
                if (assignedProductIds.contains(it.id)) 1 else 0 
            }
            
            // Generate suggestions
            val suggestions = suggestionEngine.generateSuggestions(
                date = date,
                timePeriodId = timePeriodId,
                products = prioritizedProducts,
                historicalSales = historicalSales,
                historicalWaste = historicalWaste
            )
            
            // Delete existing suggestions for this date and time period
            suggestionDao.deleteByDateAndTimePeriod(date, timePeriodId)
            
            // Save new suggestions
            suggestionDao.insertAll(suggestions)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating suggestions: ${e.message}", e)
            throw RepositoryException("Failed to generate suggestions", e)
        }
    }
    
    suspend fun deleteOldSuggestions(date: LocalDate) {
        try {
            suggestionDao.deleteOldSuggestions(date)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting old suggestions: ${e.message}", e)
            throw RepositoryException("Failed to delete old suggestions", e)
        }
    }
}