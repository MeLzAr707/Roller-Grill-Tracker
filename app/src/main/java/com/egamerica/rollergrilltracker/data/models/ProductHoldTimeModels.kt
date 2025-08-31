package com.egamerica.rollergrilltracker.data.models

import androidx.room.Embedded
import androidx.room.Relation
import com.egamerica.rollergrilltracker.data.entities.Product
import com.egamerica.rollergrilltracker.data.entities.ProductHoldTime

data class ProductWithHoldTime(
    @Embedded val product: Product,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val holdTime: ProductHoldTime
)