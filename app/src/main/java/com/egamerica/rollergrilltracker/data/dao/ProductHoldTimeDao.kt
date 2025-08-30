package com.egamerica.rollergrilltracker.data.dao

import androidx.room.*
import com.egamerica.rollergrilltracker.data.entities.Product
import com.egamerica.rollergrilltracker.data.entities.ProductHoldTime
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ProductHoldTimeDao {
    @Query("SELECT * FROM product_hold_times WHERE isActive = 1 ORDER BY expirationTime")
    fun getActiveHoldTimes(): Flow<List<ProductHoldTime>>
    
    @Query("SELECT * FROM product_hold_times WHERE grillNumber = :grillNumber AND isActive = 1 ORDER BY expirationTime")
    fun getActiveHoldTimesByGrill(grillNumber: Int): Flow<List<ProductHoldTime>>
    
    @Query("SELECT * FROM product_hold_times WHERE slotAssignmentId = :slotAssignmentId AND isActive = 1")
    suspend fun getActiveHoldTimeForSlot(slotAssignmentId: Int): ProductHoldTime?
    
    @Query("SELECT * FROM product_hold_times WHERE grillNumber = :grillNumber AND slotNumber = :slotNumber AND isActive = 1")
    suspend fun getActiveHoldTimeForPosition(grillNumber: Int, slotNumber: Int): ProductHoldTime?
    
    @Query("SELECT * FROM product_hold_times WHERE expirationTime <= :currentTime AND isActive = 1")
    suspend fun getExpiredHoldTimes(currentTime: LocalDateTime): List<ProductHoldTime>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(productHoldTime: ProductHoldTime): Long
    
    @Update
    suspend fun update(productHoldTime: ProductHoldTime)
    
    @Query("UPDATE product_hold_times SET isActive = 0, wasDiscarded = 1, discardedAt = :discardedAt, discardReason = :discardReason WHERE id = :id")
    suspend fun markAsDiscarded(id: Int, discardedAt: LocalDateTime, discardReason: String)
    
    @Query("UPDATE product_hold_times SET isActive = 0 WHERE slotAssignmentId = :slotAssignmentId AND isActive = 1")
    suspend fun deactivateHoldTimesForSlot(slotAssignmentId: Int)
    
    @Transaction
    @Query("""
        SELECT pht.id, pht.productId, pht.slotAssignmentId, pht.grillNumber, pht.slotNumber, 
               pht.startTime, pht.expirationTime, pht.isActive, pht.wasDiscarded, pht.discardedAt, pht.discardReason,
               p.name as productName, p.category as productCategory
        FROM product_hold_times pht 
        JOIN products p ON pht.productId = p.id 
        WHERE pht.isActive = 1 
        ORDER BY pht.expirationTime
    """)
    fun getActiveHoldTimesWithProducts(): Flow<List<ProductWithHoldTime>>
    
    @Query("SELECT COUNT(*) FROM product_hold_times WHERE expirationTime <= :currentTime AND isActive = 1")
    suspend fun countExpiredHoldTimes(currentTime: LocalDateTime): Int
    
    @Query("SELECT * FROM product_hold_times WHERE wasDiscarded = 1 AND discardedAt BETWEEN :startDate AND :endDate")
    suspend fun getDiscardedProductsInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<ProductHoldTime>
}

data class ProductWithHoldTime(
    val id: Int,
    val productId: Int,
    val slotAssignmentId: Int,
    val grillNumber: Int,
    val slotNumber: Int,
    val startTime: LocalDateTime,
    val expirationTime: LocalDateTime,
    val isActive: Boolean,
    val wasDiscarded: Boolean,
    val discardedAt: LocalDateTime?,
    val discardReason: String?,
    val productName: String,
    val productCategory: String
)