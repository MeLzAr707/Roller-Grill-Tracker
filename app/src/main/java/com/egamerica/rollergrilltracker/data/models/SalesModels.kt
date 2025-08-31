package com.egamerica.rollergrilltracker.data.models

data class ProductSalesSummary(
    val productId: Int,
    val name: String,
    val category: String,
    val totalQuantity: Int
)

data class ProductSalesByTimePeriod(
    val productId: Int,
    val name: String,
    val category: String,
    val totalQuantity: Int,
    val timePeriodId: Int
)