package com.egamerica.rollergrilltracker.util

import com.egamerica.rollergrilltracker.util.SuggestionEngine
import com.egamerica.rollergrilltracker.data.dao.ProductSalesByTimePeriod
import com.egamerica.rollergrilltracker.data.dao.ProductWasteByTimePeriod
import com.egamerica.rollergrilltracker.data.entities.Product
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class SuggestionEngineTest {

    private lateinit var suggestionEngine: SuggestionEngine
    private lateinit var testProducts: List<Product>
    private lateinit var testDate: LocalDate

    @Before
    fun setup() {
        suggestionEngine = SuggestionEngine()
        testDate = LocalDate.of(2025, 8, 28) // Thursday
        
        testProducts = listOf(
            Product(
                id = 1,
                name = "Hot Dog",
                barcode = "123456789",
                category = "Hot Dog",
                active = true,
                inStock = true,
                unitsPerCase = 24,
                minStockLevel = 5,
                maxStockLevel = 20,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            Product(
                id = 2,
                name = "Chicken Roller",
                barcode = "987654321",
                category = "Tornado",
                active = true,
                inStock = true,
                unitsPerCase = 24,
                minStockLevel = 5,
                maxStockLevel = 20,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            Product(
                id = 3,
                name = "Taquito",
                barcode = "456789123",
                category = "RollerBite",
                active = true,
                inStock = true,
                unitsPerCase = 36,
                minStockLevel = 10,
                maxStockLevel = 30,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
    }

    @Test
    fun `generateSuggestions returns default quantities when no historical data`() {
        // Given
        val timePeriodId = 1
        val historicalSales = emptyList<ProductSalesByTimePeriod>()
        val historicalWaste = emptyList<ProductWasteByTimePeriod>()
        
        // When
        val suggestions = suggestionEngine.generateSuggestions(
            testDate,
            timePeriodId,
            testProducts,
            historicalSales,
            historicalWaste
        )
        
        // Then
        assertEquals(3, suggestions.size)
        
        // Hot Dog should have default quantity of 8
        val hotDogSuggestion = suggestions.find { it.productId == 1 }
        assertNotNull(hotDogSuggestion)
        assertEquals(8, hotDogSuggestion!!.suggestedQuantity)
        assertEquals(0.1f, hotDogSuggestion.confidenceScore, 0.001f)
        
        // Tornado should have default quantity of 6
        val tornadoSuggestion = suggestions.find { it.productId == 2 }
        assertNotNull(tornadoSuggestion)
        assertEquals(6, tornadoSuggestion!!.suggestedQuantity)
        assertEquals(0.1f, tornadoSuggestion.confidenceScore, 0.001f)
        
        // RollerBite should have default quantity of 6
        val rollerBiteSuggestion = suggestions.find { it.productId == 3 }
        assertNotNull(rollerBiteSuggestion)
        assertEquals(6, rollerBiteSuggestion!!.suggestedQuantity)
        assertEquals(0.1f, rollerBiteSuggestion.confidenceScore, 0.001f)
    }

    @Test
    fun `generateSuggestions uses historical sales data when available`() {
        // Given
        val timePeriodId = 1
        val historicalSales = listOf(
            ProductSalesByTimePeriod(
                productId = 1,
                name = "Hot Dog",
                category = "Hot Dog",
                totalQuantity = 10,
                timePeriodId = 1
            ),
            ProductSalesByTimePeriod(
                productId = 1,
                name = "Hot Dog",
                category = "Hot Dog",
                totalQuantity = 12,
                timePeriodId = 1
            ),
            ProductSalesByTimePeriod(
                productId = 2,
                name = "Chicken Roller",
                category = "Tornado",
                totalQuantity = 8,
                timePeriodId = 1
            )
        )
        val historicalWaste = emptyList<ProductWasteByTimePeriod>()
        
        // When
        val suggestions = suggestionEngine.generateSuggestions(
            testDate,
            timePeriodId,
            testProducts,
            historicalSales,
            historicalWaste
        )
        
        // Then
        assertEquals(3, suggestions.size)
        
        // Hot Dog should average (10 + 12) / 2 = 11
        val hotDogSuggestion = suggestions.find { it.productId == 1 }
        assertNotNull(hotDogSuggestion)
        assertEquals(11, hotDogSuggestion!!.suggestedQuantity)
        assertEquals(0.5f, hotDogSuggestion.confidenceScore, 0.001f)
        
        // Tornado should use its one data point of 8
        val tornadoSuggestion = suggestions.find { it.productId == 2 }
        assertNotNull(tornadoSuggestion)
        assertEquals(8, tornadoSuggestion!!.suggestedQuantity)
        assertEquals(0.3f, tornadoSuggestion.confidenceScore, 0.001f)
        
        // RollerBite should use default of 6 since no data
        val rollerBiteSuggestion = suggestions.find { it.productId == 3 }
        assertNotNull(rollerBiteSuggestion)
        assertEquals(6, rollerBiteSuggestion!!.suggestedQuantity)
        assertEquals(0.1f, rollerBiteSuggestion.confidenceScore, 0.001f)
    }

    @Test
    fun `generateSuggestions adjusts for waste data`() {
        // Given
        val timePeriodId = 1
        val historicalSales = listOf(
            ProductSalesByTimePeriod(
                productId = 1,
                name = "Hot Dog",
                category = "Hot Dog",
                totalQuantity = 10,
                timePeriodId = 1
            )
        )
        val historicalWaste = listOf(
            ProductWasteByTimePeriod(
                productId = 1,
                name = "Hot Dog",
                category = "Hot Dog",
                totalQuantity = 2, // 20% waste
                timePeriodId = 1
            )
        )
        
        // When
        val suggestions = suggestionEngine.generateSuggestions(
            testDate,
            timePeriodId,
            testProducts.filter { it.id == 1 }, // Just test Hot Dog
            historicalSales,
            historicalWaste
        )
        
        // Then
        assertEquals(1, suggestions.size)
        
        // Hot Dog should be reduced by waste factor: 10 - (2/10 * 0.7 * 10) = 10 - 1.4 = 8.6 ≈ 9
        val hotDogSuggestion = suggestions[0]
        assertEquals(9, hotDogSuggestion.suggestedQuantity)
    }

    @Test
    fun `generateSuggestions applies day of week weights`() {
        // Given
        val timePeriodId = 1
        val historicalSales = listOf(
            ProductSalesByTimePeriod(
                productId = 1,
                name = "Hot Dog",
                category = "Hot Dog",
                totalQuantity = 10,
                timePeriodId = 1
            )
        )
        val historicalWaste = emptyList<ProductWasteByTimePeriod>()
        
        // Test with different days of the week
        val mondayDate = LocalDate.of(2025, 8, 25) // Monday
        val fridayDate = LocalDate.of(2025, 8, 29) // Friday
        val saturdayDate = LocalDate.of(2025, 8, 30) // Saturday
        
        // When
        val mondaySuggestion = suggestionEngine.generateSuggestions(
            mondayDate,
            timePeriodId,
            testProducts.filter { it.id == 1 },
            historicalSales,
            historicalWaste
        )[0]
        
        val fridaySuggestion = suggestionEngine.generateSuggestions(
            fridayDate,
            timePeriodId,
            testProducts.filter { it.id == 1 },
            historicalSales,
            historicalWaste
        )[0]
        
        val saturdaySuggestion = suggestionEngine.generateSuggestions(
            saturdayDate,
            timePeriodId,
            testProducts.filter { it.id == 1 },
            historicalSales,
            historicalWaste
        )[0]
        
        // Then
        // Monday: 10 * 0.9 = 9
        assertEquals(9, mondaySuggestion.suggestedQuantity)
        
        // Friday: 10 * 1.2 = 12
        assertEquals(12, fridaySuggestion.suggestedQuantity)
        
        // Saturday: 10 * 1.3 = 13
        assertEquals(13, saturdaySuggestion.suggestedQuantity)
    }

    @Test
    fun `generateSuggestions respects minimum and maximum quantities`() {
        // Given
        val timePeriodId = 1
        
        // Test with very low sales
        val lowSales = listOf(
            ProductSalesByTimePeriod(
                productId = 1,
                name = "Hot Dog",
                category = "Hot Dog",
                totalQuantity = 1,
                timePeriodId = 1
            )
        )
        
        // Test with very high sales
        val highSales = listOf(
            ProductSalesByTimePeriod(
                productId = 1,
                name = "Hot Dog",
                category = "Hot Dog",
                totalQuantity = 30,
                timePeriodId = 1
            )
        )
        
        // When
        val lowSuggestion = suggestionEngine.generateSuggestions(
            testDate,
            timePeriodId,
            testProducts.filter { it.id == 1 },
            lowSales,
            emptyList()
        )[0]
        
        val highSuggestion = suggestionEngine.generateSuggestions(
            testDate,
            timePeriodId,
            testProducts.filter { it.id == 1 },
            highSales,
            emptyList()
        )[0]
        
        // Then
        // Low sales should be capped at minimum of 2
        assertEquals(2, lowSuggestion.suggestedQuantity)
        
        // High sales should be capped at maximum of 20
        assertEquals(20, highSuggestion.suggestedQuantity)
    }

    @Test
    fun `generateSuggestions calculates confidence score based on data points`() {
        // Given
        val timePeriodId = 1
        
        // Create sales data with different numbers of data points
        val createSalesData = { productId: Int, count: Int ->
            List(count) {
                ProductSalesByTimePeriod(
                    productId = productId,
                    name = "Product $productId",
                    category = "Category",
                    totalQuantity = 10,
                    timePeriodId = 1
                )
            }
        }
        
        // When
        val noDataSuggestion = suggestionEngine.generateSuggestions(
            testDate,
            timePeriodId,
            listOf(testProducts[0]),
            emptyList(),
            emptyList()
        )[0]
        
        val oneDataPointSuggestion = suggestionEngine.generateSuggestions(
            testDate,
            timePeriodId,
            listOf(testProducts[0]),
            createSalesData(1, 1),
            emptyList()
        )[0]
        
        val threeDataPointsSuggestion = suggestionEngine.generateSuggestions(
            testDate,
            timePeriodId,
            listOf(testProducts[0]),
            createSalesData(1, 3),
            emptyList()
        )[0]
        
        val tenDataPointsSuggestion = suggestionEngine.generateSuggestions(
            testDate,
            timePeriodId,
            listOf(testProducts[0]),
            createSalesData(1, 10),
            emptyList()
        )[0]
        
        val twentyDataPointsSuggestion = suggestionEngine.generateSuggestions(
            testDate,
            timePeriodId,
            listOf(testProducts[0]),
            createSalesData(1, 20),
            emptyList()
        )[0]
        
        // Then
        assertEquals(0.1f, noDataSuggestion.confidenceScore, 0.001f)
        assertEquals(0.3f, oneDataPointSuggestion.confidenceScore, 0.001f)
        assertEquals(0.7f, threeDataPointsSuggestion.confidenceScore, 0.001f)
        assertEquals(0.9f, tenDataPointsSuggestion.confidenceScore, 0.001f)
        assertEquals(0.95f, twentyDataPointsSuggestion.confidenceScore, 0.001f)
    }

    @Test
    fun `generateSuggestions with custom day weights`() {
        // Given
        val timePeriodId = 1
        val historicalSales = listOf(
            ProductSalesByTimePeriod(
                productId = 1,
                name = "Hot Dog",
                category = "Hot Dog",
                totalQuantity = 10,
                timePeriodId = 1
            )
        )
        
        val customDayWeights = mapOf(
            DayOfWeek.THURSDAY to 2.0f // Double the quantity on Thursday
        )
        
        // When
        val suggestion = suggestionEngine.generateSuggestions(
            testDate, // Thursday
            timePeriodId,
            testProducts.filter { it.id == 1 },
            historicalSales,
            emptyList(),
            customDayWeights
        )[0]
        
        // Then
        // 10 * 2.0 = 20
        assertEquals(20, suggestion.suggestedQuantity)
    }
}