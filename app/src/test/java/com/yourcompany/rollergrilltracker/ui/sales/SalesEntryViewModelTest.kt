package com.yourcompany.rollergrilltracker.ui.sales

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.yourcompany.rollergrilltracker.data.entities.Product
import com.yourcompany.rollergrilltracker.data.entities.SalesDetail
import com.yourcompany.rollergrilltracker.data.entities.SalesEntry
import com.yourcompany.rollergrilltracker.data.entities.TimePeriod
import com.yourcompany.rollergrilltracker.data.repositories.ProductRepository
import com.yourcompany.rollergrilltracker.data.repositories.SalesRepository
import com.yourcompany.rollergrilltracker.data.repositories.TimePeriodRepository
import com.yourcompany.rollergrilltracker.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@ExperimentalCoroutinesApi
class SalesEntryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var salesRepository: SalesRepository
    private lateinit var productRepository: ProductRepository
    private lateinit var timePeriodRepository: TimePeriodRepository
    private lateinit var viewModel: SalesEntryViewModel

    private val testProducts = listOf(
        Product(
            id = 1,
            name = "Hot Dog",
            barcode = "123456789",
            category = "Beef",
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
            category = "Poultry",
            active = true,
            inStock = true,
            unitsPerCase = 24,
            minStockLevel = 5,
            maxStockLevel = 20,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    )

    private val testTimePeriods = listOf(
        TimePeriod(
            id = 1,
            name = "Morning (6am-10am)",
            startTime = "06:00",
            endTime = "10:00",
            displayOrder = 1
        ),
        TimePeriod(
            id = 2,
            name = "Midday (10am-2pm)",
            startTime = "10:00",
            endTime = "14:00",
            displayOrder = 2
        ),
        TimePeriod(
            id = 3,
            name = "Afternoon (2pm-6pm)",
            startTime = "14:00",
            endTime = "18:00",
            displayOrder = 3
        ),
        TimePeriod(
            id = 4,
            name = "Evening (6pm-10pm)",
            startTime = "18:00",
            endTime = "22:00",
            displayOrder = 4
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        salesRepository = mock(SalesRepository::class.java)
        productRepository = mock(ProductRepository::class.java)
        timePeriodRepository = mock(TimePeriodRepository::class.java)
        
        `when`(productRepository.getActiveProducts()).thenReturn(flowOf(testProducts))
        `when`(timePeriodRepository.getAllTimePeriods()).thenReturn(flowOf(testTimePeriods))
        
        viewModel = SalesEntryViewModel(salesRepository, productRepository, timePeriodRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `init loads products and time periods`() = runTest {
        // Then
        val products = viewModel.products.getOrAwaitValue()
        val timePeriods = viewModel.timePeriods.getOrAwaitValue()
        
        assertEquals(testProducts, products)
        assertEquals(testTimePeriods, timePeriods)
        verify(productRepository).getActiveProducts()
        verify(timePeriodRepository).getAllTimePeriods()
    }

    @Test
    fun `init sets default date to today`() {
        // Then
        val selectedDate = viewModel.selectedDate.getOrAwaitValue()
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
        
        assertEquals(today, selectedDate)
    }

    @Test
    fun `setSelectedDate updates selected date`() {
        // Given
        val testDate = Calendar.getInstance().apply {
            set(2025, 7, 15) // August 15, 2025
        }.time
        
        // When
        viewModel.setSelectedDate(testDate)
        
        // Then
        val selectedDate = viewModel.selectedDate.getOrAwaitValue()
        assertEquals("2025-08-15", selectedDate)
    }

    @Test
    fun `setSelectedTimePeriod updates selected time period`() {
        // Given
        val timePeriod = testTimePeriods[2] // Afternoon
        
        // When
        viewModel.setSelectedTimePeriod(timePeriod)
        
        // Then
        val selectedTimePeriod = viewModel.selectedTimePeriod.getOrAwaitValue()
        assertEquals(timePeriod, selectedTimePeriod)
    }

    @Test
    fun `updateProductQuantity adds product quantity to map`() {
        // When
        viewModel.updateProductQuantity(1, 5)
        viewModel.updateProductQuantity(2, 3)
        
        // Then
        val quantities = viewModel.productQuantities.getOrAwaitValue()
        assertEquals(5, quantities[1])
        assertEquals(3, quantities[2])
    }

    @Test
    fun `updateProductQuantity updates existing product quantity`() {
        // Given
        viewModel.updateProductQuantity(1, 5)
        
        // When
        viewModel.updateProductQuantity(1, 8)
        
        // Then
        val quantities = viewModel.productQuantities.getOrAwaitValue()
        assertEquals(8, quantities[1])
    }

    @Test
    fun `copyFromPreviousPeriod copies quantities from previous period`() = runTest {
        // Given
        val currentDate = "2025-08-28"
        val previousPeriod = testTimePeriods[0] // Morning
        val currentPeriod = testTimePeriods[1] // Midday
        
        val previousSalesEntry = SalesEntry(
            id = 1,
            date = LocalDate.parse(currentDate),
            timePeriodId = previousPeriod.id
        )
        
        val previousSalesDetails = listOf(
            SalesDetail(
                id = 1,
                salesEntryId = 1,
                productId = 1,
                quantity = 5
            ),
            SalesDetail(
                id = 2,
                salesEntryId = 1,
                productId = 2,
                quantity = 3
            )
        )
        
        `when`(salesRepository.getSalesEntryByDateAndTimePeriod(currentDate, previousPeriod.id))
            .thenReturn(flowOf(previousSalesEntry))
        `when`(salesRepository.getSalesDetailsBySalesEntryId(previousSalesEntry.id))
            .thenReturn(flowOf(previousSalesDetails))
        
        // Set current period
        viewModel.setSelectedTimePeriod(currentPeriod)
        
        // When
        viewModel.copyFromPreviousPeriod()
        
        // Then
        val quantities = viewModel.productQuantities.getOrAwaitValue()
        assertEquals(5, quantities[1])
        assertEquals(3, quantities[2])
    }

    @Test
    fun `saveSalesEntry creates new entry when none exists`() = runTest {
        // Given
        val currentDate = "2025-08-28"
        val currentPeriod = testTimePeriods[1] // Midday
        
        viewModel.setSelectedTimePeriod(currentPeriod)
        viewModel.updateProductQuantity(1, 5)
        viewModel.updateProductQuantity(2, 3)
        
        `when`(salesRepository.getSalesEntryByDateAndTimePeriod(currentDate, currentPeriod.id))
            .thenReturn(flowOf(null))
        `when`(salesRepository.insertSalesEntry(any())).thenReturn(1L)
        
        // When
        viewModel.saveSalesEntry()
        
        // Then
        val saveStatus = viewModel.saveStatus.getOrAwaitValue()
        assertTrue(saveStatus is SalesEntryViewModel.SaveStatus.SUCCESS)
        
        verify(salesRepository).insertSalesEntry(any())
        verify(salesRepository).insertSalesDetails(any())
    }

    @Test
    fun `saveSalesEntry updates existing entry when one exists`() = runTest {
        // Given
        val currentDate = "2025-08-28"
        val currentPeriod = testTimePeriods[1] // Midday
        
        val existingSalesEntry = SalesEntry(
            id = 1,
            date = LocalDate.parse(currentDate),
            timePeriodId = currentPeriod.id
        )
        
        viewModel.setSelectedTimePeriod(currentPeriod)
        viewModel.updateProductQuantity(1, 5)
        viewModel.updateProductQuantity(2, 3)
        
        `when`(salesRepository.getSalesEntryByDateAndTimePeriod(currentDate, currentPeriod.id))
            .thenReturn(flowOf(existingSalesEntry))
        
        // When
        viewModel.saveSalesEntry()
        
        // Then
        val saveStatus = viewModel.saveStatus.getOrAwaitValue()
        assertTrue(saveStatus is SalesEntryViewModel.SaveStatus.SUCCESS)
        
        verify(salesRepository, never()).insertSalesEntry(any())
        verify(salesRepository).deleteSalesDetailsBySalesEntryId(existingSalesEntry.id)
        verify(salesRepository).insertSalesDetails(any())
    }
}