package com.yourcompany.rollergrilltracker.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "sales_entries",
    indices = [
        Index(value = ["date"]),
        Index(value = ["timePeriodId"]),
        Index(value = ["date", "timePeriodId"], unique = true)
    ]
)
data class SalesEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: LocalDate,
    val timePeriodId: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)