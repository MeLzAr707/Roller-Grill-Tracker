package com.egamerica.rollergrilltracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "grill_config")
data class GrillConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val grillNumber: Int,
    val grillName: String, // E.g., "Front Grill", "Back Grill"
    val numberOfSlots: Int, // Typically 4 slots per grill
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)