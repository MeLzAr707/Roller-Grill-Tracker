package com.yourcompany.rollergrilltracker.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.rollergrilltracker.data.entities.StoreHours
import com.yourcompany.rollergrilltracker.data.entities.TimePeriod
import com.yourcompany.rollergrilltracker.data.repositories.StoreHoursRepository
import com.yourcompany.rollergrilltracker.data.repositories.TimePeriodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class StoreHoursViewModel @Inject constructor(
    private val storeHoursRepository: StoreHoursRepository,
    private val timePeriodRepository: TimePeriodRepository
) : ViewModel() {

    private val _storeHours = MutableLiveData<List<StoreHours>>()
    val storeHours: LiveData<List<StoreHours>> = _storeHours

    private val _timePeriods = MutableLiveData<List<TimePeriod>>()
    val timePeriods: LiveData<List<TimePeriod>> = _timePeriods

    private val _regularTimePeriods = MutableLiveData<List<TimePeriod>>()
    val regularTimePeriods: LiveData<List<TimePeriod>> = _regularTimePeriods

    private val _extendedTimePeriods = MutableLiveData<List<TimePeriod>>()
    val extendedTimePeriods: LiveData<List<TimePeriod>> = _extendedTimePeriods

    private val _has24HourOperation = MutableLiveData<Boolean>()
    val has24HourOperation: LiveData<Boolean> = _has24HourOperation

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _saveStatus = MutableLiveData<SaveStatus>()
    val saveStatus: LiveData<SaveStatus> = _saveStatus

    init {
        loadStoreHours()
        loadTimePeriods()
        check24HourOperation()
    }

    private fun loadStoreHours() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val hours = storeHoursRepository.getAllStoreHours().first()
                _storeHours.value = hours
            } catch (e: Exception) {
                // Handle error
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadTimePeriods() {
        viewModelScope.launch {
            try {
                val allPeriods = timePeriodRepository.getAllTimePeriods().first()
                _timePeriods.value = allPeriods
                
                val regularPeriods = allPeriods.filter { !it.is24HourOnly }
                _regularTimePeriods.value = regularPeriods
                
                val extendedPeriods = allPeriods.filter { it.is24HourOnly }
                _extendedTimePeriods.value = extendedPeriods
            } catch (e: Exception) {
                // Handle error
                _saveStatus.value = SaveStatus.ERROR
            }
        }
    }

    private fun check24HourOperation() {
        viewModelScope.launch {
            try {
                val has24Hour = storeHoursRepository.hasAny24HourDays()
                _has24HourOperation.value = has24Hour
            } catch (e: Exception) {
                // Handle error
                _saveStatus.value = SaveStatus.ERROR
            }
        }
    }

    fun updateStoreHours(dayOfWeek: DayOfWeek, openTime: String, closeTime: String, is24Hours: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val storeHours = StoreHours(
                    dayOfWeek = dayOfWeek.value,
                    openTime = openTime,
                    closeTime = closeTime,
                    is24Hours = is24Hours
                )
                storeHoursRepository.saveStoreHours(storeHours)
                
                // Reload store hours
                loadStoreHours()
                
                // Check if we need to update 24-hour operation status
                check24HourOperation()
                
                _saveStatus.value = SaveStatus.SUCCESS
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun update24HourStatus(dayOfWeek: Int, is24Hours: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Convert int to DayOfWeek
                val day = DayOfWeek.of(dayOfWeek)
                storeHoursRepository.update24HourStatus(day, is24Hours)
                
                // Reload store hours
                loadStoreHours()
                
                // Check if we need to update 24-hour operation status
                check24HourOperation()
                
                _saveStatus.value = SaveStatus.SUCCESS
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTimePeriodActiveStatus(timePeriodId: Int, isActive: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                timePeriodRepository.updateActiveStatus(timePeriodId, isActive)
                
                // Reload time periods
                loadTimePeriods()
                
                _saveStatus.value = SaveStatus.SUCCESS
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTimePeriodTimes(timePeriodId: Int, startTime: String, endTime: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                timePeriodRepository.updateTimePeriodTimes(timePeriodId, startTime, endTime)
                
                // Reload time periods
                loadTimePeriods()
                
                _saveStatus.value = SaveStatus.SUCCESS
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun initializeDefaultStoreHours() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                storeHoursRepository.initializeDefaultStoreHours()
                
                // Reload store hours
                loadStoreHours()
                
                _saveStatus.value = SaveStatus.SUCCESS
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun initializeDefaultTimePeriods() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                timePeriodRepository.initializeDefaultTimePeriods()
                
                // Reload time periods
                loadTimePeriods()
                
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