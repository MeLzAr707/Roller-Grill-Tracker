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
    @Query("SELECT p.* FROM products p INNER JOIN product_hold_times pht ON p.id = pht.productId WHERE pht.isActive = 1 ORDER BY pht.expirationTime")
    fun getActiveHoldTimesWithProducts(): Flow<List<ProductWithHoldTime>>
    
    @Query("SELECT COUNT(*) FROM product_hold_times WHERE expirationTime <= :currentTime AND isActive = 1")
    suspend fun countExpiredHoldTimes(currentTime: LocalDateTime): Int
    
    @Query("SELECT * FROM product_hold_times WHERE wasDiscarded = 1 AND discardedAt BETWEEN :startDate AND :endDate")
    suspend fun getDiscardedProductsInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<ProductHoldTime>
}

data class ProductWithHoldTime(
    @Embedded val product: Product,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val holdTime: ProductHoldTime
)