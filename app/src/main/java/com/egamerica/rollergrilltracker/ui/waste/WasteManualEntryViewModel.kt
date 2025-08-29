package com.egamerica.rollergrilltracker.ui.waste

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egamerica.rollergrilltracker.data.entities.Product
import com.egamerica.rollergrilltracker.data.repositories.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WasteManualEntryViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _selectedProduct = MutableLiveData<Product>()
    val selectedProduct: LiveData<Product> = _selectedProduct

    private val _quantity = MutableLiveData(1)
    val quantity: LiveData<Int> = _quantity

    private val _reason = MutableLiveData<String>()
    val reason: LiveData<String> = _reason

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _wasteItem = MutableLiveData<WasteDisplayViewModel.WasteItem?>()
    val wasteItem: LiveData<WasteDisplayViewModel.WasteItem?> = _wasteItem

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val activeProducts = productRepository.getActiveProducts().first()
                _products.value = activeProducts
                if (activeProducts.isNotEmpty()) {
                    _selectedProduct.value = activeProducts.first()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSelectedProduct(product: Product) {
        _selectedProduct.value = product
    }

    fun setQuantity(quantity: Int) {
        if (quantity > 0) {
            _quantity.value = quantity
        }
    }

    fun incrementQuantity() {
        _quantity.value = (_quantity.value ?: 1) + 1
    }

    fun decrementQuantity() {
        val currentQuantity = _quantity.value ?: 1
        if (currentQuantity > 1) {
            _quantity.value = currentQuantity - 1
        }
    }

    fun setReason(reason: String) {
        _reason.value = reason
    }

    fun createWasteItem() {
        val product = _selectedProduct.value ?: return
        val quantity = _quantity.value ?: 1
        val reason = _reason.value ?: ""

        if (reason.isBlank()) {
            return
        }

        _wasteItem.value = WasteDisplayViewModel.WasteItem(
            productId = product.id,
            productName = product.name,
            barcode = product.barcode,
            quantity = quantity,
            reason = reason
        )
    }

    fun resetWasteItem() {
        _wasteItem.value = null
    }
}