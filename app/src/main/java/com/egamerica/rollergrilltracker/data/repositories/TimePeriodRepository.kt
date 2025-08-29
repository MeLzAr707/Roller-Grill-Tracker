package com.yourcompany.rollergrilltracker.data.repositories

import android.util.Log
import com.yourcompany.rollergrilltracker.data.dao.TimePeriodDao
import com.yourcompany.rollergrilltracker.data.entities.TimePeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimePeriodRepository @Inject constructor(
    private val timePeriodDao: TimePeriodDao
) {
    private val TAG = "TimePeriodRepository"
    
    fun getAllActiveTimePeriods(): Flow<List<TimePeriod>> {
        return timePeriodDao.getAllActiveTimePeriods()
            .catch { e ->
                Log.e(TAG, "Error getting active time periods: ${e.message}", e)
                throw RepositoryException("Failed to get active time periods", e)
            }
    }
    
    fun getAllTimePeriods(): Flow<List<TimePeriod>> {
        return timePeriodDao.getAllTimePeriods()
            .catch { e ->
                Log.e(TAG, "Error getting all time periods: ${e.message}", e)
                throw RepositoryException("Failed to get time periods", e)
            }
    }
    
    fun getRegularTimePeriods(): Flow<List<TimePeriod>> {
        return timePeriodDao.getRegularTimePeriods()
            .catch { e ->
                Log.e(TAG, "Error getting regular time periods: ${e.message}", e)
                throw RepositoryException("Failed to get regular time periods", e)
            }
    }
    
    fun get24HourOnlyTimePeriods(): Flow<List<TimePeriod>> {
        return timePeriodDao.get24HourOnlyTimePeriods()
            .catch { e ->
                Log.e(TAG, "Error getting 24-hour only time periods: ${e.message}", e)
                throw RepositoryException("Failed to get 24-hour only time periods", e)
            }
    }
    
    suspend fun getTimePeriodById(id: Int): TimePeriod? {
        return try {
            timePeriodDao.getTimePeriodById(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting time period by ID: ${e.message}", e)
            throw RepositoryException("Failed to get time period by ID", e)
        }
    }
    
    suspend fun getTimePeriodForTime(time: String, is24HourStore: Boolean): TimePeriod? {
        return try {
            if (is24HourStore) {
                timePeriodDao.getTimePeriodForTimeWithOvernight(time)
            } else {
                timePeriodDao.getTimePeriodForTime(time)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting time period for time: ${e.message}", e)
            throw RepositoryException("Failed to get time period for time", e)
        }
    }
    
    suspend fun insertTimePeriod(timePeriod: TimePeriod): Long {
        return try {
            timePeriodDao.insert(timePeriod)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting time period: ${e.message}", e)
            throw RepositoryException("Failed to insert time period", e)
        }
    }
    
    suspend fun updateTimePeriod(timePeriod: TimePeriod) {
        try {
            timePeriodDao.update(timePeriod)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating time period: ${e.message}", e)
            throw RepositoryException("Failed to update time period", e)
        }
    }
    
    suspend fun deleteTimePeriod(timePeriod: TimePeriod) {
        try {
            timePeriodDao.delete(timePeriod)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting time period: ${e.message}", e)
            throw RepositoryException("Failed to delete time period", e)
        }
    }
    
    suspend fun updateActiveStatus(id: Int, isActive: Boolean) {
        try {
            timePeriodDao.updateActiveStatus(id, isActive)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating time period active status: ${e.message}", e)
            throw RepositoryException("Failed to update time period active status", e)
        }
    }
    
    suspend fun updateTimePeriodTimes(id: Int, startTime: String, endTime: String) {
        try {
            timePeriodDao.updateTimePeriodTimes(id, startTime, endTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating time period times: ${e.message}", e)
            throw RepositoryException("Failed to update time period times", e)
        }
    }
    
    suspend fun initializeDefaultTimePeriods() {
        try {
            // Create standard time periods
            val standardTimePeriods = listOf(
                TimePeriod(
                    name = "Morning (6am-10am)",
                    startTime = "06:00",
                    endTime = "10:00",
                    displayOrder = 1,
                    isActive = true,
                    is24HourOnly = false
                ),
                TimePeriod(
                    name = "Midday (10am-2pm)",
                    startTime = "10:00",
                    endTime = "14:00",
                    displayOrder = 2,
                    isActive = true,
                    is24HourOnly = false
                ),
                TimePeriod(
                    name = "Afternoon (2pm-6pm)",
                    startTime = "14:00",
                    endTime = "18:00",
                    displayOrder = 3,
                    isActive = true,
                    is24HourOnly = false
                ),
                TimePeriod(
                    name = "Evening (6pm-10pm)",
                    startTime = "18:00",
                    endTime = "22:00",
                    displayOrder = 4,
                    isActive = true,
                    is24HourOnly = false
                )
            )
            
            // Create 24-hour only time periods
            val extendedTimePeriods = listOf(
                TimePeriod(
                    name = "Late Night (10pm-2am)",
                    startTime = "22:00",
                    endTime = "02:00",
                    displayOrder = 5,
                    isActive = true,
                    is24HourOnly = true
                ),
                TimePeriod(
                    name = "Early Morning (2am-6am)",
                    startTime = "02:00",
                    endTime = "06:00",
                    displayOrder = 6,
                    isActive = true,
                    is24HourOnly = true
                )
            )
            
            // Insert all time periods
            val allTimePeriods = standardTimePeriods + extendedTimePeriods
            timePeriodDao.insertAll(allTimePeriods)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing default time periods: ${e.message}", e)
            throw RepositoryException("Failed to initialize default time periods", e)
        }
    }
}