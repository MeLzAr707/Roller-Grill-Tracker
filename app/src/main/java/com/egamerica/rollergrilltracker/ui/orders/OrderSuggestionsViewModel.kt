package com.egamerica.rollergrilltracker.ui.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egamerica.rollergrilltracker.data.entities.OrderSuggestion
import com.egamerica.rollergrilltracker.data.entities.Product
import com.egamerica.rollergrilltracker.data.repositories.OrderSettingsRepository
import com.egamerica.rollergrilltracker.data.repositories.OrderSuggestionRepository
import com.egamerica.rollergrilltracker.data.repositories.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OrderSuggestionsViewModel @Inject constructor(
    private val orderSuggestionRepository: OrderSuggestionRepository,
    private val productRepository: ProductRepository,
    private val orderSettingsRepository: OrderSettingsRepository
) : ViewModel() {

    private val _selectedDate = MutableLiveData<Date>()
    val selectedDate: LiveData<Date> = _selectedDate

    private val _orderSuggestions = MutableLiveData<List<OrderSuggestionItem>>()
    val orderSuggestions: LiveData<List<OrderSuggestionItem>> = _orderSuggestions

    private val _orderSummary = MutableLiveData<OrderSummary>()
    val orderSummary: LiveData<OrderSummary> = _orderSummary

    private val _exportStatus = MutableLiveData<ExportStatus>()
    val exportStatus: LiveData<ExportStatus> = _exportStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        // Set default date to next order date
        viewModelScope.launch {
            val nextOrderDate = getNextOrderDate()
            _selectedDate.value = nextOrderDate ?: Calendar.getInstance().time
            loadOrderSuggestions()
        }
    }

    private suspend fun getNextOrderDate(): Date? {
        try {
            val settings = orderSettingsRepository.getOrderSettings().first()
            if (settings.isEmpty()) {
                return null
            }
            
            val orderSettings = settings.first()
            val orderDays = orderSettings.orderDays.split(",")
                .filter { it.isNotBlank() }
                .map { it.toInt() }
            
            if (orderDays.isEmpty()) {
                return null
            }
            
            val calendar = Calendar.getInstance()
            val today = calendar.get(Calendar.DAY_OF_WEEK)
            
            // Find the next order day
            var daysChecked = 0
            while (daysChecked < 7) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                daysChecked++
                
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                if (orderDays.contains(dayOfWeek)) {
                    // Add lead time
                    calendar.add(Calendar.DAY_OF_YEAR, orderSettings.leadTime)
                    return calendar.time
                }
            }
            
            return null
        } catch (e: Exception) {
            return null
        }
    }

    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
        loadOrderSuggestions()
    }

    fun refreshOrderSuggestions() {
        loadOrderSuggestions()
    }

    private fun loadOrderSuggestions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val date = _selectedDate.value ?: return@launch
                // Convert Date to LocalDate
                val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                
                // Get order suggestions for the selected date
                val suggestions = orderSuggestionRepository.getOrderSuggestionsForDate(localDate)
                
                // If no suggestions exist, generate them
                if (suggestions.isEmpty()) {
                    orderSuggestionRepository.generateOrderSuggestions(localDate)
                    return@launch
                }
                
                // Create order suggestion items
                val items = suggestions.map { suggestion ->
                    // Create Product object from the suggestion data
                    val product = Product(
                        id = suggestion.productId,
                        name = suggestion.productName,
                        category = suggestion.category,
                        barcode = "", // Not available in the query, set to empty
                        isActive = true, // Assuming active since it's in suggestions
                        inStock = true // Assuming in stock since it's in suggestions
                    )
                    
                    OrderSuggestionItem(
                        product = product,
                        unitsPerCase = suggestion.unitsPerCase,
                        suggestedCases = suggestion.suggestedCases,
                        suggestedUnits = suggestion.suggestedUnits,
                        totalUnits = (suggestion.suggestedCases * suggestion.unitsPerCase) + suggestion.suggestedUnits
                    )
                }.sortedBy { it.product.name }
                
                _orderSuggestions.value = items
                
                // Calculate order summary
                calculateOrderSummary(items)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateOrderSummary(items: List<OrderSuggestionItem>) {
        val totalCases = items.sumOf { it.suggestedCases }
        val totalUnits = items.sumOf { it.suggestedUnits }
        val totalProducts = items.size
        
        _orderSummary.value = OrderSummary(
            totalCases = totalCases,
            totalUnits = totalUnits,
            totalProducts = totalProducts
        )
    }

    fun exportOrder() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // In a real app, this would generate a PDF or CSV file
                // For now, we'll just simulate success
                _exportStatus.value = ExportStatus.SUCCESS
            } catch (e: Exception) {
                _exportStatus.value = ExportStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    data class OrderSuggestionItem(
        val product: Product,
        val unitsPerCase: Int,
        val suggestedCases: Int,
        val suggestedUnits: Int,
        val totalUnits: Int
    )

    data class OrderSummary(
        val totalCases: Int,
        val totalUnits: Int,
        val totalProducts: Int
    )

    sealed class ExportStatus {
        object SUCCESS : ExportStatus()
        object ERROR : ExportStatus()
    }
}