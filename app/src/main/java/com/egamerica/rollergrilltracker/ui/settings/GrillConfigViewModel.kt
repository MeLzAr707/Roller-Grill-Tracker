package com.egamerica.rollergrilltracker.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egamerica.rollergrilltracker.data.entities.GrillConfig
import com.egamerica.rollergrilltracker.data.models.SlotWithProduct
import com.egamerica.rollergrilltracker.data.repositories.GrillConfigRepository
import com.egamerica.rollergrilltracker.data.repositories.SlotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GrillConfigViewModel @Inject constructor(
    private val grillConfigRepository: GrillConfigRepository,
    private val slotRepository: SlotRepository
) : ViewModel() {

    private val _grillConfigs = MutableLiveData<List<GrillConfig>>()
    val grillConfigs: LiveData<List<GrillConfig>> = _grillConfigs

    private val _activeGrillConfigs = MutableLiveData<List<GrillConfig>>()
    val activeGrillConfigs: LiveData<List<GrillConfig>> = _activeGrillConfigs

    private val _selectedGrillConfig = MutableLiveData<GrillConfig>()
    val selectedGrillConfig: LiveData<GrillConfig> = _selectedGrillConfig

    private val _slotsWithProducts = MutableLiveData<Map<Int, List<SlotWithProduct>>>()
    val slotsWithProducts: LiveData<Map<Int, List<SlotWithProduct>>> = _slotsWithProducts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _saveStatus = MutableLiveData<SaveStatus>()
    val saveStatus: LiveData<SaveStatus> = _saveStatus

    init {
        loadGrillConfigs()
        loadSlotsWithProducts()
    }

    private fun loadGrillConfigs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allGrills = grillConfigRepository.getAllGrillConfigs().first()
                _grillConfigs.value = allGrills
                
                val activeGrills = grillConfigRepository.getActiveGrillConfigs().first()
                _activeGrillConfigs.value = activeGrills
                
                // Set the first active grill as selected if none is selected
                if (_selectedGrillConfig.value == null && activeGrills.isNotEmpty()) {
                    _selectedGrillConfig.value = activeGrills.first()
                }
            } catch (e: Exception) {
                // Handle error
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadSlotsWithProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allSlots = slotRepository.getAllSlotsWithProducts().first()
                
                // Group slots by grill number
                val slotsByGrill = allSlots.groupBy { it.slot.grillNumber }
                _slotsWithProducts.value = slotsByGrill
            } catch (e: Exception) {
                // Handle error
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectGrillConfig(grillConfig: GrillConfig) {
        _selectedGrillConfig.value = grillConfig
    }

    fun addGrill(grillName: String, numberOfSlots: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                grillConfigRepository.addGrill(grillName, numberOfSlots)
                
                // Reload grill configs
                loadGrillConfigs()
                
                // Reload slots
                loadSlotsWithProducts()
                
                _saveStatus.value = SaveStatus.SUCCESS
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateGrill(grillConfig: GrillConfig) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                grillConfigRepository.updateGrill(grillConfig)
                
                // Reload grill configs
                loadGrillConfigs()
                
                _saveStatus.value = SaveStatus.SUCCESS
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteGrill(grillNumber: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                grillConfigRepository.deleteGrill(grillNumber)
                
                // Reload grill configs
                loadGrillConfigs()
                
                // Reload slots
                loadSlotsWithProducts()
                
                // Clear selected grill if it was deleted
                if (_selectedGrillConfig.value?.grillNumber == grillNumber) {
                    _selectedGrillConfig.value = _activeGrillConfigs.value?.firstOrNull()
                }
                
                _saveStatus.value = SaveStatus.SUCCESS
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateGrillActiveStatus(grillNumber: Int, isActive: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                grillConfigRepository.updateGrillActiveStatus(grillNumber, isActive)
                
                // Reload grill configs
                loadGrillConfigs()
                
                _saveStatus.value = SaveStatus.SUCCESS
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateGrillSlotCount(grillNumber: Int, numberOfSlots: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                grillConfigRepository.updateGrillSlotCount(grillNumber, numberOfSlots)
                
                // Reload grill configs
                loadGrillConfigs()
                
                // Reload slots
                loadSlotsWithProducts()
                
                _saveStatus.value = SaveStatus.SUCCESS
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun initializeDefaultGrill() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                grillConfigRepository.initializeDefaultGrill()
                
                // Reload grill configs
                loadGrillConfigs()
                
                // Reload slots
                loadSlotsWithProducts()
                
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