package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HealthEntryEntity
import com.example.data.model.HealthCalculators
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EditEntryDialog
import com.example.ui.components.HealthEntryCard
import com.example.ui.theme.CalorieBurnBlue
import com.example.ui.theme.CalorieIntakeOrange
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HistoryScreen(
    allEntries: List<HealthEntryEntity>,
    onEditEntry: (HealthEntryEntity) -> Unit,
    onDeleteEntry: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedDate by remember { mutableStateOf<String?>(null) }
    var editingEntry by remember { mutableStateOf<HealthEntryEntity?>(null) }
    var entryToDeleteId by remember { mutableStateOf<String?>(null) }

    // Group entries by date, sorted descending by date
    val groupedEntries = remember(allEntries) {
        allEntries.groupBy { it.date }
            .toList()
            .sortedByDescending { it.first }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Past Logs",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "History",
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
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        if (groupedEntries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No history records yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Items logged today and previously will appear grouped by date here.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(
                items = groupedEntries,
                key = { it.first }
            ) { (dateStr, entries) ->
                val isExpanded = expandedDate == dateStr
                val summary = remember(entries) {
                    HealthCalculators.calculateDailySummary(dateStr, entries)
                }

                HistoryDayCard(
                    dateStr = dateStr,
                    formattedDate = summary.formattedDate,
                    intakeKcal = summary.consumedCalories,
                    burnedKcal = summary.burnedCalories,
                    itemsCount = entries.size,
                    isExpanded = isExpanded,
                    entries = entries,
                    onToggleExpand = {
                        expandedDate = if (isExpanded) null else dateStr
                    },
                    onEditItem = { editingEntry = it },
                    onDeleteItem = { entryToDeleteId = it.id }
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
            message = "This historical item will be removed and day totals will update.",
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

@Composable
private fun HistoryDayCard(
    dateStr: String,
    formattedDate: String,
    intakeKcal: Double,
    burnedKcal: Double,
    itemsCount: Int,
    isExpanded: Boolean,
    entries: List<HealthEntryEntity>,
    onToggleExpand: () -> Unit,
    onEditItem: (HealthEntryEntity) -> Unit,
    onDeleteItem: (HealthEntryEntity) -> Unit
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "expand_rotation")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onToggleExpand() }
            .testTag("history_day_$dateStr"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${numberFormat.format(intakeKcal.toInt())} kcal intake",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CalorieIntakeOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        if (burnedKcal > 0) {
                            Text(
                                text = "•",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${numberFormat.format(burnedKcal.toInt())} kcal burned",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CalorieBurnBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$itemsCount items",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Expand day entries",
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotation),
                        tint = TextSecondary
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    entries.forEach { entry ->
                        HealthEntryCard(
                            entry = entry,
                            onEditClick = { onEditItem(entry) },
                            onDeleteClick = { onDeleteItem(entry) }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
