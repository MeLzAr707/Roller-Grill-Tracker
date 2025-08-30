package com.egamerica.rollergrilltracker.data.dao

import androidx.room.*
import com.egamerica.rollergrilltracker.data.entities.WasteDetail
import com.egamerica.rollergrilltracker.data.models.ProductWasteSummary
import com.egamerica.rollergrilltracker.data.models.ProductWasteByTimePeriod
import kotlinx.coroutines.flow.Flow

@Dao
interface WasteDetailDao {
    @Query("SELECT * FROM waste_details WHERE wasteEntryId = :wasteEntryId")
    fun getWasteDetailsByWasteEntryId(wasteEntryId: Int): Flow<List<WasteDetail>>
    
    @Query("SELECT * FROM waste_details WHERE productId = :productId")
    fun getWasteDetailsByProductId(productId: Int): Flow<List<WasteDetail>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wasteDetail: WasteDetail): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wasteDetails: List<WasteDetail>)
    
    @Update
    suspend fun update(wasteDetail: WasteDetail)
    
    @Delete
    suspend fun delete(wasteDetail: WasteDetail)
    
    @Query("DELETE FROM waste_details WHERE wasteEntryId = :wasteEntryId")
    suspend fun deleteByWasteEntryId(wasteEntryId: Int)
    
    @Transaction
    @Query("""
        SELECT p.id as productId, p.name, p.category, SUM(wd.quantity) as totalQuantity
        FROM waste_details wd
        INNER JOIN products p ON wd.productId = p.id
        INNER JOIN waste_entries we ON wd.wasteEntryId = we.id
        WHERE we.date BETWEEN :startDate AND :endDate
        GROUP BY p.id
        ORDER BY totalQuantity DESC
    """)
    suspend fun getProductWasteSummary(startDate: String, endDate: String): List<ProductWasteSummary>
    
    @Transaction
    @Query("""
        SELECT p.id as productId, p.name, p.category, SUM(wd.quantity) as totalQuantity, we.timePeriodId
        FROM waste_details wd
        INNER JOIN products p ON wd.productId = p.id
        INNER JOIN waste_entries we ON wd.wasteEntryId = we.id
        WHERE we.date BETWEEN :startDate AND :endDate
        GROUP BY p.id, we.timePeriodId
        ORDER BY we.timePeriodId, totalQuantity DESC
    """)
    suspend fun getProductWasteByTimePeriod(startDate: String, endDate: String): List<ProductWasteByTimePeriod>
}