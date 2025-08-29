package com.egamerica.rollergrilltracker.data.repositories

import android.util.Log
import androidx.room.Transaction
import com.egamerica.rollergrilltracker.data.dao.ProductSalesByTimePeriod
import com.egamerica.rollergrilltracker.data.dao.ProductSalesSummary
import com.egamerica.rollergrilltracker.data.dao.SalesDetailDao
import com.egamerica.rollergrilltracker.data.dao.SalesEntryDao
import com.egamerica.rollergrilltracker.data.entities.SalesDetail
import com.egamerica.rollergrilltracker.data.entities.SalesEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesRepository @Inject constructor(
    private val salesEntryDao: SalesEntryDao,
    private val salesDetailDao: SalesDetailDao
) {
    private val TAG = "SalesRepository"
    
    fun getAllSalesEntries(): Flow<List<SalesEntry>> {
        return salesEntryDao.getAllSalesEntries()
            .catch { e ->
                Log.e(TAG, "Error getting all sales entries: ${e.message}", e)
                throw RepositoryException("Failed to get sales entries", e)
            }
    }
    
    fun getSalesEntriesByDate(date: LocalDate): Flow<List<SalesEntry>> {
        return salesEntryDao.getSalesEntriesByDate(date)
            .catch { e ->
                Log.e(TAG, "Error getting sales entries by date: ${e.message}", e)
                throw RepositoryException("Failed to get sales entries by date", e)
            }
    }
    
    fun getSalesEntriesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<SalesEntry>> {
        return salesEntryDao.getSalesEntriesByDateRange(startDate, endDate)
            .catch { e ->
                Log.e(TAG, "Error getting sales entries by date range: ${e.message}", e)
                throw RepositoryException("Failed to get sales entries by date range", e)
            }
    }
    
    suspend fun getSalesEntryByDateAndPeriod(date: LocalDate, timePeriodId: Int): SalesEntry? {
        return try {
            salesEntryDao.getSalesEntryByDateAndPeriod(date, timePeriodId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting sales entry by date and period: ${e.message}", e)
            throw RepositoryException("Failed to get sales entry by date and period", e)
        }
    }
    
    fun getSalesDetailsBySalesEntryId(salesEntryId: Int): Flow<List<SalesDetail>> {
        return salesDetailDao.getSalesDetailsBySalesEntryId(salesEntryId)
            .catch { e ->
                Log.e(TAG, "Error getting sales details by sales entry ID: ${e.message}", e)
                throw RepositoryException("Failed to get sales details by sales entry ID", e)
            }
    }
    
    @Transaction
    suspend fun saveSalesEntry(
        date: LocalDate,
        timePeriodId: Int,
        productQuantities: Map<Int, Int>
    ) {
        try {
            // First, get or create the sales entry
            val existingEntry = salesEntryDao.getSalesEntryByDateAndPeriod(date, timePeriodId)
            val entryId = if (existingEntry != null) {
                existingEntry.id
            } else {
                val newEntry = SalesEntry(
                    date = date,
                    timePeriodId = timePeriodId
                )
                salesEntryDao.insert(newEntry).toInt()
            }
            
            // Now save the details
            val salesDetails = productQuantities.map { (productId, quantity) ->
                SalesDetail(
                    salesEntryId = entryId,
                    productId = productId,
                    quantity = quantity
                )
            }
            
            // Delete existing details if any
            salesDetailDao.deleteBySalesEntryId(entryId)
            
            // Insert new details
            salesDetailDao.insertAll(salesDetails)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving sales entry: ${e.message}", e)
            throw RepositoryException("Failed to save sales entry", e)
        }
    }
    
    suspend fun getProductSalesSummary(startDate: String, endDate: String): List<ProductSalesSummary> {
        return try {
            salesDetailDao.getProductSalesSummary(startDate, endDate)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product sales summary: ${e.message}", e)
            throw RepositoryException("Failed to get product sales summary", e)
        }
    }
    
    suspend fun getProductSalesByTimePeriod(startDate: String, endDate: String): List<ProductSalesByTimePeriod> {
        return try {
            salesDetailDao.getProductSalesByTimePeriod(startDate, endDate)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product sales by time period: ${e.message}", e)
            throw RepositoryException("Failed to get product sales by time period", e)
        }
    }
}