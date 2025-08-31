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
import java.time.format.DateTimeFormatter
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
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val formattedDate = dateFormat.format(date)
                
                // Get order suggestions for the selected date
                val suggestions = orderSuggestionRepository.getOrderSuggestionsByDate(formattedDate).first()
                
                // If no suggestions exist, generate them
                if (suggestions.isEmpty()) {
                    generateOrderSuggestions(formattedDate)
                    return@launch
                }
                
                // Get products
                val productIds = suggestions.map { it.productId }
                val products = productRepository.getProductsByIds(productIds).first()
                val productMap = products.associateBy { it.id }
                
                // Create order suggestion items
                val items = suggestions.mapNotNull { suggestion ->
                    val product = productMap[suggestion.productId] ?: return@mapNotNull null
                    
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

    private suspend fun generateOrderSuggestions(date: String) {
        try {
            // Convert string date to LocalDate
            val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            
            // Get all active products
            val products = productRepository.getActiveProducts().first()
            
            // Generate suggestions for each product
            val suggestions = products.map { product ->
                // In a real app, this would use historical data and complex algorithms
                // For now, we'll use simple defaults
                val unitsPerCase = 12
                val suggestedCases = (1..3).random() // Random number between 1 and 3
                val suggestedUnits = (0..unitsPerCase - 1).random() // Random number between 0 and unitsPerCase-1
                
                OrderSuggestion(
                    id = 0, // Room will generate the ID
                    date = localDate,
                    productId = product.id,
                    suggestedCases = suggestedCases,
                    suggestedUnits = suggestedUnits
                )
            }
            
            // Save the suggestions
            orderSuggestionRepository.insertOrderSuggestions(suggestions)
            
            // Reload the suggestions
            loadOrderSuggestions()
        } catch (e: Exception) {
            // Handle error
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