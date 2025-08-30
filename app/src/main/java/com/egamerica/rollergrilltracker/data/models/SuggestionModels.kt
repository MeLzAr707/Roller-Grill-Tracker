package com.egamerica.rollergrilltracker.data.models

import java.time.LocalDate

data class SuggestionWithProduct(
    val id: Int,
    val date: LocalDate,
    val timePeriodId: Int,
    val productId: Int,
    val suggestedQuantity: Int,
    val confidenceScore: Float,
    val name: String,
    val category: String
)