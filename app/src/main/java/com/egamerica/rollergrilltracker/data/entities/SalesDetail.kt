package com.egamerica.rollergrilltracker.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "sales_details",
    foreignKeys = [
        ForeignKey(
            entity = SalesEntry::class,
            parentColumns = ["id"],
            childColumns = ["salesEntryId"],
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
        Index(value = ["salesEntryId"]),
        Index(value = ["productId"]),
        Index(value = ["salesEntryId", "productId"], unique = true)
    ]
)
data class SalesDetail(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val salesEntryId: Int,
    val productId: Int,
    val quantity: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)