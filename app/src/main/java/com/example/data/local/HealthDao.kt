package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {

    @Query("SELECT * FROM health_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<HealthEntryEntity>>

    @Query("SELECT * FROM health_entries WHERE date = :date ORDER BY timestamp ASC")
    fun getEntriesForDate(date: String): Flow<List<HealthEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: HealthEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<HealthEntryEntity>)

    @Update
    suspend fun updateEntry(entry: HealthEntryEntity)

    @Query("DELETE FROM health_entries WHERE id = :id")
    suspend fun deleteEntry(id: String)

    @Query("DELETE FROM health_entries")
    suspend fun deleteAllEntries()

    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getSettings(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: UserSettingsEntity)
}
