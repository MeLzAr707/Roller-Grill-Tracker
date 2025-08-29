package com.egamerica.rollergrilltracker.ui.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egamerica.rollergrilltracker.data.entities.OrderSettings
import com.egamerica.rollergrilltracker.data.repositories.OrderSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OrderSettingsViewModel @Inject constructor(
    private val orderSettingsRepository: OrderSettingsRepository
) : ViewModel() {

    private val _orderFrequency = MutableLiveData<Int>()
    val orderFrequency: LiveData<Int> = _orderFrequency

    private val _orderDays = MutableLiveData<Set<Int>>()
    val orderDays: LiveData<Set<Int>> = _orderDays

    private val _leadTime = MutableLiveData<Int>()
    val leadTime: LiveData<Int> = _leadTime

    private val _nextOrderDates = MutableLiveData<List<Date>>()
    val nextOrderDates: LiveData<List<Date>> = _nextOrderDates

    private val _saveStatus = MutableLiveData<SaveStatus>()
    val saveStatus: LiveData<SaveStatus> = _saveStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadOrderSettings()
    }

    private fun loadOrderSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val settings = orderSettingsRepository.getOrderSettings().first()
                
                // Set default values if no settings exist
                if (settings.isEmpty()) {
                    _orderFrequency.value = 2 // Twice a week
                    _orderDays.value = setOf(Calendar.MONDAY, Calendar.THURSDAY) // Monday and Thursday
                    _leadTime.value = 1 // Next day
                } else {
                    val orderSettings = settings.first()
                    _orderFrequency.value = orderSettings.orderFrequency
                    _orderDays.value = parseOrderDays(orderSettings.orderDays)
                    _leadTime.value = orderSettings.leadTime
                }
                
                calculateNextOrderDates()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun parseOrderDays(orderDaysString: String): Set<Int> {
        return orderDaysString.split(",")
            .filter { it.isNotBlank() }
            .map { it.toInt() }
            .toSet()
    }

    private fun formatOrderDays(orderDays: Set<Int>): String {
        return orderDays.joinToString(",")
    }

    fun setOrderFrequency(frequency: Int) {
        _orderFrequency.value = frequency
        updateOrderDaysBasedOnFrequency(frequency)
        calculateNextOrderDates()
    }

    private fun updateOrderDaysBasedOnFrequency(frequency: Int) {
        // Suggest appropriate order days based on frequency
        val days = when (frequency) {
            1 -> setOf(Calendar.MONDAY) // Weekly
            2 -> setOf(Calendar.MONDAY, Calendar.THURSDAY) // Twice a week
            3 -> setOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY) // Three times a week
            5 -> setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY) // Every weekday
            7 -> setOf(Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY) // Daily
            else -> _orderDays.value ?: emptySet()
        }
        
        _orderDays.value = days
    }

    fun toggleOrderDay(day: Int) {
        val currentDays = _orderDays.value ?: emptySet()
        val newDays = if (currentDays.contains(day)) {
            currentDays - day
        } else {
            currentDays + day
        }
        _orderDays.value = newDays
        calculateNextOrderDates()
    }

    fun setLeadTime(leadTime: Int) {
        _leadTime.value = leadTime
        calculateNextOrderDates()
    }

    private fun calculateNextOrderDates() {
        val orderDays = _orderDays.value ?: emptySet()
        val leadTime = _leadTime.value ?: 1
        
        if (orderDays.isEmpty()) {
            _nextOrderDates.value = emptyList()
            return
        }
        
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val dates = mutableListOf<Date>()
        
        // Find the next 3 order dates
        var daysAdded = 0
        var daysChecked = 0
        
        while (daysAdded < 3 && daysChecked < 14) { // Check up to 2 weeks ahead
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            daysChecked++
            
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (orderDays.contains(dayOfWeek)) {
                // Add lead time
                val orderDate = calendar.clone() as Calendar
                orderDate.add(Calendar.DAY_OF_YEAR, leadTime)
                dates.add(orderDate.time)
                daysAdded++
            }
        }
        
        _nextOrderDates.value = dates
    }

    fun saveOrderSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val frequency = _orderFrequency.value ?: 2
                val days = _orderDays.value ?: emptySet()
                val leadTime = _leadTime.value ?: 1
                
                if (days.isEmpty()) {
                    _saveStatus.value = SaveStatus.ERROR_NO_DAYS
                    return@launch
                }
                
                val orderSettings = OrderSettings(
                    id = 1, // Use a fixed ID for settings
                    orderFrequency = frequency,
                    orderDays = formatOrderDays(days),
                    leadTime = leadTime
                )
                
                orderSettingsRepository.updateOrderSettings(orderSettings)
                
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
        object ERROR_NO_DAYS : SaveStatus()
    }
}