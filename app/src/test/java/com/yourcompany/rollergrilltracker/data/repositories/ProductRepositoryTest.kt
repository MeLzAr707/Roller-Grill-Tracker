package com.egamerica.rollergrilltracker.data.repositories

import com.egamerica.rollergrilltracker.data.dao.ProductDao
import com.egamerica.rollergrilltracker.data.entities.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class ProductRepositoryTest {

    private lateinit var productDao: ProductDao
    private lateinit var productRepository: ProductRepository

    private val testProducts = listOf(
        Product(
            id = 1,
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
        ),
        Product(
            id = 2,
            name = "Chicken Roller",
            barcode = "987654321",
            category = "Poultry",
            active = true,
            inStock = false,
            unitsPerCase = 24,
            minStockLevel = 5,
            maxStockLevel = 20,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        ),
        Product(
            id = 3,
            name = "Taquito",
            barcode = "456789123",
            category = "Mexican",
            active = false,
            inStock = true,
            unitsPerCase = 36,
            minStockLevel = 10,
            maxStockLevel = 30,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    )

    @Before
    fun setup() {
        productDao = mock(ProductDao::class.java)
        productRepository = ProductRepository(productDao)
    }

    @Test
    fun `getAllProducts returns all products from dao`() = runTest {
        // Given
        `when`(productDao.getAllProducts()).thenReturn(flowOf(testProducts))

        // When
        val result = productRepository.getAllProducts()

        // Then
        assertEquals(testProducts, result.collect { it })
        verify(productDao).getAllProducts()
    }

    @Test
    fun `getActiveProducts returns only active products from dao`() = runTest {
        // Given
        val activeProducts = testProducts.filter { it.active }
        `when`(productDao.getActiveProducts()).thenReturn(flowOf(activeProducts))

        // When
        val result = productRepository.getActiveProducts()

        // Then
        assertEquals(activeProducts, result.collect { it })
        verify(productDao).getActiveProducts()
    }

    @Test
    fun `getActiveAndInStockProducts returns only active and in-stock products from dao`() = runTest {
        // Given
        val activeAndInStockProducts = testProducts.filter { it.active && it.inStock }
        `when`(productDao.getActiveAndInStockProducts()).thenReturn(flowOf(activeAndInStockProducts))

        // When
        val result = productRepository.getActiveAndInStockProducts()

        // Then
        assertEquals(activeAndInStockProducts, result.collect { it })
        verify(productDao).getActiveAndInStockProducts()
    }

    @Test
    fun `getProductById returns product with matching id`() = runTest {
        // Given
        val product = testProducts[0]
        `when`(productDao.getProductById(product.id)).thenReturn(product)

        // When
        val result = productRepository.getProductById(product.id)

        // Then
        assertEquals(product, result)
        verify(productDao).getProductById(product.id)
    }

    @Test
    fun `getProductById returns null when no product matches id`() = runTest {
        // Given
        val nonExistentId = 999
        `when`(productDao.getProductById(nonExistentId)).thenReturn(null)

        // When
        val result = productRepository.getProductById(nonExistentId)

        // Then
        assertNull(result)
        verify(productDao).getProductById(nonExistentId)
    }

    @Test
    fun `getProductByBarcode returns product with matching barcode`() = runTest {
        // Given
        val product = testProducts[0]
        `when`(productDao.getProductByBarcode(product.barcode)).thenReturn(product)

        // When
        val result = productRepository.getProductByBarcode(product.barcode)

        // Then
        assertEquals(product, result)
        verify(productDao).getProductByBarcode(product.barcode)
    }

    @Test
    fun `getAllCategories returns distinct categories from dao`() = runTest {
        // Given
        val categories = listOf("Beef", "Poultry", "Mexican")
        `when`(productDao.getAllCategories()).thenReturn(flowOf(categories))

        // When
        val result = productRepository.getAllCategories()

        // Then
        assertEquals(categories, result.collect { it })
        verify(productDao).getAllCategories()
    }

    @Test
    fun `insertProduct delegates to dao and returns inserted id`() = runTest {
        // Given
        val product = testProducts[0]
        val expectedId = 1L
        `when`(productDao.insert(product)).thenReturn(expectedId)

        // When
        val result = productRepository.insertProduct(product)

        // Then
        assertEquals(expectedId, result)
        verify(productDao).insert(product)
    }

    @Test
    fun `updateProduct delegates to dao`() = runTest {
        // Given
        val product = testProducts[0]

        // When
        productRepository.updateProduct(product)

        // Then
        verify(productDao).update(product)
    }

    @Test
    fun `deleteProduct delegates to dao`() = runTest {
        // Given
        val product = testProducts[0]

        // When
        productRepository.deleteProduct(product)

        // Then
        verify(productDao).delete(product)
    }

    @Test
    fun `updateActiveStatus delegates to dao with correct parameters`() = runTest {
        // Given
        val id = 1
        val active = false

        // When
        productRepository.updateActiveStatus(id, active)

        // Then
        verify(productDao).updateActiveStatus(eq(id), eq(active), any())
    }

    @Test
    fun `updateStockStatus delegates to dao with correct parameters`() = runTest {
        // Given
        val id = 1
        val inStock = false

        // When
        productRepository.updateStockStatus(id, inStock)

        // Then
        verify(productDao).updateStockStatus(eq(id), eq(inStock), any())
    }

    @Test
    fun `updateProductOrderSettings delegates to dao with correct parameters`() = runTest {
        // Given
        val id = 1
        val unitsPerCase = 24
        val minStockLevel = 5
        val maxStockLevel = 20

        // When
        productRepository.updateProductOrderSettings(id, unitsPerCase, minStockLevel, maxStockLevel)

        // Then
        verify(productDao).updateProductOrderSettings(
            eq(id),
            eq(unitsPerCase),
            eq(minStockLevel),
            eq(maxStockLevel),
            any()
        )
    }

    @Test(expected = RepositoryException::class)
    fun `getAllProducts throws RepositoryException when dao throws exception`() = runTest {
        // Given
        `when`(productDao.getAllProducts()).thenReturn(flow { throw RuntimeException("Database error") })

        // When
        productRepository.getAllProducts().collect { /* This should throw */ }

        // Then: expect RepositoryException
    }

    @Test(expected = RepositoryException::class)
    fun `getProductById throws RepositoryException when dao throws exception`() = runTest {
        // Given
        `when`(productDao.getProductById(any())).thenThrow(RuntimeException("Database error"))

        // When
        productRepository.getProductById(1)

        // Then: expect RepositoryException
    }
}