package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "health_entries")
data class HealthEntryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: String, // format: "yyyy-MM-dd"
    val timestamp: String, // format: "yyyy-MM-dd'T'HH:mm:ss"
    val name: String,
    val type: String, // "intake" or "burn"
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double
)
