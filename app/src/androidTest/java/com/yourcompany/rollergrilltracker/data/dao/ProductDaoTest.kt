package com.yourcompany.rollergrilltracker.data.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yourcompany.rollergrilltracker.data.database.TestDatabase
import com.yourcompany.rollergrilltracker.data.entities.Product
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class ProductDaoTest : TestDatabase() {

    @Test
    fun insertAndGetProduct() = runBlocking {
        // Given
        val product = Product(
            id = 0, // Room will auto-generate
            name = "Hot Dog",
            barcode = "123456789",
            category = "Beef",
            active = true,
            inStock = true,
            unitsPerCase = 24,
            minStockLevel = 5,
            maxStockLevel = 20,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // When
        val id = productDao.insert(product)
        val retrievedProduct = productDao.getProductById(id.toInt())

        // Then
        assertNotNull(retrievedProduct)
        assertEquals("Hot Dog", retrievedProduct?.name)
        assertEquals("123456789", retrievedProduct?.barcode)
        assertEquals("Beef", retrievedProduct?.category)
    }

    @Test
    fun getAllProducts() = runBlocking {
        // Given
        val products = listOf(
            Product(
                id = 0,
                name = "Hot Dog",
                barcode = "123456789",
                category = "Beef",
                active = true,
                inStock = true
            ),
            Product(
                id = 0,
                name = "Chicken Roller",
                barcode = "987654321",
                category = "Poultry",
                active = true,
                inStock = false
            ),
            Product(
                id = 0,
                name = "Taquito",
                barcode = "456789123",
                category = "Mexican",
                active = false,
                inStock = true
            )
        )

        // When
        products.forEach { productDao.insert(it) }
        val allProducts = productDao.getAllProducts().first()

        // Then
        assertEquals(3, allProducts.size)
    }

    @Test
    fun getActiveProducts() = runBlocking {
        // Given
        val products = listOf(
            Product(
                id = 0,
                name = "Hot Dog",
                barcode = "123456789",
                category = "Beef",
                active = true,
                inStock = true
            ),
            Product(
                id = 0,
                name = "Chicken Roller",
                barcode = "987654321",
                category = "Poultry",
                active = true,
                inStock = false
            ),
            Product(
                id = 0,
                name = "Taquito",
                barcode = "456789123",
                category = "Mexican",
                active = false,
                inStock = true
            )
        )

        // When
        products.forEach { productDao.insert(it) }
        val activeProducts = productDao.getActiveProducts().first()

        // Then
        assertEquals(2, activeProducts.size)
        assertTrue(activeProducts.all { it.active })
    }

    @Test
    fun getActiveAndInStockProducts() = runBlocking {
        // Given
        val products = listOf(
            Product(
                id = 0,
                name = "Hot Dog",
                barcode = "123456789",
                category = "Beef",
                active = true,
                inStock = true
            ),
            Product(
                id = 0,
                name = "Chicken Roller",
                barcode = "987654321",
                category = "Poultry",
                active = true,
                inStock = false
            ),
            Product(
                id = 0,
                name = "Taquito",
                barcode = "456789123",
                category = "Mexican",
                active = false,
                inStock = true
            )
        )

        // When
        products.forEach { productDao.insert(it) }
        val activeAndInStockProducts = productDao.getActiveAndInStockProducts().first()

        // Then
        assertEquals(1, activeAndInStockProducts.size)
        assertTrue(activeAndInStockProducts.all { it.active && it.inStock })
    }

    @Test
    fun getProductByBarcode() = runBlocking {
        // Given
        val product = Product(
            id = 0,
            name = "Hot Dog",
            barcode = "123456789",
            category = "Beef",
            active = true,
            inStock = true
        )

        // When
        productDao.insert(product)
        val retrievedProduct = productDao.getProductByBarcode("123456789")

        // Then
        assertNotNull(retrievedProduct)
        assertEquals("Hot Dog", retrievedProduct?.name)
    }

    @Test
    fun getAllCategories() = runBlocking {
        // Given
        val products = listOf(
            Product(
                id = 0,
                name = "Hot Dog",
                barcode = "123456789",
                category = "Beef",
                active = true,
                inStock = true
            ),
            Product(
                id = 0,
                name = "Chicken Roller",
                barcode = "987654321",
                category = "Poultry",
                active = true,
                inStock = false
            ),
            Product(
                id = 0,
                name = "Taquito",
                barcode = "456789123",
                category = "Mexican",
                active = false,
                inStock = true
            ),
            Product(
                id = 0,
                name = "Beef Hot Dog",
                barcode = "111222333",
                category = "Beef",
                active = true,
                inStock = true
            )
        )

        // When
        products.forEach { productDao.insert(it) }
        val categories = productDao.getAllCategories().first()

        // Then
        assertEquals(3, categories.size)
        assertTrue(categories.contains("Beef"))
        assertTrue(categories.contains("Poultry"))
        assertTrue(categories.contains("Mexican"))
    }

    @Test
    fun updateProduct() = runBlocking {
        // Given
        val product = Product(
            id = 0,
            name = "Hot Dog",
            barcode = "123456789",
            category = "Beef",
            active = true,
            inStock = true
        )

        // When
        val id = productDao.insert(product).toInt()
        val retrievedProduct = productDao.getProductById(id)
        val updatedProduct = retrievedProduct?.copy(
            name = "Premium Hot Dog",
            category = "Premium Beef"
        )
        if (updatedProduct != null) {
            productDao.update(updatedProduct)
        }
        val finalProduct = productDao.getProductById(id)

        // Then
        assertNotNull(finalProduct)
        assertEquals("Premium Hot Dog", finalProduct?.name)
        assertEquals("Premium Beef", finalProduct?.category)
    }

    @Test
    fun deleteProduct() = runBlocking {
        // Given
        val product = Product(
            id = 0,
            name = "Hot Dog",
            barcode = "123456789",
            category = "Beef",
            active = true,
            inStock = true
        )

        // When
        val id = productDao.insert(product).toInt()
        val retrievedProduct = productDao.getProductById(id)
        if (retrievedProduct != null) {
            productDao.delete(retrievedProduct)
        }
        val finalProduct = productDao.getProductById(id)

        // Then
        assertNull(finalProduct)
    }

    @Test
    fun updateActiveStatus() = runBlocking {
        // Given
        val product = Product(
            id = 0,
            name = "Hot Dog",
            barcode = "123456789",
            category = "Beef",
            active = true,
            inStock = true
        )

        // When
        val id = productDao.insert(product).toInt()
        productDao.updateActiveStatus(id, false, LocalDateTime.now().toString())
        val updatedProduct = productDao.getProductById(id)

        // Then
        assertNotNull(updatedProduct)
        assertEquals(false, updatedProduct?.active)
    }

    @Test
    fun updateStockStatus() = runBlocking {
        // Given
        val product = Product(
            id = 0,
            name = "Hot Dog",
            barcode = "123456789",
            category = "Beef",
            active = true,
            inStock = true
        )

        // When
        val id = productDao.insert(product).toInt()
        productDao.updateStockStatus(id, false, LocalDateTime.now().toString())
        val updatedProduct = productDao.getProductById(id)

        // Then
        assertNotNull(updatedProduct)
        assertEquals(false, updatedProduct?.inStock)
    }

    @Test
    fun updateProductOrderSettings() = runBlocking {
        // Given
        val product = Product(
            id = 0,
            name = "Hot Dog",
            barcode = "123456789",
            category = "Beef",
            active = true,
            inStock = true,
            unitsPerCase = 24,
            minStockLevel = 5,
            maxStockLevel = 20
        )

        // When
        val id = productDao.insert(product).toInt()
        productDao.updateProductOrderSettings(
            id = id,
            unitsPerCase = 36,
            minStockLevel = 10,
            maxStockLevel = 30,
            timestamp = LocalDateTime.now().toString()
        )
        val updatedProduct = productDao.getProductById(id)

        // Then
        assertNotNull(updatedProduct)
        assertEquals(36, updatedProduct?.unitsPerCase)
        assertEquals(10, updatedProduct?.minStockLevel)
        assertEquals(30, updatedProduct?.maxStockLevel)
    }
}