package com.egamerica.rollergrilltracker.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "slot_assignments",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["grillNumber", "slotNumber"], unique = true)
    ]
)
data class SlotAssignment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val grillNumber: Int, // Added to support multiple grills
    val slotNumber: Int, // Slot number within the grill
    val productId: Int?,
    val maxCapacity: Int,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val productAddedAt: LocalDateTime? = null // Added to track 4-hour hold time
)