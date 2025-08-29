package com.egamerica.rollergrilltracker.ui.holdtime

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egamerica.rollergrilltracker.data.dao.ProductWithHoldTime
import com.egamerica.rollergrilltracker.data.dao.SlotWithProduct
import com.egamerica.rollergrilltracker.data.entities.ProductHoldTime
import com.egamerica.rollergrilltracker.data.repositories.ProductHoldTimeRepository
import com.egamerica.rollergrilltracker.data.repositories.SlotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ProductHoldTimeViewModel @Inject constructor(
    private val productHoldTimeRepository: ProductHoldTimeRepository,
    private val slotRepository: SlotRepository
) : ViewModel() {

    private val _activeHoldTimes = MutableLiveData<List<ProductHoldTime>>()
    val activeHoldTimes: LiveData<List<ProductHoldTime>> = _activeHoldTimes

    private val _activeHoldTimesWithProducts = MutableLiveData<List<ProductWithHoldTime>>()
    val activeHoldTimesWithProducts: LiveData<List<ProductWithHoldTime>> = _activeHoldTimesWithProducts

    private val _holdTimesByGrill = MutableLiveData<Map<Int, List<ProductHoldTime>>>()
    val holdTimesByGrill: LiveData<Map<Int, List<ProductHoldTime>>> = _holdTimesByGrill

    private val _slotsWithProducts = MutableLiveData<List<SlotWithProduct>>()
    val slotsWithProducts: LiveData<List<SlotWithProduct>> = _slotsWithProducts

    private val _expiredHoldTimes = MutableLiveData<List<ProductHoldTime>>()
    val expiredHoldTimes: LiveData<List<ProductHoldTime>> = _expiredHoldTimes

    private val _remainingTimeMinutes = MutableLiveData<Map<Int, Int>>()
    val remainingTimeMinutes: LiveData<Map<Int, Int>> = _remainingTimeMinutes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _actionStatus = MutableLiveData<ActionStatus>()
    val actionStatus: LiveData<ActionStatus> = _actionStatus

    init {
        loadActiveHoldTimes()
        loadSlotsWithProducts()
        startHoldTimeMonitoring()
    }

    private fun loadActiveHoldTimes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val holdTimes = productHoldTimeRepository.getActiveHoldTimes().first()
                _activeHoldTimes.value = holdTimes
                
                // Group by grill
                val byGrill = holdTimes.groupBy { it.grillNumber }
                _holdTimesByGrill.value = byGrill
                
                // Get hold times with product details
                val withProducts = productHoldTimeRepository.getActiveHoldTimesWithProducts().first()
                _activeHoldTimesWithProducts.value = withProducts
                
                // Calculate remaining time for each hold time
                updateRemainingTimes()
                
                // Check for expired hold times
                checkExpiredHoldTimes()
            } catch (e: Exception) {
                // Handle error
                _actionStatus.value = ActionStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadSlotsWithProducts() {
        viewModelScope.launch {
            try {
                val slots = slotRepository.getAllSlotsWithProducts().first()
                _slotsWithProducts.value = slots
            } catch (e: Exception) {
                // Handle error
                _actionStatus.value = ActionStatus.ERROR
            }
        }
    }

    private fun startHoldTimeMonitoring() {
        viewModelScope.launch {
            while (true) {
                updateRemainingTimes()
                checkExpiredHoldTimes()
                delay(60000) // Update every minute
            }
        }
    }

    private fun updateRemainingTimes() {
        val holdTimes = _activeHoldTimes.value ?: return
        val now = LocalDateTime.now()
        
        val remainingTimes = holdTimes.associate { holdTime ->
            val remainingMinutes = Duration.between(now, holdTime.expirationTime).toMinutes().toInt()
            holdTime.id to maxOf(0, remainingMinutes)
        }
        
        _remainingTimeMinutes.value = remainingTimes
    }

    private fun checkExpiredHoldTimes() {
        viewModelScope.launch {
            try {
                val expired = productHoldTimeRepository.getExpiredHoldTimes()
                _expiredHoldTimes.value = expired
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun startHoldTime(productId: Int, slotAssignmentId: Int, grillNumber: Int, slotNumber: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productHoldTimeRepository.startHoldTime(
                    productId = productId,
                    slotAssignmentId = slotAssignmentId,
                    grillNumber = grillNumber,
                    slotNumber = slotNumber
                )
                
                // Reload hold times
                loadActiveHoldTimes()
                
                _actionStatus.value = ActionStatus.SUCCESS
            } catch (e: Exception) {
                _actionStatus.value = ActionStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsDiscarded(holdTimeId: Int, discardReason: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productHoldTimeRepository.markAsDiscarded(holdTimeId, discardReason)
                
                // Reload hold times
                loadActiveHoldTimes()
                
                _actionStatus.value = ActionStatus.SUCCESS
            } catch (e: Exception) {
                _actionStatus.value = ActionStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deactivateHoldTimesForSlot(slotAssignmentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productHoldTimeRepository.deactivateHoldTimesForSlot(slotAssignmentId)
                
                // Reload hold times
                loadActiveHoldTimes()
                
                _actionStatus.value = ActionStatus.SUCCESS
            } catch (e: Exception) {
                _actionStatus.value = ActionStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getHoldTimeStatus(holdTimeId: Int): HoldTimeStatus {
        val remainingMinutes = _remainingTimeMinutes.value?.get(holdTimeId) ?: return HoldTimeStatus.UNKNOWN
        
        return when {
            remainingMinutes <= 0 -> HoldTimeStatus.EXPIRED
            remainingMinutes <= 30 -> HoldTimeStatus.WARNING
            else -> HoldTimeStatus.GOOD
        }
    }

    enum class HoldTimeStatus {
        GOOD,      // More than 30 minutes remaining
        WARNING,   // 30 minutes or less remaining
        EXPIRED,   // Expired
        UNKNOWN    // Status unknown
    }

    sealed class ActionStatus {
        object SUCCESS : ActionStatus()
        object ERROR : ActionStatus()
    }
}