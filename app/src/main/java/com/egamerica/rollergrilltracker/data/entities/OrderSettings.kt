package com.egamerica.rollergrilltracker.data.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "order_settings")
data class OrderSettings(
    @PrimaryKey
    val id: Int,
    val orderFrequency: Int,
    val orderDays: String,
    val leadTimeDays: Int,
    val updatedAt: LocalDateTime
) {
    @Ignore
    constructor() : this(
        id = 1, // Single row for app-wide settings
        orderFrequency = 2, // Default: 2 orders per week
        orderDays = "1,4", // Default: Monday and Thursday (stored as comma-separated day numbers, 1=Monday)
        leadTimeDays = 1, // Days between order placement and delivery
        updatedAt = LocalDateTime.now()
    )
}