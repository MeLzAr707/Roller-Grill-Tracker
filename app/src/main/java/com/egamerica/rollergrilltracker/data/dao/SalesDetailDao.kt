package com.egamerica.rollergrilltracker.data.dao

import androidx.room.*
import com.egamerica.rollergrilltracker.data.entities.SalesDetail
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SalesDetailDao {
    @Query("SELECT * FROM sales_details WHERE salesEntryId = :salesEntryId")
    fun getSalesDetailsBySalesEntryId(salesEntryId: Int): Flow<List<SalesDetail>>
    
    @Query("SELECT * FROM sales_details WHERE productId = :productId")
    fun getSalesDetailsByProductId(productId: Int): Flow<List<SalesDetail>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(salesDetail: SalesDetail): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(salesDetails: List<SalesDetail>)
    
    @Update
    suspend fun update(salesDetail: SalesDetail)
    
    @Delete
    suspend fun delete(salesDetail: SalesDetail)
    
    @Query("DELETE FROM sales_details WHERE salesEntryId = :salesEntryId")
    suspend fun deleteBySalesEntryId(salesEntryId: Int)
    
    @Transaction
    @Query("""
        SELECT p.id as productId, p.name, p.category, SUM(sd.quantity) as totalQuantity
        FROM sales_details sd
        INNER JOIN products p ON sd.productId = p.id
        INNER JOIN sales_entries se ON sd.salesEntryId = se.id
        WHERE se.date BETWEEN :startDate AND :endDate
        GROUP BY p.id
        ORDER BY totalQuantity DESC
    """)
    suspend fun getProductSalesSummary(startDate: LocalDate, endDate: LocalDate): List<ProductSalesSummary>
    
    @Transaction
    @Query("""
        SELECT p.id as productId, p.name, p.category, SUM(sd.quantity) as totalQuantity, se.timePeriodId
        FROM sales_details sd
        INNER JOIN products p ON sd.productId = p.id
        INNER JOIN sales_entries se ON sd.salesEntryId = se.id
        WHERE se.date BETWEEN :startDate AND :endDate
        GROUP BY p.id, se.timePeriodId
        ORDER BY se.timePeriodId, totalQuantity DESC
    """)
    suspend fun getProductSalesByTimePeriod(startDate: LocalDate, endDate: LocalDate): List<ProductSalesByTimePeriod>
}

data class ProductSalesSummary(
    val productId: Int,
    val name: String,
    val category: String,
    val totalQuantity: Int
)

data class ProductSalesByTimePeriod(
    val productId: Int,
    val name: String,
    val category: String,
    val totalQuantity: Int,
    val timePeriodId: Int
)