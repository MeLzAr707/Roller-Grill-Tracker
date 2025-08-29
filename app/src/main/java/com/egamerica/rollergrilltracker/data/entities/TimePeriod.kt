package com.yourcompany.rollergrilltracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_periods")
data class TimePeriod(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val startTime: String,
    val endTime: String,
    val displayOrder: Int,
    val isActive: Boolean = true, // Added to enable/disable time periods
    val is24HourOnly: Boolean = false // Added to mark periods only for 24-hour stores
)