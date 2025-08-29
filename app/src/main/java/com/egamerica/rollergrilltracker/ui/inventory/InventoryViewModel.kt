package com.yourcompany.rollergrilltracker.ui.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.rollergrilltracker.data.entities.InventoryCount
import com.yourcompany.rollergrilltracker.data.entities.Product
import com.yourcompany.rollergrilltracker.data.repositories.InventoryRepository
import com.yourcompany.rollergrilltracker.data.repositories.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _selectedDate = MutableLiveData<Date>()
    val selectedDate: LiveData<Date> = _selectedDate

    private val _inventoryItems = MutableLiveData<List<InventoryItem>>()
    val inventoryItems: LiveData<List<InventoryItem>> = _inventoryItems

    private val _saveStatus = MutableLiveData<SaveStatus>()
    val saveStatus: LiveData<SaveStatus> = _saveStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        _selectedDate.value = Calendar.getInstance().time
        loadInventoryData()
    }

    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
        loadInventoryData()
    }

    private fun loadInventoryData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val date = _selectedDate.value ?: return@launch
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val formattedDate = dateFormat.format(date)
                
                // Get all active products
                val products = productRepository.getActiveProducts().first()
                
                // Get inventory counts for the selected date
                val inventoryCounts = inventoryRepository.getInventoryCountsByDate(formattedDate).first()
                
                // Create inventory items by combining products and counts
                val items = products.map { product ->
                    val count = inventoryCounts.find { it.productId == product.id }
                    
                    InventoryItem(
                        product = product,
                        startingCount = count?.startingCount ?: 0,
                        deliveryCount = count?.deliveryCount ?: 0,
                        endingCount = count?.endingCount ?: 0,
                        used = calculateUsed(count?.startingCount ?: 0, count?.deliveryCount ?: 0, count?.endingCount ?: 0)
                    )
                }
                
                _inventoryItems.value = items
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateUsed(starting: Int, delivery: Int, ending: Int): Int {
        return starting + delivery - ending
    }

    fun updateStartingCount(productId: Int, count: Int) {
        updateInventoryItem(productId) { item ->
            item.copy(startingCount = count, used = calculateUsed(count, item.deliveryCount, item.endingCount))
        }
    }

    fun updateDeliveryCount(productId: Int, count: Int) {
        updateInventoryItem(productId) { item ->
            item.copy(deliveryCount = count, used = calculateUsed(item.startingCount, count, item.endingCount))
        }
    }

    fun updateEndingCount(productId: Int, count: Int) {
        updateInventoryItem(productId) { item ->
            item.copy(endingCount = count, used = calculateUsed(item.startingCount, item.deliveryCount, count))
        }
    }

    private fun updateInventoryItem(productId: Int, update: (InventoryItem) -> InventoryItem) {
        val currentItems = _inventoryItems.value ?: return
        val updatedItems = currentItems.map { item ->
            if (item.product.id == productId) {
                update(item)
            } else {
                item
            }
        }
        _inventoryItems.value = updatedItems
    }

    fun saveInventory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val date = _selectedDate.value ?: return@launch
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val formattedDate = dateFormat.format(date)
                
                val items = _inventoryItems.value ?: return@launch
                
                // Create inventory counts from items
                val inventoryCounts = items.map { item ->
                    InventoryCount(
                        id = 0, // Room will generate the ID
                        date = formattedDate,
                        productId = item.product.id,
                        startingCount = item.startingCount,
                        deliveryCount = item.deliveryCount,
                        endingCount = item.endingCount
                    )
                }
                
                // Delete existing counts for this date
                inventoryRepository.deleteInventoryCountsByDate(formattedDate)
                
                // Insert new counts
                inventoryRepository.insertInventoryCounts(inventoryCounts)
                
                _saveStatus.value = SaveStatus.SUCCESS
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    data class InventoryItem(
        val product: Product,
        val startingCount: Int,
        val deliveryCount: Int,
        val endingCount: Int,
        val used: Int
    )

    sealed class SaveStatus {
        object SUCCESS : SaveStatus()
        object ERROR : SaveStatus()
    }
}