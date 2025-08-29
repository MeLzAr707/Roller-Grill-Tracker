package com.egamerica.rollergrilltracker.data.repositories

import com.egamerica.rollergrilltracker.data.dao.ProductSalesByTimePeriod
import com.egamerica.rollergrilltracker.data.dao.ProductSalesSummary
import com.egamerica.rollergrilltracker.data.dao.SalesDetailDao
import com.egamerica.rollergrilltracker.data.dao.SalesEntryDao
import com.egamerica.rollergrilltracker.data.entities.SalesDetail
import com.egamerica.rollergrilltracker.data.entities.SalesEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.time.LocalDate
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class SalesRepositoryTest {

    private lateinit var salesEntryDao: SalesEntryDao
    private lateinit var salesDetailDao: SalesDetailDao
    private lateinit var salesRepository: SalesRepository

    private val testDate = LocalDate.of(2025, 8, 28)
    private val testTimePeriodId = 1

    private val testSalesEntry = SalesEntry(
        id = 1,
        date = testDate,
        timePeriodId = testTimePeriodId,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    private val testSalesDetails = listOf(
        SalesDetail(
            id = 1,
            salesEntryId = 1,
            productId = 1,
            quantity = 5,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        ),
        SalesDetail(
            id = 2,
            salesEntryId = 1,
            productId = 2,
            quantity = 3,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    )

    private val testProductSalesSummary = listOf(
        ProductSalesSummary(
            productId = 1,
            name = "Hot Dog",
            category = "Beef",
            totalQuantity = 10
        ),
        ProductSalesSummary(
            productId = 2,
            name = "Chicken Roller",
            category = "Poultry",
            totalQuantity = 7
        )
    )

    private val testProductSalesByTimePeriod = listOf(
        ProductSalesByTimePeriod(
            productId = 1,
            name = "Hot Dog",
            category = "Beef",
            totalQuantity = 5,
            timePeriodId = 1
        ),
        ProductSalesByTimePeriod(
            productId = 1,
            name = "Hot Dog",
            category = "Beef",
            totalQuantity = 5,
            timePeriodId = 2
        )
    )

    @Before
    fun setup() {
        salesEntryDao = mock(SalesEntryDao::class.java)
        salesDetailDao = mock(SalesDetailDao::class.java)
        salesRepository = SalesRepository(salesEntryDao, salesDetailDao)
    }

    @Test
    fun `getAllSalesEntries returns all entries from dao`() = runTest {
        // Given
        val salesEntries = listOf(testSalesEntry)
        `when`(salesEntryDao.getAllSalesEntries()).thenReturn(flowOf(salesEntries))

        // When
        val result = salesRepository.getAllSalesEntries()

        // Then
        assertEquals(salesEntries, result.collect { it })
        verify(salesEntryDao).getAllSalesEntries()
    }

    @Test
    fun `getSalesEntriesByDate returns entries for specific date from dao`() = runTest {
        // Given
        val salesEntries = listOf(testSalesEntry)
        `when`(salesEntryDao.getSalesEntriesByDate(testDate)).thenReturn(flowOf(salesEntries))

        // When
        val result = salesRepository.getSalesEntriesByDate(testDate)

        // Then
        assertEquals(salesEntries, result.collect { it })
        verify(salesEntryDao).getSalesEntriesByDate(testDate)
    }

    @Test
    fun `getSalesEntriesByDateRange returns entries within date range from dao`() = runTest {
        // Given
        val startDate = testDate
        val endDate = testDate.plusDays(7)
        val salesEntries = listOf(testSalesEntry)
        `when`(salesEntryDao.getSalesEntriesByDateRange(startDate, endDate)).thenReturn(flowOf(salesEntries))

        // When
        val result = salesRepository.getSalesEntriesByDateRange(startDate, endDate)

        // Then
        assertEquals(salesEntries, result.collect { it })
        verify(salesEntryDao).getSalesEntriesByDateRange(startDate, endDate)
    }

    @Test
    fun `getSalesEntryByDateAndPeriod returns entry for specific date and period from dao`() = runTest {
        // Given
        `when`(salesEntryDao.getSalesEntryByDateAndPeriod(testDate, testTimePeriodId)).thenReturn(testSalesEntry)

        // When
        val result = salesRepository.getSalesEntryByDateAndPeriod(testDate, testTimePeriodId)

        // Then
        assertEquals(testSalesEntry, result)
        verify(salesEntryDao).getSalesEntryByDateAndPeriod(testDate, testTimePeriodId)
    }

    @Test
    fun `getSalesDetailsBySalesEntryId returns details for specific entry from dao`() = runTest {
        // Given
        val salesEntryId = 1
        `when`(salesDetailDao.getSalesDetailsBySalesEntryId(salesEntryId)).thenReturn(flowOf(testSalesDetails))

        // When
        val result = salesRepository.getSalesDetailsBySalesEntryId(salesEntryId)

        // Then
        assertEquals(testSalesDetails, result.collect { it })
        verify(salesDetailDao).getSalesDetailsBySalesEntryId(salesEntryId)
    }

    @Test
    fun `saveSalesEntry creates new entry when none exists`() = runTest {
        // Given
        val productQuantities = mapOf(1 to 5, 2 to 3)
        val newEntryId = 1L
        
        `when`(salesEntryDao.getSalesEntryByDateAndPeriod(testDate, testTimePeriodId)).thenReturn(null)
        `when`(salesEntryDao.insert(any())).thenReturn(newEntryId)

        // When
        salesRepository.saveSalesEntry(testDate, testTimePeriodId, productQuantities)

        // Then
        val entryCaptor = ArgumentCaptor.forClass(SalesEntry::class.java)
        verify(salesEntryDao).insert(capture(entryCaptor))
        
        val entry = entryCaptor.value
        assertEquals(testDate, entry.date)
        assertEquals(testTimePeriodId, entry.timePeriodId)
        
        val detailsCaptor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<SalesDetail>>
        verify(salesDetailDao).insertAll(capture(detailsCaptor))
        
        val details = detailsCaptor.value
        assertEquals(2, details.size)
        assertEquals(newEntryId.toInt(), details[0].salesEntryId)
        assertEquals(newEntryId.toInt(), details[1].salesEntryId)
    }

    @Test
    fun `saveSalesEntry updates existing entry when one exists`() = runTest {
        // Given
        val productQuantities = mapOf(1 to 5, 2 to 3)
        
        `when`(salesEntryDao.getSalesEntryByDateAndPeriod(testDate, testTimePeriodId)).thenReturn(testSalesEntry)

        // When
        salesRepository.saveSalesEntry(testDate, testTimePeriodId, productQuantities)

        // Then
        verify(salesEntryDao, never()).insert(any())
        verify(salesDetailDao).deleteBySalesEntryId(testSalesEntry.id)
        
        val detailsCaptor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<SalesDetail>>
        verify(salesDetailDao).insertAll(capture(detailsCaptor))
        
        val details = detailsCaptor.value
        assertEquals(2, details.size)
        assertEquals(testSalesEntry.id, details[0].salesEntryId)
        assertEquals(testSalesEntry.id, details[1].salesEntryId)
    }

    @Test
    fun `getProductSalesSummary returns summary from dao`() = runTest {
        // Given
        val startDate = "2025-08-21"
        val endDate = "2025-08-28"
        `when`(salesDetailDao.getProductSalesSummary(startDate, endDate)).thenReturn(testProductSalesSummary)

        // When
        val result = salesRepository.getProductSalesSummary(startDate, endDate)

        // Then
        assertEquals(testProductSalesSummary, result)
        verify(salesDetailDao).getProductSalesSummary(startDate, endDate)
    }

    @Test
    fun `getProductSalesByTimePeriod returns sales by time period from dao`() = runTest {
        // Given
        val startDate = "2025-08-21"
        val endDate = "2025-08-28"
        `when`(salesDetailDao.getProductSalesByTimePeriod(startDate, endDate)).thenReturn(testProductSalesByTimePeriod)

        // When
        val result = salesRepository.getProductSalesByTimePeriod(startDate, endDate)

        // Then
        assertEquals(testProductSalesByTimePeriod, result)
        verify(salesDetailDao).getProductSalesByTimePeriod(startDate, endDate)
    }

    @Test(expected = RepositoryException::class)
    fun `getAllSalesEntries throws RepositoryException when dao throws exception`() = runTest {
        // Given
        `when`(salesEntryDao.getAllSalesEntries()).thenReturn(flow { throw RuntimeException("Database error") })

        // When
        salesRepository.getAllSalesEntries().collect { /* This should throw */ }

        // Then: expect RepositoryException
    }

    @Test(expected = RepositoryException::class)
    fun `getSalesEntryByDateAndPeriod throws RepositoryException when dao throws exception`() = runTest {
        // Given
        `when`(salesEntryDao.getSalesEntryByDateAndPeriod(any(), any())).thenThrow(RuntimeException("Database error"))

        // When
        salesRepository.getSalesEntryByDateAndPeriod(testDate, testTimePeriodId)

        // Then: expect RepositoryException
    }
}