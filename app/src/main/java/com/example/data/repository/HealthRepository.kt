package com.example.data.repository

import com.example.data.local.HealthDao
import com.example.data.local.HealthEntryEntity
import com.example.data.local.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class HealthRepository(private val healthDao: HealthDao) {

    val allEntries: Flow<List<HealthEntryEntity>> = healthDao.getAllEntries()

    val settings: Flow<UserSettingsEntity?> = healthDao.getSettings()

    fun getEntriesForDate(date: String): Flow<List<HealthEntryEntity>> {
        return healthDao.getEntriesForDate(date)
    }

    suspend fun insertEntry(entry: HealthEntryEntity) {
        healthDao.insertEntry(entry)
    }

    suspend fun insertEntries(entries: List<HealthEntryEntity>) {
        healthDao.insertEntries(entries)
    }

    suspend fun updateEntry(entry: HealthEntryEntity) {
        healthDao.updateEntry(entry)
    }

    suspend fun deleteEntry(id: String) {
        healthDao.deleteEntry(id)
    }

    suspend fun clearAllData() {
        healthDao.deleteAllEntries()
    }

    suspend fun saveSettings(settings: UserSettingsEntity) {
        healthDao.insertOrUpdateSettings(settings)
    }

    suspend fun loadSampleData() {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val sampleEntries = mutableListOf<HealthEntryEntity>()

        // Today's entries
        val todayStr = today.format(formatter)
        sampleEntries.add(
            HealthEntryEntity(
                id = UUID.randomUUID().toString(),
                date = todayStr,
                timestamp = "${todayStr}T08:15:00",
                name = "5 large eggs",
                type = "intake",
                calories = 360.0,
                proteinG = 31.0,
                carbsG = 2.0,
                fatG = 25.0
            )
        )
        sampleEntries.add(
            HealthEntryEntity(
                id = UUID.randomUUID().toString(),
                date = todayStr,
                timestamp = "${todayStr}T08:20:00",
                name = "250ml toned milk",
                type = "intake",
                calories = 127.0,
                proteinG = 8.2,
                carbsG = 11.8,
                fatG = 5.0
            )
        )
        sampleEntries.add(
            HealthEntryEntity(
                id = UUID.randomUUID().toString(),
                date = todayStr,
                timestamp = "${todayStr}T13:00:00",
                name = "4 small idlis with sambar",
                type = "intake",
                calories = 195.0,
                proteinG = 5.5,
                carbsG = 38.0,
                fatG = 1.8
            )
        )
        sampleEntries.add(
            HealthEntryEntity(
                id = UUID.randomUUID().toString(),
                date = todayStr,
                timestamp = "${todayStr}T18:30:00",
                name = "5km jog (35 minutes)",
                type = "burn",
                calories = 368.0,
                proteinG = 0.0,
                carbsG = 0.0,
                fatG = 0.0
            )
        )

        // Yesterday
        val yestStr = today.minusDays(1).format(formatter)
        sampleEntries.add(
            HealthEntryEntity(
                id = UUID.randomUUID().toString(),
                date = yestStr,
                timestamp = "${yestStr}T09:00:00",
                name = "Oatmeal with almonds & banana",
                type = "intake",
                calories = 380.0,
                proteinG = 12.0,
                carbsG = 65.0,
                fatG = 8.0
            )
        )
        sampleEntries.add(
            HealthEntryEntity(
                id = UUID.randomUUID().toString(),
                date = yestStr,
                timestamp = "${yestStr}T13:30:00",
                name = "2 chapatis, dal and 100g grilled chicken",
                type = "intake",
                calories = 540.0,
                proteinG = 38.0,
                carbsG = 52.0,
                fatG = 14.0
            )
        )
        sampleEntries.add(
            HealthEntryEntity(
                id = UUID.randomUUID().toString(),
                date = yestStr,
                timestamp = "${yestStr}T17:45:00",
                name = "45 minutes brisk walk",
                type = "burn",
                calories = 210.0,
                proteinG = 0.0,
                carbsG = 0.0,
                fatG = 0.0
            )
        )
        sampleEntries.add(
            HealthEntryEntity(
                id = UUID.randomUUID().toString(),
                date = yestStr,
                timestamp = "${yestStr}T20:30:00",
                name = "Steamed veggies with paneer",
                type = "intake",
                calories = 320.0,
                proteinG = 18.0,
                carbsG = 16.0,
                fatG = 20.0
            )
        )

        // 2 days ago
        val twoDaysAgoStr = today.minusDays(2).format(formatter)
        sampleEntries.add(
            HealthEntryEntity(
                id = UUID.randomUUID().toString(),
                date = twoDaysAgoStr,
                timestamp = "${twoDaysAgoStr}T08:30:00",
                name = "3 scrambled eggs & whole wheat toast",
                type = "intake",
                calories = 340.0,
                proteinG = 22.0,
                carbsG = 26.0,
                fatG = 16.0
            )
        )
        sampleEntries.add(
            HealthEntryEntity(
                id = UUID.randomUUID().toString(),
                date = twoDaysAgoStr,
                timestamp = "${twoDaysAgoStr}T14:00:00",
                name = "Chicken biryani bowl",
                type = "intake",
                calories = 680.0,
                proteinG = 34.0,
                carbsG = 82.0,
                fatG = 22.0
            )
        )
        sampleEntries.add(
            HealthEntryEntity(
                id = UUID.randomUUID().toString(),
                date = twoDaysAgoStr,
                timestamp = "${twoDaysAgoStr}T19:00:00",
                name = "30 minutes cycling",
                type = "burn",
                calories = 260.0,
                proteinG = 0.0,
                carbsG = 0.0,
                fatG = 0.0
            )
        )

        healthDao.insertEntries(sampleEntries)
    }
}
