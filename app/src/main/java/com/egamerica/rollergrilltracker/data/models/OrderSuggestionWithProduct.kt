package com.egamerica.rollergrilltracker.data.models

import java.time.LocalDate
import java.time.LocalDateTime

data class OrderSuggestionWithProduct(
    val id: Int,
    val date: LocalDate,
    val productId: Int,
    val suggestedCases: Int,
    val suggestedUnits: Int,
    val calculatedAt: LocalDateTime,
    val productName: String,
    val category: String,
    val unitsPerCase: Int
)