package com.egamerica.rollergrilltracker.data.repositories

import android.util.Log
import androidx.room.Transaction
import com.egamerica.rollergrilltracker.data.dao.OrderSuggestionDao
import com.egamerica.rollergrilltracker.data.entities.OrderSuggestion
import com.egamerica.rollergrilltracker.data.models.OrderSuggestionWithProduct
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class OrderSuggestionRepository @Inject constructor(
    private val orderSuggestionDao: OrderSuggestionDao,
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository,
    private val orderSettingsRepository: OrderSettingsRepository
) {
    private val TAG = "OrderSuggestionRepository"
    
    suspend fun getOrderSuggestionsForDate(date: LocalDate): List<OrderSuggestionWithProduct> {
        return try {
            orderSuggestionDao.getOrderSuggestionsWithProductInfo(date)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting order suggestions for date: ${e.message}", e)
            throw RepositoryException("Failed to get order suggestions for date", e)
        }
    }
    
    @Transaction
    suspend fun generateOrderSuggestions(date: LocalDate) {
        try {
            // Get all active products
            val products = productRepository.getActiveProducts().first()
            
            // Get current inventory
            val today = LocalDate.now()
            val inventoryCounts = inventoryRepository.getInventoryCountsForDate(today)
            
            // Get usage data for the last 4 weeks
            val fourWeeksAgo = today.minusWeeks(4)
            val usageData = inventoryRepository.getInventoryReportForDateRange(fourWeeksAgo, today)
            
            // Calculate days until the order date
            val daysUntilOrder = ChronoUnit.DAYS.between(today, date).toInt()
            
            // Get order settings
            val orderSettings = orderSettingsRepository.getOrderSettings()
            
            // Calculate days until next delivery after this order
            val orderFrequencyDays = 7 / orderSettings.orderFrequency
            
            // Calculate how many days this order needs to cover
            val daysCovered = orderFrequencyDays + orderSettings.leadTimeDays
            
            // Generate suggestions
            val suggestions = mutableListOf<OrderSuggestion>()
            
            for (product in products) {
                // Skip products with no units per case set
                if (product.unitsPerCase <= 0) continue
                
                // Get current inventory
                val currentInventory = inventoryCounts
                    .find { it.productId == product.id }
                    ?.endingCount ?: 0
                
                // Get average daily usage
                val usageInfo = usageData.find { it.productId == product.id }
                val avgDailyUsage = if (usageInfo != null) {
                    usageInfo.totalUsed.toFloat() / 28 // 4 weeks = 28 days
                } else {
                    // Default to category-based estimate if no usage data
                    getDefaultDailyUsage(product.category)
                }
                
                // Calculate projected usage until delivery
                val projectedUsageUntilDelivery = (avgDailyUsage * (daysUntilOrder + orderSettings.leadTimeDays)).roundToInt()
                
                // Calculate projected inventory at delivery
                val projectedInventory = maxOf(0, currentInventory - projectedUsageUntilDelivery)
                
                // Calculate needed inventory for the coverage period
                val neededForCoverage = (avgDailyUsage * daysCovered).roundToInt()
                
                // Calculate total needed
                val totalNeeded = maxOf(0, neededForCoverage + product.minStockLevel - projectedInventory)
                
                // Calculate cases and units
                val suggestedCases = totalNeeded / product.unitsPerCase
                val suggestedUnits = totalNeeded % product.unitsPerCase
                
                // Add suggestion
                suggestions.add(
                    OrderSuggestion(
                        date = date,
                        productId = product.id,
                        suggestedCases = suggestedCases,
                        suggestedUnits = suggestedUnits
                    )
                )
            }
            
            // Save suggestions
            orderSuggestionDao.deleteOrderSuggestionsForDate(date)
            orderSuggestionDao.insertOrUpdateSuggestions(suggestions)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating order suggestions: ${e.message}", e)
            throw RepositoryException("Failed to generate order suggestions", e)
        }
    }
    
    private fun getDefaultDailyUsage(category: String): Float {
        return when (category.lowercase()) {
            "hot dog" -> 12f
            "tornado" -> 8f
            "rollerbite" -> 8f
            "brat" -> 6f
            "egg roll" -> 6f
            "sausage" -> 8f
            "tamale" -> 6f
            else -> 5f
        }
    }
}