package com.egamerica.rollergrilltracker.data.repositories

import android.util.Log
import androidx.room.Transaction
import com.egamerica.rollergrilltracker.data.models.ProductWasteByTimePeriod
import com.egamerica.rollergrilltracker.data.models.ProductWasteSummary
import com.egamerica.rollergrilltracker.data.dao.WasteDetailDao
import com.egamerica.rollergrilltracker.data.dao.WasteEntryDao
import com.egamerica.rollergrilltracker.data.entities.WasteDetail
import com.egamerica.rollergrilltracker.data.entities.WasteEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WasteRepository @Inject constructor(
    private val wasteEntryDao: WasteEntryDao,
    private val wasteDetailDao: WasteDetailDao
) {
    private val TAG = "WasteRepository"
    
    fun getAllWasteEntries(): Flow<List<WasteEntry>> {
        return wasteEntryDao.getAllWasteEntries()
            .catch { e ->
                Log.e(TAG, "Error getting all waste entries: ${e.message}", e)
                throw RepositoryException("Failed to get waste entries", e)
            }
    }
    
    fun getWasteEntriesByDate(date: LocalDate): Flow<List<WasteEntry>> {
        return wasteEntryDao.getWasteEntriesByDate(date)
            .catch { e ->
                Log.e(TAG, "Error getting waste entries by date: ${e.message}", e)
                throw RepositoryException("Failed to get waste entries by date", e)
            }
    }
    
    fun getWasteEntriesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WasteEntry>> {
        return wasteEntryDao.getWasteEntriesByDateRange(startDate, endDate)
            .catch { e ->
                Log.e(TAG, "Error getting waste entries by date range: ${e.message}", e)
                throw RepositoryException("Failed to get waste entries by date range", e)
            }
    }
    
    suspend fun getWasteEntryByDateAndPeriod(date: LocalDate, timePeriodId: Int): WasteEntry? {
        return try {
            wasteEntryDao.getWasteEntryByDateAndPeriod(date, timePeriodId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting waste entry by date and period: ${e.message}", e)
            throw RepositoryException("Failed to get waste entry by date and period", e)
        }
    }
    
    fun getWasteDetailsByWasteEntryId(wasteEntryId: Int): Flow<List<WasteDetail>> {
        return wasteDetailDao.getWasteDetailsByWasteEntryId(wasteEntryId)
            .catch { e ->
                Log.e(TAG, "Error getting waste details by waste entry ID: ${e.message}", e)
                throw RepositoryException("Failed to get waste details by waste entry ID", e)
            }
    }
    
    @Transaction
    suspend fun saveWasteEntry(
        date: LocalDate,
        timePeriodId: Int,
        reasonCode: Int?,
        productQuantities: Map<Int, Int>
    ): Int {
        try {
            // Create a new waste entry
            val wasteEntry = WasteEntry(
                date = date,
                timePeriodId = timePeriodId,
                reasonCode = reasonCode
            )
            val entryId = wasteEntryDao.insert(wasteEntry).toInt()
            
            // Save the details
            val wasteDetails = productQuantities.map { (productId, quantity) ->
                WasteDetail(
                    wasteEntryId = entryId,
                    productId = productId,
                    quantity = quantity
                )
            }
            
            wasteDetailDao.insertAll(wasteDetails)
            
            return entryId
        } catch (e: Exception) {
            Log.e(TAG, "Error saving waste entry: ${e.message}", e)
            throw RepositoryException("Failed to save waste entry", e)
        }
    }
    
    suspend fun getProductWasteSummary(startDate: String, endDate: String): List<ProductWasteSummary> {
        return try {
            wasteDetailDao.getProductWasteSummary(startDate, endDate)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product waste summary: ${e.message}", e)
            throw RepositoryException("Failed to get product waste summary", e)
        }
    }
    
    suspend fun getProductWasteByTimePeriod(startDate: String, endDate: String): List<ProductWasteByTimePeriod> {
        return try {
            wasteDetailDao.getProductWasteByTimePeriod(startDate, endDate)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product waste by time period: ${e.message}", e)
            throw RepositoryException("Failed to get product waste by time period", e)
        }
    }
}