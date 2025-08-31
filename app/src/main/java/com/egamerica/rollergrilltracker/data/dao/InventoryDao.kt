package com.egamerica.rollergrilltracker.data.dao

import androidx.room.*
import com.egamerica.rollergrilltracker.data.entities.InventoryCount
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_counts WHERE date = :date")
    suspend fun getInventoryCountsForDate(date: LocalDate): List<InventoryCount>
    
    @Query("SELECT * FROM inventory_counts WHERE productId = :productId AND date = :date")
    suspend fun getInventoryCountForProductAndDate(productId: Int, date: LocalDate): InventoryCount?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inventoryCount: InventoryCount): Long
    
    @Update
    suspend fun update(inventoryCount: InventoryCount)
    
    @Query("DELETE FROM inventory_counts WHERE date = :date")
    suspend fun deleteByDate(date: LocalDate)
    
    @Transaction
    @Query("""
        SELECT ic.productId, p.name as productName, 
        SUM(ic.startingCount + ic.deliveryCount - ic.endingCount) as totalUsed,
        AVG(ic.startingCount + ic.deliveryCount - ic.endingCount) as averageUsed
        FROM inventory_counts ic
        INNER JOIN products p ON ic.productId = p.id
        WHERE ic.date BETWEEN :startDate AND :endDate
        GROUP BY ic.productId
        ORDER BY totalUsed DESC
    """)
    suspend fun getInventoryReportForDateRange(startDate: LocalDate, endDate: LocalDate): List<InventoryReportItem>
}

data class InventoryReportItem(
    val productId: Int,
    val productName: String,
    val totalUsed: Int,
    val averageUsed: Float
)