package com.egamerica.rollergrilltracker.data.repositories

import android.util.Log
import com.egamerica.rollergrilltracker.data.dao.SettingDao
import com.egamerica.rollergrilltracker.data.entities.Setting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingDao: SettingDao
) {
    private val TAG = "SettingsRepository"
    
    companion object {
        // Setting keys
        const val KEY_STORE_NAME = "store_name"
        const val KEY_MANAGER_NAME = "manager_name"
        const val KEY_CONTACT_INFO = "contact_info"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_SHOW_SUGGESTIONS = "show_suggestions"
        const val KEY_ENABLE_WASTE_ALERTS = "enable_waste_alerts"
        const val KEY_AUTO_BACKUP = "auto_backup"
    }
    
    fun getAllSettings(): Flow<List<Setting>> {
        return settingDao.getAllSettings()
            .catch { e ->
                Log.e(TAG, "Error getting all settings: ${e.message}", e)
                throw RepositoryException("Failed to get settings", e)
            }
    }
    
    suspend fun getSettingByKey(key: String): Setting? {
        return try {
            settingDao.getSettingByKey(key)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting setting by key: ${e.message}", e)
            throw RepositoryException("Failed to get setting by key", e)
        }
    }
    
    suspend fun saveSetting(key: String, value: String?) {
        try {
            val existingSetting = settingDao.getSettingByKey(key)
            if (existingSetting != null) {
                settingDao.update(existingSetting.copy(value = value, updatedAt = LocalDateTime.now()))
            } else {
                settingDao.insert(Setting(key = key, value = value))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving setting: ${e.message}", e)
            throw RepositoryException("Failed to save setting", e)
        }
    }
    
    suspend fun getStringSetting(key: String, defaultValue: String = ""): String {
        return try {
            val setting = settingDao.getSettingByKey(key)
            setting?.value ?: defaultValue
        } catch (e: Exception) {
            Log.e(TAG, "Error getting string setting: ${e.message}", e)
            defaultValue
        }
    }
    
    suspend fun getBooleanSetting(key: String, defaultValue: Boolean = false): Boolean {
        return try {
            val setting = settingDao.getSettingByKey(key)
            setting?.value?.toBoolean() ?: defaultValue
        } catch (e: Exception) {
            Log.e(TAG, "Error getting boolean setting: ${e.message}", e)
            defaultValue
        }
    }
    
    suspend fun getIntSetting(key: String, defaultValue: Int = 0): Int {
        return try {
            val setting = settingDao.getSettingByKey(key)
            setting?.value?.toIntOrNull() ?: defaultValue
        } catch (e: Exception) {
            Log.e(TAG, "Error getting int setting: ${e.message}", e)
            defaultValue
        }
    }
    
    suspend fun saveBooleanSetting(key: String, value: Boolean) {
        saveSetting(key, value.toString())
    }
    
    suspend fun saveIntSetting(key: String, value: Int) {
        saveSetting(key, value.toString())
    }
    
    suspend fun deleteSetting(key: String) {
        try {
            settingDao.deleteByKey(key)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting setting: ${e.message}", e)
            throw RepositoryException("Failed to delete setting", e)
        }
    }
}