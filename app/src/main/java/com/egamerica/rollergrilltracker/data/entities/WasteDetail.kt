package com.egamerica.rollergrilltracker.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "waste_details",
    foreignKeys = [
        ForeignKey(
            entity = WasteEntry::class,
            parentColumns = ["id"],
            childColumns = ["wasteEntryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["wasteEntryId"]),
        Index(value = ["productId"])
    ]
)
data class WasteDetail(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val wasteEntryId: Int,
    val productId: Int,
    val quantity: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)