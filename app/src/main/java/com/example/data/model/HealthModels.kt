package com.example.data.model

import com.example.data.local.HealthEntryEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class DailySummary(
    val date: String,
    val formattedDate: String,
    val consumedCalories: Double,
    val burnedCalories: Double,
    val netCalories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val entriesCount: Int,
    val entries: List<HealthEntryEntity>
)

data class MonthlySummary(
    val monthKey: String, // "yyyy-MM"
    val monthTitle: String, // e.g. "September 2026"
    val totalConsumed: Double,
    val totalBurned: Double,
    val avgDailyCalories: Double,
    val avgProtein: Double,
    val avgCarbs: Double,
    val avgFat: Double,
    val daysLogged: Int,
    val mostActiveDay: String,
    val highestCalorieDay: String
)

object HealthCalculators {

    fun calculateDailySummary(date: String, entries: List<HealthEntryEntity>): DailySummary {
        var consumed = 0.0
        var burned = 0.0
        var protein = 0.0
        var carbs = 0.0
        var fat = 0.0

        for (entry in entries) {
            if (entry.type == "intake") {
                consumed += entry.calories
                protein += entry.proteinG
                carbs += entry.carbsG
                fat += entry.fatG
            } else if (entry.type == "burn") {
                burned += entry.calories
            }
        }

        val net = consumed - burned
        val formattedDate = formatDateString(date)

        return DailySummary(
            date = date,
            formattedDate = formattedDate,
            consumedCalories = consumed,
            burnedCalories = burned,
            netCalories = net,
            proteinG = protein,
            carbsG = carbs,
            fatG = fat,
            entriesCount = entries.size,
            entries = entries
        )
    }

    fun calculateMonthlySummary(monthKey: String, allEntries: List<HealthEntryEntity>): MonthlySummary {
        // monthKey format: "yyyy-MM"
        val monthEntries = allEntries.filter { it.date.startsWith(monthKey) }
        val entriesByDate = monthEntries.groupBy { it.date }
        val daysLogged = entriesByDate.size

        var totalConsumed = 0.0
        var totalBurned = 0.0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFat = 0.0

        var maxBurned = -1.0
        var mostActiveDay = "None"
        var maxIntake = -1.0
        var highestCalorieDay = "None"

        for ((date, dayEntries) in entriesByDate) {
            var dayConsumed = 0.0
            var dayBurned = 0.0
            for (item in dayEntries) {
                if (item.type == "intake") {
                    dayConsumed += item.calories
                    totalProtein += item.proteinG
                    totalCarbs += item.carbsG
                    totalFat += item.fatG
                } else if (item.type == "burn") {
                    dayBurned += item.calories
                }
            }
            totalConsumed += dayConsumed
            totalBurned += dayBurned

            if (dayBurned > maxBurned && dayBurned > 0) {
                maxBurned = dayBurned
                mostActiveDay = formatDateShort(date)
            }
            if (dayConsumed > maxIntake && dayConsumed > 0) {
                maxIntake = dayConsumed
                highestCalorieDay = formatDateShort(date)
            }
        }

        val divisor = if (daysLogged > 0) daysLogged.toDouble() else 1.0
        val avgDailyCalories = if (daysLogged > 0) totalConsumed / divisor else 0.0
        val avgProtein = if (daysLogged > 0) totalProtein / divisor else 0.0
        val avgCarbs = if (daysLogged > 0) totalCarbs / divisor else 0.0
        val avgFat = if (daysLogged > 0) totalFat / divisor else 0.0

        val monthTitle = formatMonthKey(monthKey)

        return MonthlySummary(
            monthKey = monthKey,
            monthTitle = monthTitle,
            totalConsumed = totalConsumed,
            totalBurned = totalBurned,
            avgDailyCalories = avgDailyCalories,
            avgProtein = avgProtein,
            avgCarbs = avgCarbs,
            avgFat = avgFat,
            daysLogged = daysLogged,
            mostActiveDay = mostActiveDay,
            highestCalorieDay = highestCalorieDay
        )
    }

    fun formatDateString(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr)
            val today = LocalDate.now()
            when {
                date.isEqual(today) -> "Today"
                date.isEqual(today.minusDays(1)) -> "Yesterday"
                else -> {
                    val monthName = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    "$monthName ${date.dayOfMonth}, ${date.year}"
                }
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatDateShort(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr)
            val monthName = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            "$monthName ${date.dayOfMonth}"
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatMonthKey(monthKey: String): String {
        return try {
            val parts = monthKey.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val date = LocalDate.of(year, month, 1)
            val monthName = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            "$monthName $year"
        } catch (e: Exception) {
            monthKey
        }
    }
}
