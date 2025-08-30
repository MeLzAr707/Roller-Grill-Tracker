package com.egamerica.rollergrilltracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "order_settings")
data class OrderSettings @JvmOverloads constructor(
    @PrimaryKey
    val id: Int = 1, // Single row for app-wide settings
    val orderFrequency: Int = 2, // Default: 2 orders per week
    val orderDays: String = "1,4", // Default: Monday and Thursday (stored as comma-separated day numbers, 1=Monday)
    val leadTimeDays: Int = 1, // Days between order placement and delivery
    val updatedAt: LocalDateTime = LocalDateTime.now()
)