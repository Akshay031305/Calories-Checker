package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.HealthEntryEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.model.DailySummary
import com.example.ui.components.CalorieProgressRing
import com.example.ui.components.CalorieSummaryCards
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EditEntryDialog
import com.example.ui.components.HealthEntryCard
import com.example.ui.components.MacroNutrientRow
import com.example.ui.components.NaturalLanguageInputBar
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.time.LocalTime

@Composable
fun DashboardScreen(
    dailySummary: DailySummary,
    userSettings: UserSettingsEntity,
    isAnalyzing: Boolean,
    onLogSubmit: (String) -> Unit,
    onEditEntry: (HealthEntryEntity) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onLoadSampleData: () -> Unit,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingEntry by remember { mutableStateOf<HealthEntryEntity?>(null) }
    var entryToDeleteId by remember { mutableStateOf<String?>(null) }

    val greeting = remember {
        val hour = LocalTime.now().hour
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..22 -> "Good evening"
            else -> "Hello"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "$greeting, ${userSettings.userName.ifBlank { "there" }}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Today's Health",
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
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // 2. Natural Language Input Bar (Prominent at Center/Top)
        item {
            NaturalLanguageInputBar(
                isAnalyzing = isAnalyzing,
                onLogSubmit = onLogSubmit,
                onMicClick = onMicClick
            )
        }

        // 3. Calorie Progress Ring Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calorie_ring_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Calories",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    CalorieProgressRing(
                        consumedCalories = dailySummary.consumedCalories,
                        targetCalories = userSettings.dailyCalorieTarget
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Consumed, Burned, Net Summary
                    CalorieSummaryCards(
                        consumedCalories = dailySummary.consumedCalories,
                        burnedCalories = dailySummary.burnedCalories,
                        netCalories = dailySummary.netCalories
                    )
                }
            }
        }

        // 4. Macro Cards
        item {
            MacroNutrientRow(
                proteinG = dailySummary.proteinG,
                carbsG = dailySummary.carbsG,
                fatG = dailySummary.fatG,
                dailyCalorieTarget = userSettings.dailyCalorieTarget
            )
        }

        // 5. Timeline Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S TIMELINE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                )

                if (dailySummary.entries.isNotEmpty()) {
                    Text(
                        text = "${dailySummary.entries.size} items",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // 6. Timeline Entries or Empty State
        if (dailySummary.entries.isEmpty()) {
            item {
                EmptyStateCard(
                    onLoadSampleData = onLoadSampleData,
                    onQuickPromptClick = onLogSubmit
                )
            }
        } else {
            items(
                items = dailySummary.entries,
                key = { it.id }
            ) { entry ->
                HealthEntryCard(
                    entry = entry,
                    onEditClick = { editingEntry = entry },
                    onDeleteClick = { entryToDeleteId = entry.id }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Edit Dialog
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

    // Delete Confirmation Dialog
    entryToDeleteId?.let { id ->
        ConfirmationDialog(
            title = "Delete Entry?",
            message = "This item will be removed from your health log and totals will be updated.",
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
private fun EmptyStateCard(
    onLoadSampleData: () -> Unit,
    onQuickPromptClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_state_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                Image(
                    painter = painterResource(R.drawable.img_health_hero_1789963103446),
                    contentDescription = "Healthy lifestyle illustration",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nothing logged yet",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tell Healthify what you ate or what activity you did. Gemini will automatically calculate calories and nutrition.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Suggestions
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                listOf(
                    "3 eggs and 2 idlis",
                    "30 minute walk",
                    "Chicken biryani for lunch"
                ).forEach { sample ->
                    OutlinedButton(
                        onClick = { onQuickPromptClick(sample) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "“$sample”",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onLoadSampleData,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("load_sample_data_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Load sample data",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
