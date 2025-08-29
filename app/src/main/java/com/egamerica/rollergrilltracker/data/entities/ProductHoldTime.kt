package com.yourcompany.rollergrilltracker.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "product_hold_times",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SlotAssignment::class,
            parentColumns = ["id"],
            childColumns = ["slotAssignmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["slotAssignmentId"])
    ]
)
data class ProductHoldTime(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val slotAssignmentId: Int,
    val grillNumber: Int,
    val slotNumber: Int,
    val startTime: LocalDateTime,
    val expirationTime: LocalDateTime, // startTime + 4 hours
    val isActive: Boolean = true,
    val wasDiscarded: Boolean = false,
    val discardedAt: LocalDateTime? = null,
    val discardReason: String? = null
)