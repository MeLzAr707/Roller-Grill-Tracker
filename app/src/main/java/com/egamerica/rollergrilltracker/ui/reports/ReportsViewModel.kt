package com.egamerica.rollergrilltracker.ui.reports

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egamerica.rollergrilltracker.data.entities.Product
import com.egamerica.rollergrilltracker.data.repositories.ProductRepository
import com.egamerica.rollergrilltracker.data.repositories.SalesRepository
import com.egamerica.rollergrilltracker.data.repositories.WasteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val wasteRepository: WasteRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _startDate = MutableLiveData<Date>()
    val startDate: LiveData<Date> = _startDate

    private val _endDate = MutableLiveData<Date>()
    val endDate: LiveData<Date> = _endDate

    private val _reportType = MutableLiveData(ReportType.SALES)
    val reportType: LiveData<ReportType> = _reportType

    private val _topProducts = MutableLiveData<List<ProductPerformance>>()
    val topProducts: LiveData<List<ProductPerformance>> = _topProducts

    private val _salesVsWaste = MutableLiveData<SalesVsWasteData>()
    val salesVsWaste: LiveData<SalesVsWasteData> = _salesVsWaste

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        // Set default date range to last 7 days
        val calendar = Calendar.getInstance()
        _endDate.value = calendar.time
        
        calendar.add(Calendar.DAY_OF_MONTH, -7)
        _startDate.value = calendar.time
        
        loadReportData()
    }

    fun setStartDate(date: Date) {
        _startDate.value = date
        loadReportData()
    }

    fun setEndDate(date: Date) {
        _endDate.value = date
        loadReportData()
    }

    fun setReportType(type: ReportType) {
        _reportType.value = type
        loadReportData()
    }

    private fun loadReportData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val start = _startDate.value ?: return@launch
                val end = _endDate.value ?: return@launch
                val type = _reportType.value ?: return@launch
                
                // Convert Date to LocalDate
                val startLocalDate = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                val endLocalDate = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                
                when (type) {
                    ReportType.SALES -> loadSalesReport(startLocalDate, endLocalDate)
                    ReportType.WASTE -> loadWasteReport(startLocalDate, endLocalDate)
                    ReportType.SALES_VS_WASTE -> loadSalesVsWasteReport(startLocalDate, endLocalDate)
                    ReportType.CUSTOM -> loadCustomReport(startLocalDate, endLocalDate)
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadSalesReport(startDate: LocalDate, endDate: LocalDate) {
        val salesEntries = salesRepository.getSalesEntriesByDateRange(startDate, endDate).first()
        
        val productSales = mutableMapOf<Int, Int>()
        
        for (entry in salesEntries) {
            val details = salesRepository.getSalesDetailsBySalesEntryId(entry.id).first()
            for (detail in details) {
                val currentCount = productSales[detail.productId] ?: 0
                productSales[detail.productId] = currentCount + detail.quantity
            }
        }
        
        val products = productRepository.getAllProducts().first()
        val productMap = products.associateBy { it.id }
        
        val performances = productSales.map { (productId, quantity) ->
            val product = productMap[productId]
            ProductPerformance(
                productId = productId,
                productName = product?.name ?: "Unknown",
                salesQuantity = quantity,
                wasteQuantity = 0,
                ratio = 0.0
            )
        }.sortedByDescending { it.salesQuantity }
        
        _topProducts.value = performances.take(10)
    }

    private suspend fun loadWasteReport(startDate: LocalDate, endDate: LocalDate) {
        val wasteEntries = wasteRepository.getWasteEntriesByDateRange(startDate, endDate).first()
        
        val productWaste = mutableMapOf<Int, Int>()
        
        for (entry in wasteEntries) {
            val details = wasteRepository.getWasteDetailsByWasteEntryId(entry.id).first()
            for (detail in details) {
                val currentCount = productWaste[detail.productId] ?: 0
                productWaste[detail.productId] = currentCount + detail.quantity
            }
        }
        
        val products = productRepository.getAllProducts().first()
        val productMap = products.associateBy { it.id }
        
        val performances = productWaste.map { (productId, quantity) ->
            val product = productMap[productId]
            ProductPerformance(
                productId = productId,
                productName = product?.name ?: "Unknown",
                salesQuantity = 0,
                wasteQuantity = quantity,
                ratio = 0.0
            )
        }.sortedByDescending { it.wasteQuantity }
        
        _topProducts.value = performances.take(10)
    }

    private suspend fun loadSalesVsWasteReport(startDate: LocalDate, endDate: LocalDate) {
        val salesEntries = salesRepository.getSalesEntriesByDateRange(startDate, endDate).first()
        val wasteEntries = wasteRepository.getWasteEntriesByDateRange(startDate, endDate).first()
        
        val productSales = mutableMapOf<Int, Int>()
        val productWaste = mutableMapOf<Int, Int>()
        
        // Calculate sales by product
        for (entry in salesEntries) {
            val details = salesRepository.getSalesDetailsBySalesEntryId(entry.id).first()
            for (detail in details) {
                val currentCount = productSales[detail.productId] ?: 0
                productSales[detail.productId] = currentCount + detail.quantity
            }
        }
        
        // Calculate waste by product
        for (entry in wasteEntries) {
            val details = wasteRepository.getWasteDetailsByWasteEntryId(entry.id).first()
            for (detail in details) {
                val currentCount = productWaste[detail.productId] ?: 0
                productWaste[detail.productId] = currentCount + detail.quantity
            }
        }
        
        // Get all product IDs
        val productIds = (productSales.keys + productWaste.keys).toSet()
        
        val products = productRepository.getAllProducts().first()
        val productMap = products.associateBy { it.id }
        
        val performances = productIds.map { productId ->
            val sales = productSales[productId] ?: 0
            val waste = productWaste[productId] ?: 0
            val ratio = if (sales + waste > 0) {
                sales.toDouble() / (sales + waste)
            } else {
                0.0
            }
            
            val product = productMap[productId]
            ProductPerformance(
                productId = productId,
                productName = product?.name ?: "Unknown",
                salesQuantity = sales,
                wasteQuantity = waste,
                ratio = ratio
            )
        }.sortedByDescending { it.salesQuantity + it.wasteQuantity }
        
        _topProducts.value = performances.take(10)
        
        // Calculate total sales and waste
        val totalSales = productSales.values.sum()
        val totalWaste = productWaste.values.sum()
        
        _salesVsWaste.value = SalesVsWasteData(
            totalSales = totalSales,
            totalWaste = totalWaste,
            salesPercentage = if (totalSales + totalWaste > 0) {
                totalSales.toDouble() / (totalSales + totalWaste) * 100
            } else {
                0.0
            },
            wastePercentage = if (totalSales + totalWaste > 0) {
                totalWaste.toDouble() / (totalSales + totalWaste) * 100
            } else {
                0.0
            }
        )
    }

    private suspend fun loadCustomReport(startDate: LocalDate, endDate: LocalDate) {
        // Custom report implementation
        // For now, just load sales vs waste report
        loadSalesVsWasteReport(startDate, endDate)
    }

    enum class ReportType {
        SALES,
        WASTE,
        SALES_VS_WASTE,
        CUSTOM
    }

    data class ProductPerformance(
        val productId: Int,
        val productName: String,
        val salesQuantity: Int,
        val wasteQuantity: Int,
        val ratio: Double
    )

    data class SalesVsWasteData(
        val totalSales: Int,
        val totalWaste: Int,
        val salesPercentage: Double,
        val wastePercentage: Double
    )
}