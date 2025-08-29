package com.yourcompany.rollergrilltracker.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "settings",
    indices = [Index(value = ["key"], unique = true)]
)
data class Setting(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val key: String,
    val value: String?,
    val updatedAt: LocalDateTime = LocalDateTime.now()
)