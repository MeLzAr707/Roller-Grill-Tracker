package com.egamerica.rollergrilltracker.ui.products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egamerica.rollergrilltracker.data.entities.Product
import com.egamerica.rollergrilltracker.data.repositories.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: Int = savedStateHandle.get<Int>("productId") ?: 0

    private val _product = MutableLiveData<Product>()
    val product: LiveData<Product> = _product

    private val _productName = MutableLiveData<String>()
    val productName: LiveData<String> = _productName

    private val _barcode = MutableLiveData<String>()
    val barcode: LiveData<String> = _barcode

    private val _category = MutableLiveData<String>()
    val category: LiveData<String> = _category

    private val _isActive = MutableLiveData<Boolean>()
    val isActive: LiveData<Boolean> = _isActive

    private val _inStock = MutableLiveData<Boolean>()
    val inStock: LiveData<Boolean> = _inStock

    private val _saveStatus = MutableLiveData<SaveStatus>()
    val saveStatus: LiveData<SaveStatus> = _saveStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isNewProduct = MutableLiveData<Boolean>()
    val isNewProduct: LiveData<Boolean> = _isNewProduct

    init {
        _isNewProduct.value = productId == 0
        if (productId > 0) {
            loadProduct()
        } else {
            // Initialize with default values for a new product
            _productName.value = ""
            _barcode.value = ""
            _category.value = ""
            _isActive.value = true
            _inStock.value = true
        }
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val product = productRepository.getProductById(productId).first()
                _product.value = product
                _productName.value = product.name
                _barcode.value = product.barcode
                _category.value = product.category
                _isActive.value = product.isActive
                _inStock.value = product.inStock
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setProductName(name: String) {
        _productName.value = name
    }

    fun setBarcode(barcode: String) {
        _barcode.value = barcode
    }

    fun setCategory(category: String) {
        _category.value = category
    }

    fun setIsActive(isActive: Boolean) {
        _isActive.value = isActive
    }

    fun setInStock(inStock: Boolean) {
        _inStock.value = inStock
    }

    fun saveProduct() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val name = _productName.value ?: ""
                val barcode = _barcode.value ?: ""
                val category = _category.value ?: ""
                val isActive = _isActive.value ?: true
                val inStock = _inStock.value ?: true
                
                if (name.isBlank()) {
                    _saveStatus.value = SaveStatus.ERROR_EMPTY_NAME
                    return@launch
                }
                
                val product = if (productId > 0) {
                    Product(
                        id = productId,
                        name = name,
                        barcode = barcode,
                        category = category,
                        isActive = isActive,
                        inStock = inStock
                    )
                } else {
                    Product(
                        id = 0, // Room will generate the ID
                        name = name,
                        barcode = barcode,
                        category = category,
                        isActive = isActive,
                        inStock = inStock
                    )
                }
                
                if (productId > 0) {
                    productRepository.updateProduct(product)
                } else {
                    productRepository.insertProduct(product)
                }
                
                _saveStatus.value = SaveStatus.SUCCESS
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProduct() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val product = _product.value ?: return@launch
                productRepository.deleteProduct(product)
                _saveStatus.value = SaveStatus.DELETED
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    sealed class SaveStatus {
        object SUCCESS : SaveStatus()
        object ERROR : SaveStatus()
        object ERROR_EMPTY_NAME : SaveStatus()
        object DELETED : SaveStatus()
    }
}