package com.egamerica.rollergrilltracker.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "products",
    indices = [Index(value = ["barcode"], unique = true)]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val barcode: String,
    val category: String,
    val active: Boolean = true,
    val inStock: Boolean = true,
    val unitsPerCase: Int = 0, // Number of units in a standard case/box
    val minStockLevel: Int = 0, // Minimum desired stock level
    val maxStockLevel: Int = 0, // Maximum desired stock level
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)