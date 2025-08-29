package com.egamerica.rollergrilltracker.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "order_suggestions",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["date", "productId"], unique = true)]
)
data class OrderSuggestion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: LocalDate, // The date for which this order is suggested
    val productId: Int,
    val suggestedCases: Int, // Suggested number of cases to order
    val suggestedUnits: Int, // Suggested number of individual units
    val calculatedAt: LocalDateTime = LocalDateTime.now()
)