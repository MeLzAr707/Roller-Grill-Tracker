package com.egamerica.rollergrilltracker.data.repositories

import android.util.Log
import androidx.room.Transaction
import com.egamerica.rollergrilltracker.data.dao.InventoryDao
import com.egamerica.rollergrilltracker.data.entities.InventoryCount
import com.egamerica.rollergrilltracker.data.models.InventoryReportItem
import java.time.LocalDate
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
}