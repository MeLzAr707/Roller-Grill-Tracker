package com.egamerica.rollergrilltracker.data.models

import androidx.room.Embedded
import androidx.room.Relation
import com.egamerica.rollergrilltracker.data.entities.Product
import com.egamerica.rollergrilltracker.data.entities.SlotAssignment

data class SlotWithProduct(
    @Embedded val slot: SlotAssignment,
    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val product: Product?
)