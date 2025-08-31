package com.egamerica.rollergrilltracker.data.models

data class ProductWasteSummary(
    val productId: Int,
    val name: String,
    val category: String,
    val totalQuantity: Int
)

data class ProductWasteByTimePeriod(
    val productId: Int,
    val name: String,
    val category: String,
    val totalQuantity: Int,
    val timePeriodId: Int
)