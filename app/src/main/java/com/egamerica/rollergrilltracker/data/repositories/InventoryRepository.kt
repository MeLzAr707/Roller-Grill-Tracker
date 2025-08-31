package com.egamerica.rollergrilltracker.data.repositories

import android.util.Log
import androidx.room.Transaction
import com.egamerica.rollergrilltracker.data.dao.InventoryDao
import com.egamerica.rollergrilltracker.data.dao.InventoryReportItem
import com.egamerica.rollergrilltracker.data.entities.InventoryCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val inventoryDao: InventoryDao
) {
    private val TAG = "InventoryRepository"
    
    suspend fun getInventoryCountsForDate(date: LocalDate): List<InventoryCount> {
        return try {
            inventoryDao.getInventoryCountsForDate(date)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting inventory counts for date: ${e.message}", e)
            throw RepositoryException("Failed to get inventory counts for date", e)
        }
    }
    
    @Transaction
    suspend fun saveInventoryCount(inventoryCount: InventoryCount) {
        try {
            val existing = inventoryDao.getInventoryCountForProductAndDate(
                inventoryCount.productId, 
                inventoryCount.date
            )
            
            if (existing != null) {
                inventoryDao.update(inventoryCount.copy(id = existing.id))
            } else {
                inventoryDao.insert(inventoryCount)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving inventory count: ${e.message}", e)
            throw RepositoryException("Failed to save inventory count", e)
        }
    }
    
    suspend fun getInventoryReportForDateRange(startDate: LocalDate, endDate: LocalDate): List<InventoryReportItem> {
        return try {
            inventoryDao.getInventoryReportForDateRange(startDate, endDate)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting inventory report for date range: ${e.message}", e)
            throw RepositoryException("Failed to get inventory report for date range", e)
        }
    }

    // Missing methods that are called from ViewModels
    fun getInventoryCountsByDate(dateString: String): Flow<List<InventoryCount>> {
        return flow {
            try {
                val date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                emit(inventoryDao.getInventoryCountsForDate(date))
            } catch (e: Exception) {
                Log.e(TAG, "Error getting inventory counts by date: ${e.message}", e)
                throw RepositoryException("Failed to get inventory counts by date", e)
            }
        }
    }

    suspend fun deleteInventoryCountsByDate(dateString: String) {
        try {
            val date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            inventoryDao.deleteByDate(date)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting inventory counts by date: ${e.message}", e)
            throw RepositoryException("Failed to delete inventory counts by date", e)
        }
    }

    suspend fun insertInventoryCounts(inventoryCounts: List<InventoryCount>) {
        try {
            inventoryCounts.forEach { count ->
                inventoryDao.insert(count)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting inventory counts: ${e.message}", e)
            throw RepositoryException("Failed to insert inventory counts", e)
        }
    }
}