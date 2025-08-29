package com.yourcompany.rollergrilltracker.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.rollergrilltracker.data.entities.TimePeriod
import com.yourcompany.rollergrilltracker.data.repositories.SalesRepository
import com.yourcompany.rollergrilltracker.data.repositories.SuggestionRepository
import com.yourcompany.rollergrilltracker.data.repositories.TimePeriodRepository
import com.yourcompany.rollergrilltracker.data.repositories.WasteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val wasteRepository: WasteRepository,
    private val timePeriodRepository: TimePeriodRepository,
    private val suggestionRepository: SuggestionRepository
) : ViewModel() {

    private val _currentTimePeriod = MutableLiveData<TimePeriod>()
    val currentTimePeriod: LiveData<TimePeriod> = _currentTimePeriod

    private val _todaySalesSummary = MutableLiveData<String>()
    val todaySalesSummary: LiveData<String> = _todaySalesSummary

    private val _todayWasteSummary = MutableLiveData<String>()
    val todayWasteSummary: LiveData<String> = _todayWasteSummary

    private val _topSuggestions = MutableLiveData<List<String>>()
    val topSuggestions: LiveData<List<String>> = _topSuggestions

    init {
        loadCurrentTimePeriod()
        loadTodaySalesSummary()
        loadTodayWasteSummary()
        loadTopSuggestions()
    }

    private fun loadCurrentTimePeriod() {
        viewModelScope.launch {
            try {
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val timePeriods = timePeriodRepository.getAllTimePeriods().first()
                
                // Find the current time period based on the hour
                val currentPeriod = when (currentHour) {
                    in 6..9 -> timePeriods.find { it.name.contains("Morning", ignoreCase = true) }
                    in 10..13 -> timePeriods.find { it.name.contains("Midday", ignoreCase = true) }
                    in 14..17 -> timePeriods.find { it.name.contains("Afternoon", ignoreCase = true) }
                    in 18..21 -> timePeriods.find { it.name.contains("Evening", ignoreCase = true) }
                    else -> null
                }
                
                currentPeriod?.let {
                    _currentTimePeriod.value = it
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun loadTodaySalesSummary() {
        viewModelScope.launch {
            try {
                val today = Calendar.getInstance().time
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val todayFormatted = dateFormat.format(today)
                
                val salesEntries = salesRepository.getSalesEntriesByDate(todayFormatted).first()
                val totalSales = salesEntries.sumOf { entry ->
                    salesRepository.getSalesDetailsBySalesEntryId(entry.id).first().sumOf { it.quantity }
                }
                
                _todaySalesSummary.value = "$totalSales items sold today"
            } catch (e: Exception) {
                _todaySalesSummary.value = "No sales data available"
            }
        }
    }

    private fun loadTodayWasteSummary() {
        viewModelScope.launch {
            try {
                val today = Calendar.getInstance().time
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val todayFormatted = dateFormat.format(today)
                
                val wasteEntries = wasteRepository.getWasteEntriesByDate(todayFormatted).first()
                val totalWaste = wasteEntries.sumOf { entry ->
                    wasteRepository.getWasteDetailsByWasteEntryId(entry.id).first().sumOf { it.quantity }
                }
                
                _todayWasteSummary.value = "$totalWaste items wasted today"
            } catch (e: Exception) {
                _todayWasteSummary.value = "No waste data available"
            }
        }
    }

    private fun loadTopSuggestions() {
        viewModelScope.launch {
            try {
                val suggestions = suggestionRepository.getTopSuggestions(3).first()
                _topSuggestions.value = suggestions.map { suggestion ->
                    "${suggestion.productName}: ${suggestion.suggestedQuantity} items (${suggestion.confidenceScore}% confidence)"
                }
            } catch (e: Exception) {
                _topSuggestions.value = emptyList()
            }
        }
    }
}