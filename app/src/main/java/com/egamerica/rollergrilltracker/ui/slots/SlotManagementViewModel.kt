package com.yourcompany.rollergrilltracker.ui.slots

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.rollergrilltracker.data.entities.Product
import com.yourcompany.rollergrilltracker.data.entities.SlotAssignment
import com.yourcompany.rollergrilltracker.data.repositories.ProductRepository
import com.yourcompany.rollergrilltracker.data.repositories.SlotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SlotManagementViewModel @Inject constructor(
    private val slotRepository: SlotRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _slotAssignments = MutableLiveData<Map<Int, List<SlotAssignment>>>()
    val slotAssignments: LiveData<Map<Int, List<SlotAssignment>>> = _slotAssignments

    private val _availableProducts = MutableLiveData<List<Product>>()
    val availableProducts: LiveData<List<Product>> = _availableProducts

    private val _saveStatus = MutableLiveData<SaveStatus>()
    val saveStatus: LiveData<SaveStatus> = _saveStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Track the current slot being edited
    private val _currentSlot = MutableLiveData<Int>()
    val currentSlot: LiveData<Int> = _currentSlot

    // Track the products assigned to the current slot
    private val _currentSlotProducts = MutableLiveData<List<Product>>()
    val currentSlotProducts: LiveData<List<Product>> = _currentSlotProducts

    init {
        loadSlotAssignments()
        loadAvailableProducts()
        _currentSlot.value = 1 // Default to first slot
    }

    private fun loadSlotAssignments() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val assignments = slotRepository.getAllSlotAssignments().first()
                
                // Group assignments by slot
                val groupedAssignments = assignments.groupBy { it.slotNumber }
                _slotAssignments.value = groupedAssignments
                
                // Update current slot products if needed
                updateCurrentSlotProducts()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadAvailableProducts() {
        viewModelScope.launch {
            try {
                val products = productRepository.getActiveProducts().first()
                _availableProducts.value = products
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun setCurrentSlot(slotNumber: Int) {
        _currentSlot.value = slotNumber
        updateCurrentSlotProducts()
    }

    private fun updateCurrentSlotProducts() {
        viewModelScope.launch {
            try {
                val slotNumber = _currentSlot.value ?: 1
                val assignments = _slotAssignments.value?.get(slotNumber) ?: emptyList()
                
                if (assignments.isNotEmpty()) {
                    val productIds = assignments.map { it.productId }
                    val products = productRepository.getProductsByIds(productIds).first()
                    _currentSlotProducts.value = products
                } else {
                    _currentSlotProducts.value = emptyList()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addProductToSlot(product: Product) {
        val slotNumber = _currentSlot.value ?: 1
        val currentProducts = _currentSlotProducts.value ?: emptyList()
        
        // Check if we already have 8 products in this slot
        if (currentProducts.size >= 8) {
            _saveStatus.value = SaveStatus.ERROR_SLOT_FULL
            return
        }
        
        // Check if product is already in this slot
        if (currentProducts.any { it.id == product.id }) {
            _saveStatus.value = SaveStatus.ERROR_PRODUCT_ALREADY_ASSIGNED
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val position = currentProducts.size + 1
                val assignment = SlotAssignment(
                    id = 0, // Room will generate the ID
                    slotNumber = slotNumber,
                    productId = product.id,
                    position = position
                )
                
                slotRepository.insertSlotAssignment(assignment)
                loadSlotAssignments() // Reload all assignments
                _saveStatus.value = SaveStatus.SUCCESS
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeProductFromSlot(product: Product) {
        val slotNumber = _currentSlot.value ?: 1
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get all assignments for this slot
                val assignments = _slotAssignments.value?.get(slotNumber) ?: emptyList()
                
                // Find the assignment for this product
                val assignment = assignments.find { it.productId == product.id }
                
                if (assignment != null) {
                    // Delete the assignment
                    slotRepository.deleteSlotAssignment(assignment)
                    
                    // Update positions for remaining assignments
                    val remainingAssignments = assignments.filter { it.productId != product.id }
                        .sortedBy { it.position }
                    
                    // Update positions
                    remainingAssignments.forEachIndexed { index, slotAssignment ->
                        val updatedAssignment = slotAssignment.copy(position = index + 1)
                        slotRepository.updateSlotAssignment(updatedAssignment)
                    }
                    
                    loadSlotAssignments() // Reload all assignments
                    _saveStatus.value = SaveStatus.SUCCESS
                }
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun moveProductUp(product: Product) {
        moveProduct(product, -1)
    }

    fun moveProductDown(product: Product) {
        moveProduct(product, 1)
    }

    private fun moveProduct(product: Product, positionChange: Int) {
        val slotNumber = _currentSlot.value ?: 1
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get all assignments for this slot
                val assignments = _slotAssignments.value?.get(slotNumber) ?: emptyList()
                
                // Find the assignment for this product
                val assignment = assignments.find { it.productId == product.id } ?: return@launch
                
                // Calculate new position
                val newPosition = assignment.position + positionChange
                
                // Check if new position is valid
                if (newPosition < 1 || newPosition > assignments.size) {
                    return@launch
                }
                
                // Find the assignment at the new position
                val swapAssignment = assignments.find { it.position == newPosition } ?: return@launch
                
                // Swap positions
                val updatedAssignment = assignment.copy(position = newPosition)
                val updatedSwapAssignment = swapAssignment.copy(position = assignment.position)
                
                slotRepository.updateSlotAssignment(updatedAssignment)
                slotRepository.updateSlotAssignment(updatedSwapAssignment)
                
                loadSlotAssignments() // Reload all assignments
                _saveStatus.value = SaveStatus.SUCCESS
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSlot() {
        val slotNumber = _currentSlot.value ?: 1
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get all assignments for this slot
                val assignments = _slotAssignments.value?.get(slotNumber) ?: emptyList()
                
                // Delete all assignments for this slot
                for (assignment in assignments) {
                    slotRepository.deleteSlotAssignment(assignment)
                }
                
                loadSlotAssignments() // Reload all assignments
                _saveStatus.value = SaveStatus.SUCCESS
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
        object ERROR_SLOT_FULL : SaveStatus()
        object ERROR_PRODUCT_ALREADY_ASSIGNED : SaveStatus()
    }
}