package com.yourcompany.rollergrilltracker.util

import com.yourcompany.rollergrilltracker.data.dao.ProductSalesByTimePeriod
import com.yourcompany.rollergrilltracker.data.dao.ProductWasteByTimePeriod
import com.yourcompany.rollergrilltracker.data.entities.Product
import com.yourcompany.rollergrilltracker.data.entities.Suggestion
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Engine for generating product quantity suggestions based on historical sales and waste data.
 */
class SuggestionEngine {

    /**
     * Generate suggestions for products based on historical sales and waste data.
     *
     * @param date The date for which to generate suggestions
     * @param timePeriodId The time period ID
     * @param products List of active products
     * @param historicalSales Historical sales data by product and time period
     * @param historicalWaste Historical waste data by product and time period
     * @param dayOfWeekWeights Map of day of week to weight factor (1.0 is normal)
     * @return List of suggestions with product IDs and quantities
     */
    fun generateSuggestions(
        date: LocalDate,
        timePeriodId: Int,
        products: List<Product>,
        historicalSales: List<ProductSalesByTimePeriod>,
        historicalWaste: List<ProductWasteByTimePeriod>,
        dayOfWeekWeights: Map<DayOfWeek, Float> = DEFAULT_DAY_WEIGHTS
    ): List<Suggestion> {
        val dayOfWeek = date.dayOfWeek
        val dayWeight = dayOfWeekWeights[dayOfWeek] ?: 1.0f
        
        return products.map { product ->
            // Filter sales data for this product and time period
            val salesForProduct = historicalSales.filter { 
                it.productId == product.id && it.timePeriodId == timePeriodId 
            }
            
            // Calculate average sales
            val avgSales = if (salesForProduct.isNotEmpty()) {
                salesForProduct.sumOf { it.totalQuantity }.toFloat() / salesForProduct.size
            } else {
                // Default value if no sales data
                getDefaultQuantity(product.category).toFloat()
            }
            
            // Filter waste data for this product and time period
            val wasteForProduct = historicalWaste.filter { 
                it.productId == product.id && it.timePeriodId == timePeriodId 
            }
            
            // Calculate average waste
            val avgWaste = if (wasteForProduct.isNotEmpty()) {
                wasteForProduct.sumOf { it.totalQuantity }.toFloat() / wasteForProduct.size
            } else {
                0f
            }
            
            // Calculate waste percentage
            val wastePercentage = if (avgSales > 0.001f) {
                min(avgWaste / avgSales, 1.0f) // Cap at 100%
            } else {
                0f
            }
            
            // Calculate suggested quantity using our algorithm
            val baseQuantity = avgSales * (1.0f - wastePercentage * WASTE_REDUCTION_FACTOR)
            
            // Apply day of week weight
            val weightedQuantity = baseQuantity * dayWeight
            
            // Apply minimum and maximum constraints
            val suggestedQuantity = max(
                DEFAULT_MIN_QUANTITY,
                min(weightedQuantity.roundToInt(), DEFAULT_MAX_QUANTITY)
            )
            
            // Calculate confidence score based on data points
            val confidenceScore = calculateConfidenceScore(salesForProduct.size)
            
            Suggestion(
                date = date,
                timePeriodId = timePeriodId,
                productId = product.id,
                suggestedQuantity = suggestedQuantity,
                confidenceScore = confidenceScore
            )
        }
    }
    
    /**
     * Calculate a confidence score based on the number of data points.
     * More data points = higher confidence.
     */
    private fun calculateConfidenceScore(dataPoints: Int): Float {
        return when {
            dataPoints == 0 -> 0.1f
            dataPoints == 1 -> 0.3f
            dataPoints == 2 -> 0.5f
            dataPoints == 3 -> 0.7f
            dataPoints in 4..6 -> 0.8f
            dataPoints in 7..13 -> 0.9f
            else -> 0.95f
        }
    }
    
    /**
     * Get default quantity based on product category.
     */
    private fun getDefaultQuantity(category: String): Int {
        return when (category.lowercase()) {
            "hot dog" -> 8
            "tornado" -> 6
            "rollerbite" -> 6
            "brat" -> 4
            "egg roll" -> 4
            "sausage" -> 6
            "tamale" -> 4
            else -> 4
        }
    }
    
    companion object {
        // Constants for the suggestion algorithm
        private const val WASTE_REDUCTION_FACTOR = 0.7f // How much to reduce based on waste percentage
        private const val DEFAULT_MIN_QUANTITY = 2 // Minimum suggested quantity
        private const val DEFAULT_MAX_QUANTITY = 20 // Maximum suggested quantity
        
        // Default day of week weights
        val DEFAULT_DAY_WEIGHTS = mapOf(
            DayOfWeek.MONDAY to 0.9f,
            DayOfWeek.TUESDAY to 0.85f,
            DayOfWeek.WEDNESDAY to 0.9f,
            DayOfWeek.THURSDAY to 1.0f,
            DayOfWeek.FRIDAY to 1.2f,
            DayOfWeek.SATURDAY to 1.3f,
            DayOfWeek.SUNDAY to 1.1f
        )
    }
}