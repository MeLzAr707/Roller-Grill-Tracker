package com.yourcompany.rollergrilltracker.data.repositories

import android.util.Log
import com.yourcompany.rollergrilltracker.data.dao.GrillConfigDao
import com.yourcompany.rollergrilltracker.data.dao.SlotDao
import com.yourcompany.rollergrilltracker.data.entities.GrillConfig
import com.yourcompany.rollergrilltracker.data.entities.SlotAssignment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GrillConfigRepository @Inject constructor(
    private val grillConfigDao: GrillConfigDao,
    private val slotDao: SlotDao
) {
    private val TAG = "GrillConfigRepository"
    
    fun getAllGrillConfigs(): Flow<List<GrillConfig>> {
        return grillConfigDao.getAllGrillConfigs()
            .catch { e ->
                Log.e(TAG, "Error getting all grill configs: ${e.message}", e)
                throw RepositoryException("Failed to get grill configs", e)
            }
    }
    
    fun getActiveGrillConfigs(): Flow<List<GrillConfig>> {
        return grillConfigDao.getActiveGrillConfigs()
            .catch { e ->
                Log.e(TAG, "Error getting active grill configs: ${e.message}", e)
                throw RepositoryException("Failed to get active grill configs", e)
            }
    }
    
    suspend fun getGrillConfigByNumber(grillNumber: Int): GrillConfig? {
        return try {
            grillConfigDao.getGrillConfigByNumber(grillNumber)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting grill config by number: ${e.message}", e)
            throw RepositoryException("Failed to get grill config by number", e)
        }
    }
    
    suspend fun countActiveGrills(): Int {
        return try {
            grillConfigDao.countActiveGrills()
        } catch (e: Exception) {
            Log.e(TAG, "Error counting active grills: ${e.message}", e)
            throw RepositoryException("Failed to count active grills", e)
        }
    }
    
    // Added method to match ViewModel call
    suspend fun addGrill(grillName: String, numberOfSlots: Int): Long {
        return try {
            // Find the next available grill number
            val existingGrills = getAllGrillConfigs().first()
            val nextGrillNumber = if (existingGrills.isEmpty()) {
                1
            } else {
                existingGrills.maxOf { it.grillNumber } + 1
            }
            
            val grillConfig = GrillConfig(
                grillNumber = nextGrillNumber,
                grillName = grillName,
                numberOfSlots = numberOfSlots,
                isActive = true
            )
            
            addGrillConfig(grillConfig)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding grill: ${e.message}", e)
            throw RepositoryException("Failed to add grill", e)
        }
    }
    
    suspend fun addGrillConfig(grillConfig: GrillConfig): Long {
        return try {
            val grillId = grillConfigDao.insert(grillConfig)
            
            // Create default slots for this grill
            val slots = (1..grillConfig.numberOfSlots).map { slotNumber ->
                SlotAssignment(
                    grillNumber = grillConfig.grillNumber,
                    slotNumber = slotNumber,
                    productId = null,
                    maxCapacity = 8 // Default capacity
                )
            }
            slotDao.insertAll(slots)
            
            grillId
        } catch (e: Exception) {
            Log.e(TAG, "Error adding grill config: ${e.message}", e)
            throw RepositoryException("Failed to add grill config", e)
        }
    }
    
    // Added method to match ViewModel call
    suspend fun updateGrill(grillConfig: GrillConfig) {
        try {
            grillConfigDao.update(grillConfig)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating grill: ${e.message}", e)
            throw RepositoryException("Failed to update grill", e)
        }
    }
    
    suspend fun updateGrillConfig(grillConfig: GrillConfig) {
        try {
            grillConfigDao.update(grillConfig)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating grill config: ${e.message}", e)
            throw RepositoryException("Failed to update grill config", e)
        }
    }
    
    // Added method to match ViewModel call
    suspend fun deleteGrill(grillNumber: Int) {
        try {
            val grillConfig = getGrillConfigByNumber(grillNumber)
            if (grillConfig != null) {
                deleteGrillConfig(grillConfig)
            } else {
                throw RepositoryException("Grill config not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting grill: ${e.message}", e)
            throw RepositoryException("Failed to delete grill", e)
        }
    }
    
    suspend fun deleteGrillConfig(grillConfig: GrillConfig) {
        try {
            // Delete all slots for this grill first
            slotDao.deleteSlotsByGrill(grillConfig.grillNumber)
            
            // Then delete the grill config
            grillConfigDao.delete(grillConfig)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting grill config: ${e.message}", e)
            throw RepositoryException("Failed to delete grill config", e)
        }
    }
    
    suspend fun updateGrillActiveStatus(grillNumber: Int, isActive: Boolean) {
        try {
            grillConfigDao.updateActiveStatus(grillNumber, isActive)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating grill active status: ${e.message}", e)
            throw RepositoryException("Failed to update grill active status", e)
        }
    }
    
    // Added method to match ViewModel call
    suspend fun updateGrillSlotCount(grillNumber: Int, numberOfSlots: Int) {
        try {
            updateNumberOfSlots(grillNumber, numberOfSlots)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating grill slot count: ${e.message}", e)
            throw RepositoryException("Failed to update grill slot count", e)
        }
    }
    
    suspend fun updateNumberOfSlots(grillNumber: Int, numberOfSlots: Int) {
        try {
            // Get current grill config
            val grillConfig = grillConfigDao.getGrillConfigByNumber(grillNumber)
                ?: throw RepositoryException("Grill config not found")
            
            // Update grill config
            grillConfigDao.updateNumberOfSlots(grillNumber, numberOfSlots)
            
            // Get current slots
            val currentSlots = slotDao.getSlotsByGrill(grillNumber).first()
            val currentSlotCount = currentSlots.size
            
            if (numberOfSlots > currentSlotCount) {
                // Add new slots
                val newSlots = (currentSlotCount + 1..numberOfSlots).map { slotNumber ->
                    SlotAssignment(
                        grillNumber = grillNumber,
                        slotNumber = slotNumber,
                        productId = null,
                        maxCapacity = 8 // Default capacity
                    )
                }
                slotDao.insertAll(newSlots)
            } else if (numberOfSlots < currentSlotCount) {
                // Remove excess slots
                for (slot in currentSlots) {
                    if (slot.slotNumber > numberOfSlots) {
                        slotDao.delete(slot)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating number of slots: ${e.message}", e)
            throw RepositoryException("Failed to update number of slots", e)
        }
    }
    
    // Added method to match ViewModel call
    suspend fun initializeDefaultGrill() {
        try {
            initializeDefaultGrillConfig()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing default grill: ${e.message}", e)
            throw RepositoryException("Failed to initialize default grill", e)
        }
    }
    
    suspend fun initializeDefaultGrillConfig() {
        try {
            // Check if any grill configs exist
            val count = grillConfigDao.countActiveGrills()
            
            if (count == 0) {
                // Create a default grill config
                val defaultGrillConfig = GrillConfig(
                    grillNumber = 1,
                    grillName = "Main Grill",
                    numberOfSlots = 4,
                    isActive = true
                )
                
                val grillId = grillConfigDao.insert(defaultGrillConfig)
                
                // Create default slots
                val slots = (1..4).map { slotNumber ->
                    SlotAssignment(
                        grillNumber = 1,
                        slotNumber = slotNumber,
                        productId = null,
                        maxCapacity = 8 // Default capacity
                    )
                }
                slotDao.insertAll(slots)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing default grill config: ${e.message}", e)
            throw RepositoryException("Failed to initialize default grill config", e)
        }
    }
}