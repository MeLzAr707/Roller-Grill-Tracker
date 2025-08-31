package com.egamerica.rollergrilltracker.data.repositories

import android.util.Log
import com.egamerica.rollergrilltracker.data.dao.ProductDao
import com.egamerica.rollergrilltracker.data.entities.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {
    private val TAG = "ProductRepository"
    
    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
            .catch { e ->
                Log.e(TAG, "Error getting all products: ${e.message}", e)
                throw RepositoryException("Failed to get products", e)
            }
    }
    
    fun getActiveProducts(): Flow<List<Product>> {
        return productDao.getActiveProducts()
            .catch { e ->
                Log.e(TAG, "Error getting active products: ${e.message}", e)
                throw RepositoryException("Failed to get active products", e)
            }
    }
    
    fun getActiveAndInStockProducts(): Flow<List<Product>> {
        return productDao.getActiveAndInStockProducts()
            .catch { e ->
                Log.e(TAG, "Error getting active and in-stock products: ${e.message}", e)
                throw RepositoryException("Failed to get active and in-stock products", e)
            }
    }
    
    suspend fun getProductById(id: Int): Product? {
        return try {
            productDao.getProductById(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product by ID: ${e.message}", e)
            throw RepositoryException("Failed to get product by ID", e)
        }
    }
    
    suspend fun getProductByBarcode(barcode: String): Product? {
        return try {
            productDao.getProductByBarcode(barcode)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product by barcode: ${e.message}", e)
            throw RepositoryException("Failed to get product by barcode", e)
        }
    }
    
    fun getAllCategories(): Flow<List<String>> {
        return productDao.getAllCategories()
            .catch { e ->
                Log.e(TAG, "Error getting all categories: ${e.message}", e)
                throw RepositoryException("Failed to get categories", e)
            }
    }
    
    fun getProductsByIds(ids: List<Int>): Flow<List<Product>> {
        return productDao.getProductsByIds(ids)
            .catch { e ->
                Log.e(TAG, "Error getting products by IDs: ${e.message}", e)
                throw RepositoryException("Failed to get products by IDs", e)
            }
    }
    
    suspend fun insertProduct(product: Product): Long {
        return try {
            productDao.insert(product)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting product: ${e.message}", e)
            throw RepositoryException("Failed to insert product", e)
        }
    }
    
    suspend fun updateProduct(product: Product) {
        try {
            productDao.update(product)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating product: ${e.message}", e)
            throw RepositoryException("Failed to update product", e)
        }
    }
    
    suspend fun deleteProduct(product: Product) {
        try {
            productDao.delete(product)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product: ${e.message}", e)
            throw RepositoryException("Failed to delete product", e)
        }
    }
    
    suspend fun updateActiveStatus(id: Int, active: Boolean) {
        try {
            productDao.updateActiveStatus(id, active, LocalDateTime.now().toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error updating active status: ${e.message}", e)
            throw RepositoryException("Failed to update active status", e)
        }
    }
    
    suspend fun updateStockStatus(id: Int, inStock: Boolean) {
        try {
            productDao.updateStockStatus(id, inStock, LocalDateTime.now().toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error updating stock status: ${e.message}", e)
            throw RepositoryException("Failed to update stock status", e)
        }
    }
    
    suspend fun updateProductOrderSettings(
        id: Int, 
        unitsPerCase: Int, 
        minStockLevel: Int, 
        maxStockLevel: Int
    ) {
        try {
            productDao.updateProductOrderSettings(
                id = id,
                unitsPerCase = unitsPerCase,
                minStockLevel = minStockLevel,
                maxStockLevel = maxStockLevel,
                timestamp = LocalDateTime.now().toString()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating product order settings: ${e.message}", e)
            throw RepositoryException("Failed to update product order settings", e)
        }
    }
}

class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)