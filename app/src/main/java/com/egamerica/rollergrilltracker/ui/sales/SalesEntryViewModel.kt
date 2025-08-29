package com.yourcompany.rollergrilltracker.ui.sales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.rollergrilltracker.data.entities.Product
import com.yourcompany.rollergrilltracker.data.entities.SalesDetail
import com.yourcompany.rollergrilltracker.data.entities.SalesEntry
import com.yourcompany.rollergrilltracker.data.entities.TimePeriod
import com.yourcompany.rollergrilltracker.data.repositories.ProductRepository
import com.yourcompany.rollergrilltracker.data.repositories.SalesRepository
import com.yourcompany.rollergrilltracker.data.repositories.TimePeriodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SalesEntryViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val productRepository: ProductRepository,
    private val timePeriodRepository: TimePeriodRepository
) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _timePeriods = MutableLiveData<List<TimePeriod>>()
    val timePeriods: LiveData<List<TimePeriod>> = _timePeriods

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    private val _selectedTimePeriod = MutableLiveData<TimePeriod>()
    val selectedTimePeriod: LiveData<TimePeriod> = _selectedTimePeriod

    private val _productQuantities = MutableLiveData<Map<Int, Int>>()
    val productQuantities: LiveData<Map<Int, Int>> = _productQuantities

    private val _saveStatus = MutableLiveData<SaveStatus>()
    val saveStatus: LiveData<SaveStatus> = _saveStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadProducts()
        loadTimePeriods()
        initializeDate()
        _productQuantities.value = mutableMapOf()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val activeProducts = productRepository.getActiveProducts().first()
                _products.value = activeProducts
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadTimePeriods() {
        viewModelScope.launch {
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
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun initializeDate() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        _selectedDate.value = dateFormat.format(Calendar.getInstance().time)
    }

    fun setSelectedDate(date: Date) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        _selectedDate.value = dateFormat.format(date)
    }

    fun setSelectedTimePeriod(timePeriod: TimePeriod) {
        _selectedTimePeriod.value = timePeriod
    }

    fun updateProductQuantity(productId: Int, quantity: Int) {
        val currentQuantities = _productQuantities.value?.toMutableMap() ?: mutableMapOf()
        currentQuantities[productId] = quantity
        _productQuantities.value = currentQuantities
    }

    fun copyFromPreviousPeriod() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val date = _selectedDate.value ?: return@launch
                val currentTimePeriod = _selectedTimePeriod.value ?: return@launch
                val timePeriods = _timePeriods.value ?: return@launch
                
                // Find the previous time period
                val currentIndex = timePeriods.indexOf(currentTimePeriod)
                if (currentIndex <= 0) {
                    // This is the first period, nothing to copy from
                    return@launch
                }
                
                val previousTimePeriod = timePeriods[currentIndex - 1]
                
                // Get the sales entry for the previous period
                val previousSalesEntry = salesRepository.getSalesEntryByDateAndTimePeriod(
                    date, previousTimePeriod.id
                ).first()
                
                if (previousSalesEntry != null) {
                    // Get the sales details for the previous entry
                    val previousSalesDetails = salesRepository.getSalesDetailsBySalesEntryId(
                        previousSalesEntry.id
                    ).first()
                    
                    // Copy the quantities
                    val newQuantities = mutableMapOf<Int, Int>()
                    previousSalesDetails.forEach { detail ->
                        newQuantities[detail.productId] = detail.quantity
                    }
                    
                    _productQuantities.value = newQuantities
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSalesEntry() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val date = _selectedDate.value ?: return@launch
                val timePeriodId = _selectedTimePeriod.value?.id ?: return@launch
                val quantities = _productQuantities.value ?: return@launch
                
                // Check if there's already an entry for this date and time period
                val existingEntry = salesRepository.getSalesEntryByDateAndTimePeriod(
                    date, timePeriodId
                ).first()
                
                val entryId = if (existingEntry != null) {
                    // Update existing entry
                    existingEntry.id
                } else {
                    // Create new entry
                    val newEntry = SalesEntry(
                        id = 0, // Room will generate the ID
                        date = date,
                        timePeriodId = timePeriodId
                    )
                    salesRepository.insertSalesEntry(newEntry).toInt()
                }
                
                // Delete existing details if any
                if (existingEntry != null) {
                    salesRepository.deleteSalesDetailsBySalesEntryId(existingEntry.id)
                }
                
                // Insert new details
                val salesDetails = quantities.map { (productId, quantity) ->
                    SalesDetail(
                        id = 0, // Room will generate the ID
                        salesEntryId = entryId,
                        productId = productId,
                        quantity = quantity
                    )
                }
                
                salesRepository.insertSalesDetails(salesDetails)
                
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
    }
}