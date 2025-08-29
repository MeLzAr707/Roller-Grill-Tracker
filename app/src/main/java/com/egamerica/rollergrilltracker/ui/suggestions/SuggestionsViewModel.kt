package com.egamerica.rollergrilltracker.ui.suggestions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egamerica.rollergrilltracker.data.entities.Product
import com.egamerica.rollergrilltracker.data.entities.Suggestion
import com.egamerica.rollergrilltracker.data.entities.TimePeriod
import com.egamerica.rollergrilltracker.data.repositories.ProductRepository
import com.egamerica.rollergrilltracker.data.repositories.SuggestionRepository
import com.egamerica.rollergrilltracker.data.repositories.TimePeriodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SuggestionsViewModel @Inject constructor(
    private val suggestionRepository: SuggestionRepository,
    private val productRepository: ProductRepository,
    private val timePeriodRepository: TimePeriodRepository
) : ViewModel() {

    private val _selectedDate = MutableLiveData<Date>()
    val selectedDate: LiveData<Date> = _selectedDate

    private val _selectedTimePeriod = MutableLiveData<TimePeriod>()
    val selectedTimePeriod: LiveData<TimePeriod> = _selectedTimePeriod

    private val _timePeriods = MutableLiveData<List<TimePeriod>>()
    val timePeriods: LiveData<List<TimePeriod>> = _timePeriods

    private val _suggestions = MutableLiveData<List<SuggestionWithProduct>>()
    val suggestions: LiveData<List<SuggestionWithProduct>> = _suggestions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        _selectedDate.value = Calendar.getInstance().time
        loadTimePeriods()
    }

    private fun loadTimePeriods() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val periods = timePeriodRepository.getAllTimePeriods().first()
                _timePeriods.value = periods
                
                // Set default time period based on current time
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val currentPeriod = when (currentHour) {
                    in 6..9 -> periods.find { it.name.contains("Morning", ignoreCase = true) }
                    in 10..13 -> periods.find { it.name.contains("Midday", ignoreCase = true) }
                    in 14..17 -> periods.find { it.name.contains("Afternoon", ignoreCase = true) }
                    in 18..21 -> periods.find { it.name.contains("Evening", ignoreCase = true) }
                    else -> periods.firstOrNull()
                }
                
                currentPeriod?.let {
                    _selectedTimePeriod.value = it
                    loadSuggestions()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
        loadSuggestions()
    }

    fun setSelectedTimePeriod(timePeriod: TimePeriod) {
        _selectedTimePeriod.value = timePeriod
        loadSuggestions()
    }

    fun refreshSuggestions() {
        loadSuggestions()
    }

    private fun loadSuggestions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val date = _selectedDate.value ?: return@launch
                val timePeriod = _selectedTimePeriod.value ?: return@launch
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val formattedDate = dateFormat.format(date)
                
                val suggestions = suggestionRepository.getSuggestionsByDateAndTimePeriod(
                    formattedDate, timePeriod.id
                ).first()
                
                val products = productRepository.getAllProducts().first()
                val productMap = products.associateBy { it.id }
                
                val suggestionsWithProducts = suggestions.map { suggestion ->
                    val product = productMap[suggestion.productId]
                    SuggestionWithProduct(
                        suggestion = suggestion,
                        product = product ?: Product(
                            id = suggestion.productId,
                            name = "Unknown Product",
                            barcode = "",
                            category = "",
                            isActive = true,
                            inStock = true
                        )
                    )
                }.sortedByDescending { it.suggestion.confidenceScore }
                
                _suggestions.value = suggestionsWithProducts
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    data class SuggestionWithProduct(
        val suggestion: Suggestion,
        val product: Product
    )
}