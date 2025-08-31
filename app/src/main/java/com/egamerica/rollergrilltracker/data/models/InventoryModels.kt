package com.egamerica.rollergrilltracker.data.models

data class InventoryReportItem(
    val productId: Int,
    val productName: String,
    val totalUsed: Int,
    val averageUsed: Float
)