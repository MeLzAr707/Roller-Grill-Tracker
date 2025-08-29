package com.egamerica.rollergrilltracker.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "suggestions",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["date", "timePeriodId", "productId"], unique = true),
        Index(value = ["productId"])
    ]
)
data class Suggestion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: LocalDate,
    val timePeriodId: Int,
    val productId: Int,
    val suggestedQuantity: Int,
    val confidenceScore: Float,
    val createdAt: LocalDateTime = LocalDateTime.now()
)