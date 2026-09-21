package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HealthEntryEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.model.HealthCalculators
import com.example.ui.components.CalorieSummaryCards
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EditEntryDialog
import com.example.ui.components.HealthEntryCard
import com.example.ui.components.MacroNutrientRow
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    allEntries: List<HealthEntryEntity>,
    userSettings: UserSettingsEntity,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onEditEntry: (HealthEntryEntity) -> Unit,
    onDeleteEntry: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val selectedLocalDate = remember(selectedDate) {
        try { LocalDate.parse(selectedDate) } catch (e: Exception) { LocalDate.now() }
    }

    var currentYearMonth by remember { mutableStateOf(YearMonth.from(selectedLocalDate)) }

    val daysInMonth = currentYearMonth.lengthOfMonth()
    val firstDayOfWeek = currentYearMonth.atDay(1).dayOfWeek.value % 7 // Sunday = 0, Monday = 1...

    // Precalculate day activity lookup (date -> count of items)
    val entriesByDate = remember(allEntries) {
        allEntries.groupBy { it.date }
    }

    val selectedDayEntries = remember(selectedDate, allEntries) {
        allEntries.filter { it.date == selectedDate }
    }

    val selectedDaySummary = remember(selectedDate, selectedDayEntries) {
        HealthCalculators.calculateDailySummary(selectedDate, selectedDayEntries)
    }

    var editingEntry by remember { mutableStateOf<HealthEntryEntity?>(null) }
    var entryToDeleteId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Monthly Overview",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Calendar",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(EmeraldAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 2. Calendar Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous month",
                                tint = TextSecondary
                            )
                        }

                        val monthName = currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                        Text(
                            text = "$monthName ${currentYearMonth.year}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        IconButton(
                            onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next month",
                                tint = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Day of week labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayLabel ->
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Days Grid (6 rows x 7 cols)
                    val totalCells = 42
                    val cellDays = remember(currentYearMonth) {
                        val list = mutableListOf<LocalDate?>()
                        for (i in 0 until firstDayOfWeek) {
                            list.add(null)
                        }
                        for (day in 1..daysInMonth) {
                            list.add(currentYearMonth.atDay(day))
                        }
                        while (list.size < 35 || (list.size % 7 != 0 && list.size < totalCells)) {
                            list.add(null)
                        }
                        list
                    }

                    val rows = cellDays.chunked(7)
                    rows.forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            week.forEach { date ->
                                if (date == null) {
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                } else {
                                    val dateStr = date.format(dateFormatter)
                                    val isSelected = dateStr == selectedDate
                                    val hasEntries = entriesByDate.containsKey(dateStr)
                                    val isToday = date.isEqual(LocalDate.now())

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                when {
                                                    isSelected -> EmeraldPrimary
                                                    isToday -> EmeraldAccent.copy(alpha = 0.15f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable { onDateSelected(dateStr) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "${date.dayOfMonth}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                                    color = when {
                                                        isSelected -> Color.White
                                                        isToday -> EmeraldPrimary
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    },
                                                    fontSize = 13.sp
                                                )
                                            )
                                            if (hasEntries) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color.White else EmeraldAccent)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Selected Day Breakdown
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "DETAILS FOR ${selectedDaySummary.formattedDate.uppercase()}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Consumed, Burned, Net
                CalorieSummaryCards(
                    consumedCalories = selectedDaySummary.consumedCalories,
                    burnedCalories = selectedDaySummary.burnedCalories,
                    netCalories = selectedDaySummary.netCalories
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Macros
                MacroNutrientRow(
                    proteinG = selectedDaySummary.proteinG,
                    carbsG = selectedDaySummary.carbsG,
                    fatG = selectedDaySummary.fatG,
                    dailyCalorieTarget = userSettings.dailyCalorieTarget
                )
            }
        }

        // 4. Timeline items for this selected day
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LOGGED ITEMS (${selectedDayEntries.size})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                )
            }
        }

        if (selectedDayEntries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "No items logged for ${selectedDaySummary.formattedDate}.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(
                items = selectedDayEntries,
                key = { it.id }
            ) { entry ->
                HealthEntryCard(
                    entry = entry,
                    onEditClick = { editingEntry = entry },
                    onDeleteClick = { entryToDeleteId = entry.id }
                )
            }
        }
    }

    editingEntry?.let { entry ->
        EditEntryDialog(
            entry = entry,
            onDismiss = { editingEntry = null },
            onConfirmSave = { updated ->
                onEditEntry(updated)
                editingEntry = null
            }
        )
    }

    entryToDeleteId?.let { id ->
        ConfirmationDialog(
            title = "Delete Entry?",
            message = "This item will be removed from your health log.",
            confirmText = "Delete",
            isDestructive = true,
            onDismiss = { entryToDeleteId = null },
            onConfirm = {
                onDeleteEntry(id)
                entryToDeleteId = null
            }
        )
    }
}
