package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val userName: String = "Health Explorer",
    val dailyCalorieTarget: Int = 2000,
    val weightKg: Double = 70.0,
    val heightCm: Double = 172.0,
    val age: Int = 28,
    val gender: String = "Not specified",
    val unitSystem: String = "Metric (kg/cm)",
    val activityLevel: String = "Moderately Active",
    val isOnboardingCompleted: Boolean = false
)
