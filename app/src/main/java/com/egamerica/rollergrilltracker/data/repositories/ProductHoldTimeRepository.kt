package com.egamerica.rollergrilltracker.data.repositories

import android.util.Log
import com.egamerica.rollergrilltracker.data.dao.ProductHoldTimeDao
import com.egamerica.rollergrilltracker.data.entities.ProductHoldTime
import com.egamerica.rollergrilltracker.data.models.ProductWithHoldTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductHoldTimeRepository @Inject constructor(
    private val productHoldTimeDao: ProductHoldTimeDao
) {
    private val TAG = "ProductHoldTimeRepository"
    
    fun getActiveHoldTimes(): Flow<List<ProductHoldTime>> {
        return productHoldTimeDao.getActiveHoldTimes()
            .catch { e ->
                Log.e(TAG, "Error getting active hold times: ${e.message}", e)
                throw RepositoryException("Failed to get active hold times", e)
            }
    }
    
    fun getActiveHoldTimesByGrill(grillNumber: Int): Flow<List<ProductHoldTime>> {
        return productHoldTimeDao.getActiveHoldTimesByGrill(grillNumber)
            .catch { e ->
                Log.e(TAG, "Error getting active hold times by grill: ${e.message}", e)
                throw RepositoryException("Failed to get active hold times by grill", e)
            }
    }
    
    fun getActiveHoldTimesWithProducts(): Flow<List<ProductWithHoldTime>> {
        return productHoldTimeDao.getActiveHoldTimesWithProducts()
            .catch { e ->
                Log.e(TAG, "Error getting active hold times with products: ${e.message}", e)
                throw RepositoryException("Failed to get active hold times with products", e)
            }
    }
    
    suspend fun getActiveHoldTimeForSlot(slotAssignmentId: Int): ProductHoldTime? {
        return try {
            productHoldTimeDao.getActiveHoldTimeForSlot(slotAssignmentId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active hold time for slot: ${e.message}", e)
            throw RepositoryException("Failed to get active hold time for slot", e)
        }
    }
    
    suspend fun getActiveHoldTimeForPosition(grillNumber: Int, slotNumber: Int): ProductHoldTime? {
        return try {
            productHoldTimeDao.getActiveHoldTimeForPosition(grillNumber, slotNumber)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active hold time for position: ${e.message}", e)
            throw RepositoryException("Failed to get active hold time for position", e)
        }
    }
    
    suspend fun getExpiredHoldTimes(): List<ProductHoldTime> {
        return try {
            productHoldTimeDao.getExpiredHoldTimes(LocalDateTime.now())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting expired hold times: ${e.message}", e)
            throw RepositoryException("Failed to get expired hold times", e)
        }
    }
    
    suspend fun startHoldTime(
        productId: Int,
        slotAssignmentId: Int,
        grillNumber: Int,
        slotNumber: Int
    ): Long {
        return try {
            // Deactivate any existing hold time for this slot
            productHoldTimeDao.deactivateHoldTimesForSlot(slotAssignmentId)
            
            // Create new hold time
            val startTime = LocalDateTime.now()
            val expirationTime = startTime.plusHours(4) // 4-hour hold time
            
            val holdTime = ProductHoldTime(
                productId = productId,
                slotAssignmentId = slotAssignmentId,
                grillNumber = grillNumber,
                slotNumber = slotNumber,
                startTime = startTime,
                expirationTime = expirationTime,
                isActive = true
            )
            
            productHoldTimeDao.insert(holdTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting hold time: ${e.message}", e)
            throw RepositoryException("Failed to start hold time", e)
        }
    }
    
    suspend fun markAsDiscarded(id: Int, discardReason: String) {
        try {
            productHoldTimeDao.markAsDiscarded(
                id = id,
                discardedAt = LocalDateTime.now(),
                discardReason = discardReason
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error marking hold time as discarded: ${e.message}", e)
            throw RepositoryException("Failed to mark hold time as discarded", e)
        }
    }
    
    suspend fun deactivateHoldTimesForSlot(slotAssignmentId: Int) {
        try {
            productHoldTimeDao.deactivateHoldTimesForSlot(slotAssignmentId)
        } catch (e: Exception) {
            Log.e(TAG, "Error deactivating hold times for slot: ${e.message}", e)
            throw RepositoryException("Failed to deactivate hold times for slot", e)
        }
    }
    
    suspend fun countExpiredHoldTimes(): Int {
        return try {
            productHoldTimeDao.countExpiredHoldTimes(LocalDateTime.now())
        } catch (e: Exception) {
            Log.e(TAG, "Error counting expired hold times: ${e.message}", e)
            throw RepositoryException("Failed to count expired hold times", e)
        }
    }
    
    suspend fun getDiscardedProductsInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<ProductHoldTime> {
        return try {
            productHoldTimeDao.getDiscardedProductsInDateRange(startDate, endDate)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting discarded products in date range: ${e.message}", e)
            throw RepositoryException("Failed to get discarded products in date range", e)
        }
    }
}