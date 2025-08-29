package com.egamerica.rollergrilltracker.ui.waste

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egamerica.rollergrilltracker.data.entities.Product
import com.egamerica.rollergrilltracker.data.entities.WasteDetail
import com.egamerica.rollergrilltracker.data.entities.WasteEntry
import com.egamerica.rollergrilltracker.data.repositories.ProductRepository
import com.egamerica.rollergrilltracker.data.repositories.WasteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WasteDisplayViewModel @Inject constructor(
    private val wasteRepository: WasteRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _selectedProduct = MutableLiveData<Product>()
    val selectedProduct: LiveData<Product> = _selectedProduct

    private val _wasteItems = MutableLiveData<List<WasteItem>>()
    val wasteItems: LiveData<List<WasteItem>> = _wasteItems

    private val _saveStatus = MutableLiveData<SaveStatus>()
    val saveStatus: LiveData<SaveStatus> = _saveStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadProducts()
        _wasteItems.value = mutableListOf()
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

    fun addWasteItem(product: Product, quantity: Int, reason: String) {
        val currentItems = _wasteItems.value?.toMutableList() ?: mutableListOf()
        currentItems.add(
            WasteItem(
                productId = product.id,
                productName = product.name,
                barcode = product.barcode,
                quantity = quantity,
                reason = reason
            )
        )
        _wasteItems.value = currentItems
    }

    fun removeWasteItem(index: Int) {
        val currentItems = _wasteItems.value?.toMutableList() ?: mutableListOf()
        if (index in currentItems.indices) {
            currentItems.removeAt(index)
            _wasteItems.value = currentItems
        }
    }

    fun clearAllWasteItems() {
        _wasteItems.value = mutableListOf()
    }

    fun saveWasteEntry() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val items = _wasteItems.value ?: return@launch
                if (items.isEmpty()) {
                    _saveStatus.value = SaveStatus.EMPTY
                    return@launch
                }
                
                // Create a new waste entry
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val today = dateFormat.format(Calendar.getInstance().time)
                
                val wasteEntry = WasteEntry(
                    id = 0, // Room will generate the ID
                    date = today,
                    timestamp = System.currentTimeMillis()
                )
                
                val entryId = wasteRepository.insertWasteEntry(wasteEntry).toInt()
                
                // Create waste details
                val wasteDetails = items.map { item ->
                    WasteDetail(
                        id = 0, // Room will generate the ID
                        wasteEntryId = entryId,
                        productId = item.productId,
                        quantity = item.quantity,
                        reason = item.reason
                    )
                }
                
                wasteRepository.insertWasteDetails(wasteDetails)
                
                // Clear the list after successful save
                clearAllWasteItems()
                
                _saveStatus.value = SaveStatus.SUCCESS
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    data class WasteItem(
        val productId: Int,
        val productName: String,
        val barcode: String,
        val quantity: Int,
        val reason: String
    )

    sealed class SaveStatus {
        object SUCCESS : SaveStatus()
        object ERROR : SaveStatus()
        object EMPTY : SaveStatus()
    }
}