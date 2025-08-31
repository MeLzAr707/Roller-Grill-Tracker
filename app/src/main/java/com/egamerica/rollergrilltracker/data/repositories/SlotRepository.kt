package com.egamerica.rollergrilltracker.data.repositories

import android.util.Log
import com.egamerica.rollergrilltracker.data.dao.SlotDao
import com.egamerica.rollergrilltracker.data.entities.SlotAssignment
import com.egamerica.rollergrilltracker.data.models.SlotWithProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SlotRepository @Inject constructor(
    private val slotDao: SlotDao
) {
    private val TAG = "SlotRepository"
    
    fun getAllSlotsWithProducts(): Flow<List<SlotWithProduct>> {
        return slotDao.getAllSlotsWithProducts()
            .catch { e ->
                Log.e(TAG, "Error getting all slots with products: ${e.message}", e)
                throw RepositoryException("Failed to get slots with products", e)
            }
    }
    
    suspend fun updateSlotAssignment(grillNumber: Int, slotNumber: Int, productId: Int?) {
        try {
            slotDao.updateSlotAssignment(grillNumber, slotNumber, productId, LocalDateTime.now())
        } catch (e: Exception) {
            Log.e(TAG, "Error updating slot assignment: ${e.message}", e)
            throw RepositoryException("Failed to update slot assignment", e)
        }
    }
    
    suspend fun getAssignedProductIds(): List<Int> {
        return try {
            slotDao.getAssignedProductIds()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting assigned product IDs: ${e.message}", e)
            throw RepositoryException("Failed to get assigned product IDs", e)
        }
    }
    
    suspend fun initializeDefaultSlots() {
        try {
            // Check if slots already exist
            val existingSlots = slotDao.getAllSlots()
            if (existingSlots.isEmpty()) {
                // Create default slots
                val slots = listOf(
                    SlotAssignment(grillNumber = 1, slotNumber = 1, productId = null, maxCapacity = 8),
                    SlotAssignment(grillNumber = 1, slotNumber = 2, productId = null, maxCapacity = 8),
                    SlotAssignment(grillNumber = 1, slotNumber = 3, productId = null, maxCapacity = 8),
                    SlotAssignment(grillNumber = 1, slotNumber = 4, productId = null, maxCapacity = 8),
                    SlotAssignment(grillNumber = 1, slotNumber = 5, productId = null, maxCapacity = 16) // Tamale cooker
                )
                slotDao.insertAll(slots)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing default slots: ${e.message}", e)
            throw RepositoryException("Failed to initialize default slots", e)
        }
    }
    
    // Added methods to match ViewModel calls
    suspend fun getAllSlotAssignments(): Flow<List<SlotAssignment>> {
        return slotDao.getAllSlotAssignments()
            .catch { e ->
                Log.e(TAG, "Error getting all slot assignments: ${e.message}", e)
                throw RepositoryException("Failed to get slot assignments", e)
            }
    }
    
    suspend fun insertSlotAssignment(slotAssignment: SlotAssignment): Long {
        return try {
            slotDao.insert(slotAssignment)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting slot assignment: ${e.message}", e)
            throw RepositoryException("Failed to insert slot assignment", e)
        }
    }
    
    suspend fun deleteSlotAssignment(slotAssignment: SlotAssignment) {
        try {
            slotDao.delete(slotAssignment)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting slot assignment: ${e.message}", e)
            throw RepositoryException("Failed to delete slot assignment", e)
        }
    }
}