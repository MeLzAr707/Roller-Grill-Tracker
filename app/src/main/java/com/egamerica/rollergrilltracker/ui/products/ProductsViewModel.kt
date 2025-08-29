package com.yourcompany.rollergrilltracker.ui.products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.rollergrilltracker.data.entities.Product
import com.yourcompany.rollergrilltracker.data.repositories.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _filteredProducts = MutableLiveData<List<Product>>()
    val filteredProducts: LiveData<List<Product>> = _filteredProducts

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    private val _selectedCategory = MutableLiveData<String>()
    val selectedCategory: LiveData<String> = _selectedCategory

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadProducts()
        _searchQuery.value = ""
        _selectedCategory.value = "All"
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allProducts = productRepository.getAllProducts().first()
                _products.value = allProducts
                _filteredProducts.value = allProducts
                
                // Extract unique categories
                val uniqueCategories = allProducts.map { it.category }.distinct().filter { it.isNotBlank() }
                _categories.value = listOf("All") + uniqueCategories
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterProducts()
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
        filterProducts()
    }

    private fun filterProducts() {
        val query = _searchQuery.value ?: ""
        val category = _selectedCategory.value ?: "All"
        val allProducts = _products.value ?: emptyList()
        
        val filtered = allProducts.filter { product ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                product.name.contains(query, ignoreCase = true) ||
                product.barcode.contains(query, ignoreCase = true)
            }
            
            val matchesCategory = if (category == "All") {
                true
            } else {
                product.category == category
            }
            
            matchesQuery && matchesCategory
        }
        
        _filteredProducts.value = filtered
    }

    fun refreshProducts() {
        loadProducts()
    }

    fun toggleProductActive(product: Product) {
        viewModelScope.launch {
            try {
                val updatedProduct = product.copy(isActive = !product.isActive)
                productRepository.updateProduct(updatedProduct)
                refreshProducts()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun toggleProductStock(product: Product) {
        viewModelScope.launch {
            try {
                val updatedProduct = product.copy(inStock = !product.inStock)
                productRepository.updateProduct(updatedProduct)
                refreshProducts()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}