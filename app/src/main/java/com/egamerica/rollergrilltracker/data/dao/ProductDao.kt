package com.egamerica.rollergrilltracker.data.dao

import androidx.room.*
import com.egamerica.rollergrilltracker.data.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY category, name")
    fun getAllProducts(): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE active = 1 ORDER BY category, name")
    fun getActiveProducts(): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE active = 1 AND inStock = 1 ORDER BY category, name")
    fun getActiveAndInStockProducts(): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?
    
    @Query("SELECT * FROM products WHERE id IN (:ids)")
    fun getProductsByIds(ids: List<Int>): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE barcode = :barcode")
    suspend fun getProductByBarcode(barcode: String): Product?
    
    @Query("SELECT DISTINCT category FROM products ORDER BY category")
    fun getAllCategories(): Flow<List<String>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)
    
    @Update
    suspend fun update(product: Product)
    
    @Delete
    suspend fun delete(product: Product)
    
    @Query("UPDATE products SET active = :active, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateActiveStatus(id: Int, active: Boolean, timestamp: String)
    
    @Query("UPDATE products SET inStock = :inStock, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateStockStatus(id: Int, inStock: Boolean, timestamp: String)
    
    @Query("UPDATE products SET unitsPerCase = :unitsPerCase, minStockLevel = :minStockLevel, maxStockLevel = :maxStockLevel, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateProductOrderSettings(
        id: Int, 
        unitsPerCase: Int, 
        minStockLevel: Int, 
        maxStockLevel: Int, 
        timestamp: String
    )
}