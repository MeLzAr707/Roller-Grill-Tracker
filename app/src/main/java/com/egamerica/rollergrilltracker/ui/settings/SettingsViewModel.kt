package com.yourcompany.rollergrilltracker.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.rollergrilltracker.data.entities.Setting
import com.yourcompany.rollergrilltracker.data.repositories.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _storeName = MutableLiveData<String>()
    val storeName: LiveData<String> = _storeName

    private val _managerName = MutableLiveData<String>()
    val managerName: LiveData<String> = _managerName

    private val _contactInfo = MutableLiveData<String>()
    val contactInfo: LiveData<String> = _contactInfo

    private val _darkMode = MutableLiveData<Boolean>()
    val darkMode: LiveData<Boolean> = _darkMode

    private val _showSuggestions = MutableLiveData<Boolean>()
    val showSuggestions: LiveData<Boolean> = _showSuggestions

    private val _enableWasteAlerts = MutableLiveData<Boolean>()
    val enableWasteAlerts: LiveData<Boolean> = _enableWasteAlerts

    private val _autoBackup = MutableLiveData<Boolean>()
    val autoBackup: LiveData<Boolean> = _autoBackup

    private val _saveStatus = MutableLiveData<SaveStatus>()
    val saveStatus: LiveData<SaveStatus> = _saveStatus

    private val _backupStatus = MutableLiveData<BackupStatus>()
    val backupStatus: LiveData<BackupStatus> = _backupStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val settings = settingsRepository.getAllSettings().first()
                
                // Process settings
                for (setting in settings) {
                    when (setting.key) {
                        "store_name" -> _storeName.value = setting.value
                        "manager_name" -> _managerName.value = setting.value
                        "contact_info" -> _contactInfo.value = setting.value
                        "dark_mode" -> _darkMode.value = setting.value.toBoolean()
                        "show_suggestions" -> _showSuggestions.value = setting.value.toBoolean()
                        "enable_waste_alerts" -> _enableWasteAlerts.value = setting.value.toBoolean()
                        "auto_backup" -> _autoBackup.value = setting.value.toBoolean()
                    }
                }
                
                // Set defaults for missing settings
                if (_storeName.value == null) _storeName.value = ""
                if (_managerName.value == null) _managerName.value = ""
                if (_contactInfo.value == null) _contactInfo.value = ""
                if (_darkMode.value == null) _darkMode.value = false
                if (_showSuggestions.value == null) _showSuggestions.value = true
                if (_enableWasteAlerts.value == null) _enableWasteAlerts.value = true
                if (_autoBackup.value == null) _autoBackup.value = false
                
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setStoreName(name: String) {
        _storeName.value = name
    }

    fun setManagerName(name: String) {
        _managerName.value = name
    }

    fun setContactInfo(contact: String) {
        _contactInfo.value = contact
    }

    fun setDarkMode(enabled: Boolean) {
        _darkMode.value = enabled
    }

    fun setShowSuggestions(enabled: Boolean) {
        _showSuggestions.value = enabled
    }

    fun setEnableWasteAlerts(enabled: Boolean) {
        _enableWasteAlerts.value = enabled
    }

    fun setAutoBackup(enabled: Boolean) {
        _autoBackup.value = enabled
    }

    fun saveSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val settings = listOf(
                    Setting(key = "store_name", value = _storeName.value ?: ""),
                    Setting(key = "manager_name", value = _managerName.value ?: ""),
                    Setting(key = "contact_info", value = _contactInfo.value ?: ""),
                    Setting(key = "dark_mode", value = (_darkMode.value ?: false).toString()),
                    Setting(key = "show_suggestions", value = (_showSuggestions.value ?: true).toString()),
                    Setting(key = "enable_waste_alerts", value = (_enableWasteAlerts.value ?: true).toString()),
                    Setting(key = "auto_backup", value = (_autoBackup.value ?: false).toString())
                )
                
                settingsRepository.updateSettings(settings)
                _saveStatus.value = SaveStatus.SUCCESS
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun backupData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Backup implementation would go here
                // For now, just simulate success
                _backupStatus.value = BackupStatus.SUCCESS
            } catch (e: Exception) {
                _backupStatus.value = BackupStatus.ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun restoreData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Restore implementation would go here
                // For now, just simulate success
                _backupStatus.value = BackupStatus.RESTORE_SUCCESS
            } catch (e: Exception) {
                _backupStatus.value = BackupStatus.RESTORE_ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun exportReports() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Export implementation would go here
                // For now, just simulate success
                _backupStatus.value = BackupStatus.EXPORT_SUCCESS
            } catch (e: Exception) {
                _backupStatus.value = BackupStatus.EXPORT_ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Clear data implementation would go here
                // For now, just simulate success
                _backupStatus.value = BackupStatus.CLEAR_SUCCESS
            } catch (e: Exception) {
                _backupStatus.value = BackupStatus.CLEAR_ERROR
            } finally {
                _isLoading.value = false
            }
        }
    }

    sealed class SaveStatus {
        object SUCCESS : SaveStatus()
        object ERROR : SaveStatus()
    }

    sealed class BackupStatus {
        object SUCCESS : BackupStatus()
        object ERROR : BackupStatus()
        object RESTORE_SUCCESS : BackupStatus()
        object RESTORE_ERROR : BackupStatus()
        object EXPORT_SUCCESS : BackupStatus()
        object EXPORT_ERROR : BackupStatus()
        object CLEAR_SUCCESS : BackupStatus()
        object CLEAR_ERROR : BackupStatus()
    }
}