package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HealthDatabase
import com.example.data.local.HealthEntryEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.model.DailySummary
import com.example.data.model.HealthCalculators
import com.example.data.model.MonthlySummary
import com.example.data.remote.GeminiService
import com.example.data.remote.ParseResult
import com.example.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class NavigationTab(val title: String) {
    TODAY("Today"),
    HISTORY("History"),
    CALENDAR("Calendar"),
    INSIGHTS("Insights"),
    SETTINGS("Settings")
}

class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val database = HealthDatabase.getDatabase(application)
    private val repository = HealthRepository(database.healthDao())
    private val geminiService = GeminiService()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    val todayDateString: String = LocalDate.now().format(dateFormatter)

    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _activeTab = MutableStateFlow(NavigationTab.TODAY)
    val activeTab: StateFlow<NavigationTab> = _activeTab.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Observes all entries reactively from Room
    val allEntries: StateFlow<List<HealthEntryEntity>> = repository.allEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Observes user settings
    val userSettings: StateFlow<UserSettingsEntity> = repository.settings
        .combine(_selectedDate) { settings, _ ->
            settings ?: UserSettingsEntity()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettingsEntity())

    // Daily summary for today
    val todaySummary: StateFlow<DailySummary> = allEntries
        .combine(_selectedDate) { entries, _ ->
            val todayItems = entries.filter { it.date == todayDateString }
            HealthCalculators.calculateDailySummary(todayDateString, todayItems)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            HealthCalculators.calculateDailySummary(todayDateString, emptyList())
        )

    // Daily summary for the currently selected date
    val selectedDateSummary: StateFlow<DailySummary> = combine(allEntries, _selectedDate) { entries, date ->
        val dayItems = entries.filter { it.date == date }
        HealthCalculators.calculateDailySummary(date, dayItems)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HealthCalculators.calculateDailySummary(todayDateString, emptyList())
    )

    // Monthly summary for current month
    val currentMonthKey: String = todayDateString.substring(0, 7) // "yyyy-MM"
    val monthlySummary: StateFlow<MonthlySummary> = allEntries
        .combine(_selectedDate) { entries, _ ->
            HealthCalculators.calculateMonthlySummary(currentMonthKey, entries)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            HealthCalculators.calculateMonthlySummary(currentMonthKey, emptyList())
        )

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun setActiveTab(tab: NavigationTab) {
        _activeTab.value = tab
    }

    fun submitNaturalLanguageLog(text: String) {
        if (text.isBlank() || _isAnalyzing.value) return

        viewModelScope.launch {
            _isAnalyzing.value = true
            val currentSettings = userSettings.value
            val result = geminiService.parseHealthInput(
                text = text,
                userWeightKg = currentSettings.weightKg
            )
            _isAnalyzing.value = false

            when (result) {
                is ParseResult.Success -> {
                    val now = LocalDateTime.now()
                    val dateStr = now.format(dateFormatter)
                    val timestampStr = now.format(timeFormatter)

                    val newEntities = result.items.map { item ->
                        HealthEntryEntity(
                            id = UUID.randomUUID().toString(),
                            date = dateStr,
                            timestamp = timestampStr,
                            name = item.name,
                            type = item.type,
                            calories = item.calories,
                            proteinG = item.proteinG,
                            carbsG = item.carbsG,
                            fatG = item.fatG
                        )
                    }

                    repository.insertEntries(newEntities)
                    // Reset selected date to today to show newly added items
                    _selectedDate.value = dateStr
                    val count = newEntities.size
                    val itemWord = if (count == 1) "1 item" else "$count items"
                    _userMessage.emit("Added $itemWord")
                }
                is ParseResult.Empty -> {
                    _userMessage.emit(result.message)
                }
                is ParseResult.Error -> {
                    _userMessage.emit(result.userFriendlyMessage)
                }
            }
        }
    }

    fun updateEntry(entry: HealthEntryEntity) {
        viewModelScope.launch {
            repository.updateEntry(entry)
            _userMessage.emit("Entry updated")
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            repository.deleteEntry(id)
            _userMessage.emit("Entry deleted")
        }
    }

    fun saveSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.saveSettings(settings)
            _userMessage.emit("Settings saved")
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            repository.loadSampleData()
            _userMessage.emit("Sample health data loaded")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _userMessage.emit("All health data has been reset")
        }
    }
}
