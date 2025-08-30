package com.egamerica.rollergrilltracker.data.dao

import androidx.room.*
import com.egamerica.rollergrilltracker.data.entities.Product
import com.egamerica.rollergrilltracker.data.entities.SlotAssignment
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface SlotDao {
    @Query("SELECT * FROM slot_assignments ORDER BY grillNumber, slotNumber")
    suspend fun getAllSlots(): List<SlotAssignment>
    
    @Query("SELECT * FROM slot_assignments ORDER BY grillNumber, slotNumber")
    fun getAllSlotAssignments(): Flow<List<SlotAssignment>>
    
    @Query("SELECT * FROM slot_assignments WHERE grillNumber = :grillNumber ORDER BY slotNumber")
    fun getSlotsByGrill(grillNumber: Int): Flow<List<SlotAssignment>>
    
    @Transaction
    @Query("SELECT * FROM slot_assignments ORDER BY grillNumber, slotNumber")
    fun getAllSlotsWithProducts(): Flow<List<SlotWithProduct>>
    
    @Transaction
    @Query("SELECT * FROM slot_assignments WHERE grillNumber = :grillNumber ORDER BY slotNumber")
    fun getSlotsWithProductsByGrill(grillNumber: Int): Flow<List<SlotWithProduct>>
    
    @Query("UPDATE slot_assignments SET productId = :productId, updatedAt = :updatedAt WHERE id = :slotId")
    suspend fun updateSlotAssignment(slotId: Int, productId: Int?, updatedAt: LocalDateTime)
    
    @Query("UPDATE slot_assignments SET productId = :productId, updatedAt = :updatedAt WHERE grillNumber = :grillNumber AND slotNumber = :slotNumber")
    suspend fun updateSlotAssignment(grillNumber: Int, slotNumber: Int, productId: Int?, updatedAt: LocalDateTime)
    
    @Query("UPDATE slot_assignments SET productId = :productId, updatedAt = :updatedAt, productAddedAt = :productAddedAt WHERE grillNumber = :grillNumber AND slotNumber = :slotNumber")
    suspend fun updateSlotAssignmentByPosition(grillNumber: Int, slotNumber: Int, productId: Int?, updatedAt: LocalDateTime, productAddedAt: LocalDateTime?)
    
    @Query("SELECT productId FROM slot_assignments WHERE productId IS NOT NULL")
    suspend fun getAssignedProductIds(): List<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(slot: SlotAssignment): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(slots: List<SlotAssignment>): List<Long>
    
    @Update
    suspend fun update(slot: SlotAssignment): Int
    
    @Delete
    suspend fun delete(slot: SlotAssignment): Int
    
    @Query("DELETE FROM slot_assignments WHERE grillNumber = :grillNumber")
    suspend fun deleteSlotsByGrill(grillNumber: Int)
    
    @Query("SELECT * FROM slot_assignments WHERE id = :slotId")
    suspend fun getSlotById(slotId: Int): SlotAssignment?
    
    @Query("SELECT * FROM slot_assignments WHERE grillNumber = :grillNumber AND slotNumber = :slotNumber")
    suspend fun getSlotByPosition(grillNumber: Int, slotNumber: Int): SlotAssignment?
    
    @Query("SELECT COUNT(*) FROM slot_assignments WHERE productAddedAt IS NOT NULL AND datetime(productAddedAt) < datetime('now', '-4 hours')")
    suspend fun countExpiredProducts(): Int
    
    @Query("SELECT * FROM slot_assignments WHERE productAddedAt IS NOT NULL AND datetime(productAddedAt) < datetime('now', '-4 hours')")
    suspend fun getExpiredProductSlots(): List<SlotAssignment>
}

data class SlotWithProduct(
    @Embedded val slot: SlotAssignment,
    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val product: Product?
)