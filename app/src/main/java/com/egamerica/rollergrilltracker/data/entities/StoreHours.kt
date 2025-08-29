package com.yourcompany.rollergrilltracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDateTime

@Entity(tableName = "store_hours")
data class StoreHours(
    @PrimaryKey
    val dayOfWeek: Int, // 1-7 for Monday-Sunday
    val openTime: String, // 24-hour format, e.g., "06:00"
    val closeTime: String, // 24-hour format, e.g., "22:00"
    val is24Hours: Boolean = false,
    val updatedAt: LocalDateTime = LocalDateTime.now()
)